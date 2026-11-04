# Modern UI — Unofficial Port

An unofficial multi-loader port of Modern UI for Minecraft, supporting both NeoForge and Fabric.

Modern UI is a desktop-grade UI framework for Minecraft, featuring a view system, a Unicode text layout engine, and the Arc3D renderer. It replaces and substantially enhances vanilla's GUI and rendering pipeline.

This repository is a community port. The copyright of the framework itself belongs to its original author; this project only handles version adaptation.

## Editions

Two editions are published for every Minecraft version. Pick one file, not both.

| Edition | What you get |
|---|---|
| **Modern UI** | The complete build: the in-house settings screen, the bundled font pack, and everything below. |
| **Modern UI Lite** | The same rendering and text engine, but without the in-house settings screen and without the bundled font pack. Smaller download, lower memory use. |

### Modern UI Lite — read this first

- **A config library is required.** Lite has no settings screen of its own; every option is drawn by [YetAnotherConfigLib](https://modrinth.com/mod/yacl). Without it the mod still runs, but there is no way to change any option in-game.
- **The settings entry opens YetAnotherConfigLib directly.** In the mod list, or with the config key, you land straight in the library's screen.
- **Fabric needs [Fabric API](https://modrinth.com/mod/fabric-api) and [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port).** [Mod Menu](https://modrinth.com/mod/modmenu) is optional and only provides the in-game entry point.
- **The bundled font pack is absent**, so the default font comes from your system instead of the shipped families. You can pick a family in the settings.
- **A notice is printed in chat when you join a world.** Turn it off under "About & Links" in the settings screen.
- **Do not install both editions at once.** They share the same mod id; the launcher will refuse to load the second one.

## Requirements

- **NeoForge**: No additional dependency mods required.
- **Fabric**: [Fabric API](https://modrinth.com/mod/fabric-api) and [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) are required (dependencies). [Mod Menu](https://modrinth.com/mod/modmenu) (or a similar mod) is optional, used to open the config screen in-game.

## Credits

- [BloCamLimb](https://github.com/BloCamLimb) — Original author of Modern UI. All foundational frameworks of this project come from the original repository.
- [DragonHua](https://github.com/DragonHua) — Author of the Fabric port. The Fabric adaptation in this repository is based on their work.

Building on their work, this repository completes the NeoForge port, adapts it to newer Minecraft versions, and merges both loaders into a unified multi-loader project.

## License

Released under the GNU Lesser General Public License v3.0 or later (LGPL-3.0-or-later). See [LICENSE](LICENSE) for the full terms.

---

# Modern UI — 非官方移植版

Modern UI 在 Minecraft 上的非官方多加载器移植，同时支持 NeoForge 与 Fabric。

Modern UI 是一套面向 Minecraft 的桌面级 UI 框架，包含视图系统、Unicode 文本排版引擎与 Arc3D 渲染器，用于替换并大幅增强原版的 GUI 与渲染管线。

本仓库为社区移植版本，框架本身著作权归原作者所有，仅做版本适配工作。

## 两个版本

每个 Minecraft 版本都会发布两个版本，**只装其中一个**。

| 版本 | 内容 |
|---|---|
| **Modern UI** | 完整版：自带设置界面、自带字体包，以及下文全部功能。 |
| **Modern UI Lite** | 渲染与文本引擎相同，但**不含自带设置界面**，也**不含自带字体包**。体积更小、内存占用更低。 |

### Modern UI Lite 必看

- **必须另装配置库。** Lite 版没有自己的设置界面，所有配置项由 [YetAnotherConfigLib](https://modrinth.com/mod/yacl) 绘制。不装该模组时游戏能正常启动，但游戏内无法修改任何配置。
- **配置入口直接打开 YetAnotherConfigLib。** 无论从模组列表还是快捷键进入，看到的都是配置库的界面。
- **Fabric 端需要 [Fabric API](https://modrinth.com/mod/fabric-api) 与 [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port)。** [Mod Menu](https://modrinth.com/mod/modmenu) 为可选，只负责提供游戏内入口。
- **不含自带字体包**，默认字体取自系统字体而非随包字体，可在设置里改选字体家族。
- **进入世界时会在聊天栏提示一次。** 可在设置界面的「关于与链接」中关闭。
- **不要同时安装两个版本。** 两者 mod id 相同，加载器会拒绝加载后装的那个。

## 运行前置

- **NeoForge**：无需任何额外前置模组。
- **Fabric**：必须安装 [Fabric API](https://modrinth.com/mod/fabric-api) 与 [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port)（前置依赖）；[Mod Menu](https://modrinth.com/mod/modmenu)（或其他类似模组）为可选，用于在游戏内打开配置界面。

## 致谢

- [BloCamLimb](https://github.com/BloCamLimb) —— Modern UI 原作者，本项目的全部基础框架均来自其仓库。
- [DragonHua](https://github.com/DragonHua) —— Fabric 版移植作者，本仓库的 Fabric 端适配即基于其移植成果。

本仓库在其二人工作之上完成了 NeoForge 端移植，并进一步适配至更新的 Minecraft 版本，将两端合并为统一的多加载器工程。

## 开源协议

以 GNU Lesser General Public License v3.0 or later（LGPL-3.0-or-later）发布，完整条款见 [LICENSE](LICENSE)。
