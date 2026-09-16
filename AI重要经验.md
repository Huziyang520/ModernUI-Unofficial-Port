# AI 协作重要经验记录

> 沉淀与 AI 协作修复问题时值得复用的方法论、踩坑教训与协作要点。
> 记录原则：**只写有证据支撑的结论**；误判同样记录，因为它们比成功更有信息量。

---

## 案例一：Mixin LVT 静默失效 —— tooltip 背景整块消失

| 项目 | 内容 |
|---|---|
| 时间 | 2026-09-16 |
| 场景 | Modern UI 26.2 移植（NeoForge + Fabric 双加载器） |
| 性质 | 典型的"运行期静默失效"，AI 连续两轮误判，最终靠反汇编取证定位 |
| 结果 | 已修复并实机验证通过 |

### 1. 现象

开启"现代提示框"功能后，物品悬浮提示**只有文字、没有背景**，NeoForge 与 Fabric **同时出现**。

关键诱导因素：**文字正常，只有背景缺失** —— 这个"部分正常"的表象让排查方向多次跑偏。

### 2. 根因

MC 26.2 将 `GuiRenderer.executeDrawRange` 从 **3 个参数**改为 **5 个参数**（追加 `int startIndex, int endIndex`），
而 `common/.../mixin/MixinGuiRenderer.java` 的 `@Inject` 处理器仍按 26.1.2 声明：

```java
// 错误（26.1.2 遗留）：3 个目标参数 + 5 个陈旧捕获变量
onExecuteDrawRange(Supplier, RenderTarget, GpuBufferSlice,
                   GpuBufferSlice, GpuBuffer, Object, int, int, CallbackInfo, RenderPass)

// 正确（26.2）：5 个目标参数 + 只捕获 renderPass
onExecuteDrawRange(Supplier, RenderTarget, GpuBufferSlice, int, int, CallbackInfo, RenderPass)
```

参数从第 4 个起全部错位 → Mixin 在注入点无法解析局部变量表（LVT）→ 捕获到的 `renderPass` 无效
→ `renderPass.setUniform("ModernTooltip", tooltipUniforms)` **从未执行**
→ 管线拿不到 tooltip 的 uniform → 背景整块不绘制（文字走 `prepareText` 另一条路径，所以照常显示）。

### 3. 决定性证据

**证据 A —— 运行日志早已写明（却被当成噪音）**：

```
[Render thread/WARN] [mixin/]: Injection warning: LVT in
net/minecraft/client/gui/render/GuiRenderer::executeDrawRange(...)V
has incompatible changes at opcode 37
in callback mixins.modernui-neoforge.json:MixinGuiRenderer
from mod modernui->@Inject::onExecuteDrawRange(...)
```

**证据 B —— `javap -l` 反汇编 26.2 官方包，拿到真实签名与 LVT**：

```
private void executeDrawRange(Supplier<String>, RenderTarget, GpuBufferSlice, int, int)

Start  Length  Slot  Name                Signature
   78       8     8  draw                GuiRenderer$Draw
   55      37     7  i                   I
   37      95     6  renderPass          Lcom/mojang/blaze3d/systems/RenderPass;   ← 注入点 opcode 37 处有效
    0     133     1  label               Ljava/util/function/Supplier;
    0     133     2  mainRenderTarget    Lcom/mojang/blaze3d/pipeline/RenderTarget;
    0     133     3  dynamicTransforms   Lcom/mojang/blaze3d/buffers/GpuBufferSlice;
    0     133     4  startIndex          I
    0     133     5  endIndex            I
```

**证据 C —— 反汇编自己构建的产物，确认修复真的落盘**：

```
产物: onExecuteDrawRange(Supplier, RenderTarget, GpuBufferSlice, int, int, CallbackInfo, RenderPass)
目标: executeDrawRange    (Supplier, RenderTarget, GpuBufferSlice, int, int)
                          └────────── 前 5 个一一对应 ──────────┘
```

### 4. 踩坑记录（AI 的误判，供警惕）

| # | AI 的判断 | 实际情况 | 代价 |
|---|---|---|---|
| 1 | "tooltip 缺背景 = 管线未声明 bind group" → 改写 `GuiRenderType` | 该修复**必要但非根因**（只消除了 `unknown and unsupported uniform` 警告） | 用户白测 1 轮 |
| 2 | "shader `#version 150` 与 26.2 的 330 不一致" → 升级版本 | 与现象**完全无关** | 用户白测 1 轮 |
| 3 | 日志里的 `Injection warning: LVT ...` 被判定为 "`CAPTURE_FAILSOFT`，捕获失败时跳过回调，不影响运行" | **它就是根因**。`CAPTURE_FAILSOFT` 会静默传 null，让功能消失而不崩溃 | 掩盖根因达两轮 |
| 4 | 既有文档写"Fabric 端一切正常" | 用户实测**两端同时失效**（根因在 common，两端必然同坏） | 排查方向被错误限定为 NeoForge |

