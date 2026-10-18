Changelogs
===

### Modern UI 3.14.8.1
Changes from
* 3.14.8 → 3.14.8.1 for Minecraft 26.3
#### All Extensions 26.3
* Fix the client crashing when text drawn with a custom bitmap font (for example the fonts a server resource pack provides for CustomFishing, Oraxen or ItemsAdder) appears in a title or subtitle: the GPU texture of that font could still be missing while the HUD was being built 修复自定义位图字体（例如服务端资源包为 CustomFishing、Oraxen、ItemsAdder 提供的字体）的文字出现在标题或副标题中时客户端崩溃的问题：构建 HUD 时该字体的 GPU 纹理可能尚未创建
* Text using a custom bitmap font is no longer dropped when its texture is still being initialized, it is drawn on the next frame instead 自定义位图字体的纹理仍在初始化时不再丢失这段文字，改为在下一帧绘制

### Modern UI 3.14.8
Changes from
* 3.14.7 → 3.14.8 for Minecraft 26.3
#### NeoForge Extension 26.3
* Fix the mod failing to load on the latest NeoForge 26.3 builds (26.3.0.77 and later) 修复本模组在最新 NeoForge 26.3 构建（26.3.0.77 及更高版本）上加载失败的问题
#### Fabric Extension 26.3
* No functional change, the version is only kept in sync with the NeoForge build 无功能改动，仅与 NeoForge 端同步版本号

### Modern UI 3.14.7
Changes from
* 3.14.6 → 3.14.7 for Minecraft 26.3
#### All Extensions 26.3
* Fix "§" formatting codes not working in chat, the player list and item custom names: every lower-case code (§a-§f, §k-§o, §r) was not recognized, so the "§" prefix was drawn as a visible character instead 修复聊天框、Tab 栏、物品自定义名称中 § 格式化代码不生效的问题：所有小写代码（§a-§f、§k-§o、§r）都无法被识别，§ 前缀被当成可见字符绘制出来
* Fix icons from icon resource packs (e.g. Icons) not showing while this mod is installed 修复安装本模组后图标类资源包（如 Icons）的图标不显示的问题
* Fix text being drawn as stretched white quads across the screen when a resource pack replaces Minecraft's core text shader (e.g. Recolourful Containers) 修复资源包替换原版文本核心着色器时（如 Recolourful Containers）文字被画成横跨屏幕的拉伸白色四边形的问题

### Modern UI 3.14.6
Changes from
* 3.14.5fix → 3.14.6 for Minecraft 26.3
#### All Extensions 26.3
* Fix color emoji used as icons by other mods (e.g. the quick backup / world switcher buttons of backup mods) not being drawn at all 修复其他模组用作图标的彩色 emoji（例如备份类模组的快捷备份、世界切换按钮）完全不显示的问题
* Fix icons drawn as emoji with thin strokes (star, tree, gear, trash) still not showing: the emoji atlas no longer uses mipmaps and the full resolution image is sampled now 修复细线条的 emoji 图标（星形、树木、齿轮、垃圾桶等）仍不显示的问题：emoji 图集不再使用 mipmap，改为采样全分辨率图像
* Log a warning when an emoji image cannot be produced and keep deferring to the next frame instead of silently drawing nothing 无法生成 emoji 图像时输出警告日志并继续延迟重试，不再静默地什么都不画
* Fix color emoji still missing in the 3D world: signs, entity name tags and text displays now draw emoji from the emoji atlas as well 修复 3D 世界内的彩色 emoji 仍不显示的问题：告示牌、实体命名牌、文本展示实体现在同样使用 emoji 图集绘制
* Color emoji in the world are no longer tinted by the text color, so they look exactly the same as in the GUI 世界内的彩色 emoji 不再被文字颜色染色，与界面内显示一致
* Fix the joiners and variation selectors inside multi-part emoji showing up as empty squares in the world text 修复世界内文本中多部件 emoji 的连接符与变体选择符被画成空白方块的问题

### Modern UI 3.14.5fix
Changes from
* 3.14.5 → 3.14.5fix for Minecraft 26.3
#### All Extensions 26.3
* Fix sign text rendering introduced by the previous version: the text no longer disappears when close and is properly occluded by blocks and entities 修复上一版引入的告示牌文字显示异常（不再近处消失，且会被方块与实体正确遮挡）

