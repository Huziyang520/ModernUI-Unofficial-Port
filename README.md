# Modern UI — 26.2 非官方移植版

**Modern UI** 在 **Minecraft 26.2** 上的非官方多加载器移植，同时支持 **NeoForge** 与 **Fabric**。

Modern UI 是一套面向 Minecraft 的桌面级 UI 框架，包含视图系统、Unicode 文本排版引擎与 Arc3D 渲染器，
用于替换并大幅增强原版的 GUI 与渲染管线。

> 本仓库为社区移植版本，框架本身著作权归原作者所有，仅做版本适配工作。

## 版本信息

| 项目 | 版本 |
|---|---|
| Minecraft | 26.2 |
| Java | 25 |
| NeoForge | 26.2.0.88 |
| Fabric Loader | 0.19.3+ |
| 模组版本 | 3.13.0.8-SNAPSHOT |
| 核心库 | `dev.icyllis:modernui-core:3.13.0`|
| 开源协议 | LGPL-3.0-or-later |

两个加载器由同一份 `common` 模块构建。
## 运行前置

- **NeoForge**：无需任何额外前置模组。
- **Fabric**：必须安装 [Fabric API](https://modrinth.com/mod/fabric-api) 与
  [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port)（前置依赖）；
  [Mod Menu](https://modrinth.com/mod/modmenu) 为可选，用于在游戏内打开配置界面。

## 构建

需要 JDK 25 以及网络连接（用于下载 Gradle 依赖）。

```bash
./gradlew :ModernUI-NeoForge:build     # 产物：neoforge/build/libs/*-universal.jar
./gradlew :ModernUI-Fabric:build       # 产物：fabric/build/libs/*-universal.jar
./gradlew build                        # 同时构建两个加载器
```

可安装的文件是各模块 `build/libs/` 下的 `*-universal.jar`。

## 目录结构

```
common/     共享代码、Mixin、着色器与资源
neoforge/   NeoForge 入口与加载器相关适配
fabric/     Fabric 入口与加载器相关适配
libs/       随仓库携带的构建期依赖（ModernUI-Fonts）
assets/     Blockbench 模型等美术源文件
```

## 致谢

- **[BloCamLimb](https://github.com/BloCamLimb/ModernUI)** —— Modern UI 原作者，本项目的全部基础框架均来自此仓库。
- **[DragonHua](https://github.com/drangonmc/ModernUI-Fabric26.2)** —— 26.2 Fabric 版移植作者，
  本仓库的 Fabric 端适配即基于其移植成果。
- 本仓库在其二人工作之上完成了 NeoForge 26.2 端移植，并将两端合并为统一的多加载器工程。

## 开源协议

以 **GNU Lesser General Public License v3.0 or later**（LGPL-3.0-or-later）发布，完整条款见 [LICENSE](LICENSE)。