**共性归纳**：

- 1~2 属于 **"用看起来合理的推断代替取证"** —— 改动本身不错，但没有验证它是否触及根因；
- 3~4 属于 **"轻信既有文档/日志中的自述结论"** —— 把已知信息当成已结论，而没有回头复核。

### 5. 解决问题的关键（可复用方法论）

1. **先取证，后动手** —— 把现象落到具体证据（日志行 + 字节码），再决定改什么；不要从"最像的原因"开始改。
2. **反汇编官方包** —— `javap -p -c -l` 拿真实签名与 LVT，不要凭旧版本代码想象新版本 API。
3. **横向对比上游** —— `git show upstream/master:<path>` 对照 26.1.2 原版，差异即线索。
4. **纵向验证产物** —— 构建后反汇编**自己产出的 class**，确认修复真的写进去了（而不是只改了源码）。
5. **全量扫描同类风险** —— 一次把所有 `LocalCapture` / `CAPTURE_FAILHARD` / `require = 0` 查干，避免"修一个漏一个"。
6. **信任 `Injection warning`** —— 它是 Mixin 明确的失败信号，不是噪音。

### 6. Mixin 三类失效模型（重要）

| 类型 | 表现 | 发现时机 | 危险度 |
|---|---|---|---|
| 编译期可见 | 找不到类 / 方法 | 编译 | 低 |
| 启动期暴露 | `@At` 目标不存在 → 直接崩溃 | 启动 | 低（会叫） |
| **运行期静默** | **LVT 捕获失败 → 功能消失，日志仅一行 WARN** | **用户实测** | **高（不叫）** |

第三类是本案例的核心：**它不会崩，只会"功能不对"，唯一线索是一行极易被忽略的 WARN。**
这也解释了为什么这类 bug 会连续穿越多轮修复。

### 7. 用户在本案例中的作用

> AI 无法启动游戏、无法实机验证。在"运行期静默失效"这类问题面前，
> **用户是唯一的信息来源，也是唯一的真值裁判。**

1. **提供唯一真实的运行时反馈** —— 截图、日志、现象描述是 AI 收敛判断的唯一依据。
2. **推翻 AI 的错误结论** —— "经实测，neoforge 和 fabric 都存在问题" 直接纠正了"Fabric 正常"的误判，
   把排查从"平台差异"拉回"common 层"，这是本案例的转折点。
3. **施压要求证据** —— 用户对"什么都不改就端上来""每次都要我手动测试"的强烈质疑，
   是 AI 从"猜测-试探"转向"取证-验证"的直接推力。**用户的情绪不是干扰，而是质量信号。**
4. **承担验证成本** —— 每次改动都要用户手动启动测试。因此 **降低来回次数是 AI 的责任**，
   不能把"试试看"当作交付；一次交付前应把能静态验证的部分全部验完。

### 8. 移植期 Checklist（下次直接照做）

**排查渲染 / 功能类问题时**：

- [ ] 全量检索日志：`Injection warning` / `LVT` / `incompatible` / `unknown and unsupported uniform` / `Couldn't find source`
- [ ] 对每个 `@Inject + LocalCapture` 逐条 `javap -l` 比对目标方法签名与 LVT
- [ ] 对每个 `@Overwrite` / `@Redirect` / `@ModifyVariable` 核对目标签名
- [ ] 构建后反汇编产物 class 复核修复是否落盘
- [ ] 检索 `require = 0`（静默放行）与注释外的 `CAPTURE_FAILHARD`
- [ ] 现象若"部分正常"，先问：**正常的那部分走的是哪条渲染/代码路径？** 差异处即线索
- [ ] 现象若"多平台同时出现"，优先怀疑 common 层共享代码

**命令模板**：

```powershell
# 目标方法的签名与局部变量表
javap -p -c -l -cp <minecraft.jar> <类全名>

# 对照 26.1.2 原版实现
git show upstream/master:<相对路径>

# 复核自己构建的产物
javap -p -cp <产物.jar> <mixin类全名>
```

### 9. 协作原则（写给未来的 AI）

1. **无法验证的部分，不要用"应该"来交付。** 明确区分"已验证"与"推测"。
2. **改动前先回答：这个改动如果正确，它能解释全部现象吗？** 若只能解释一部分，就不是根因。
3. **用户反馈是最高优先级证据**，高于任何既有文档、注释和历史结论。
4. **交付时给出验证方式与剩余风险**，让用户知道该测什么、以及失败时下一步查什么。
5. **判断失误要主动说明**，因为用户有权知道哪些结论可信、哪些只是"碰运气"。