### Modern UI 3.14.5
Changes from
* 3.14.4 → 3.14.5 for Minecraft 26.3
#### All Extensions 26.3
* Fix missing anti-aliasing for in-world text (entity name tags, signs, text displays, maps, ...), the Modern UI text pipeline is now used again as before 修复世界内文本（实体命名牌、告示牌、文本展示实体、地图等）缺失抗锯齿的问题，现已恢复使用 Modern UI 文本管线

### Modern UI 3.14.4
Changes from
* 3.14.3 → 3.14.4 for Minecraft 26.3
#### Fabric Extension 26.3
* Add the "C" key zoom (4x), plus a "Zoom" option in the settings screen, exactly the same as the NeoForge version 新增 C 键 4 倍缩放（按住不放），设置界面新增"缩放"开关，与 NeoForge 端完全一致
#### NeoForge Extension 26.3
* Zoom now shares one implementation with the Fabric version, behavior unchanged 缩放改为与 Fabric 端共用同一实现，行为不变

### Modern UI 3.14.3
Changes from
* 3.14.2 → 3.14.3 for Minecraft 26.3
#### All Extensions 26.3
* Fix the published jar missing its bundled libraries (arc3d-compiler, commonmark), which crashed the game at startup on the default OpenGL renderer 修复发布包缺失内嵌依赖库（arc3d-compiler、commonmark），导致默认 OpenGL 渲染器下启动崩溃

### Modern UI 3.14.2
Changes from
* 3.14.1 → 3.14.2 for Minecraft 26.3
#### NeoForge Extension 26.3
* Fix a startup crash when the game runs on its built-in Vulkan renderer 修复游戏以内置 Vulkan 渲染器运行时的启动崩溃
* Fix a crash when exiting the game on the built-in Vulkan renderer 修复使用内置 Vulkan 渲染器时退出游戏崩溃
* Fix the square icon not showing in the mod list 修复模组列表中不显示方形图标
* Show a hand cursor when hovering buttons in the settings screen 配置界面中鼠标悬停按钮时显示手形光标
* Add the backup issue tracker and replace the mod list background image 新增备用反馈入口，更换模组列表背景图
#### Fabric Extension 26.3
* Fix a crash when exiting the game on the built-in Vulkan renderer 修复使用内置 Vulkan 渲染器时退出游戏崩溃
* Show a hand cursor when hovering buttons in the settings screen 配置界面中鼠标悬停按钮时显示手形光标
* Add the backup issue tracker, also as a clickable link in the Mod Menu "Links" section, and replace the mod list background image 新增备用反馈入口（并在 Mod Menu"链接"区提供可点击链接），更换模组列表背景图

### Modern UI 3.14.1
Changes from
* 3.14.0 → 3.14.1 for Minecraft 26.3
#### All Extensions 26.3
* Support Minecraft's built-in Vulkan renderer, no more crash at startup when the game runs on Vulkan 支持 Minecraft 内置 Vulkan 渲染器，游戏以 Vulkan 运行时不再启动崩溃

### Modern UI 3.14.0
Changes from
* 3.13.0.10 → 3.14.0 for Minecraft 26.3
#### All Extensions 26.3
* Fix mod icon not showing in mod lists 修复模组图标在模组列表中不显示（改为 128×128 正方形图标）
* Fix the "Full Changelog" link in the dashboard 修复操作中心"完整日志"链接指向错误

### Modern UI 3.13.0.10
Changes from
* 3.13.0.5 → 3.13.0.10 for Minecraft 26.2-26.3
#### NeoForge Extension 26.3
* Fix modern tooltip background not rendering (GPU uniform block layout) 修复现代提示框背景不显示
* Fix post-processing shaders (menu blur, grayscale, radial blur) failing to load 修复菜单模糊等后处理着色器加载失败导致的资源重载提示与花屏
* Fix keyboard input in Modern UI screens (ESC to close, etc.) 修复界面内键盘输入失效（如 ESC 无法退出）
* Fix mouse clicks and slider dragging not working in config screen 修复配置界面鼠标点击与滑块拖动失效
* Fix possible crash when closing screens during game shutdown 修复退出游戏时关闭界面可能导致的崩溃
#### Fabric Extension 26.3
* Same as NeoForge version, plus fix modern tooltip not taking effect (mixin method signature) 同 NeoForge 版本，另修复现代提示框在 Fabric 端不生效（Mixin 方法签名漂移）
