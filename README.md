# Modern UI — Unofficial Port

An unofficial multi-loader port of Modern UI for Minecraft, supporting both NeoForge and Fabric.

Modern UI is a desktop-grade UI framework for Minecraft, featuring a view system, a Unicode text layout engine, and the Arc3D renderer. It replaces and substantially enhances vanilla's GUI and rendering pipeline.

This repository is a community port. The copyright of the framework itself belongs to its original author; this project only handles version adaptation.

Both loaders are built from the same common module.

## Requirements

- **NeoForge**: No additional dependency mods required.
- **Fabric**: Fabric API and Forge Config API Port are required. [Mod Menu](https://modrinth.com/mod/modmenu) (or a similar mod) is optional, used to open the config screen in-game.

## Building

Requires a JDK and an internet connection (to download Gradle dependencies).

```bash
./gradlew :ModernUI-NeoForge:build     # Output: neoforge/build/libs/*-universal.jar
./gradlew :ModernUI-Fabric:build       # Output: fabric/build/libs/*-universal.jar
./gradlew build                        # Build both loaders
```

The installable files are the `*-universal.jar` under each module's `build/libs/`.

## Project Structure

```text
common/     Shared code, Mixins, shaders and resources
neoforge/   NeoForge entry point and loader-specific adaptation
fabric/     Fabric entry point and loader-specific adaptation
libs/       Build-time dependencies bundled with the repo (ModernUI-Fonts)
assets/     Art source files such as Blockbench models
```

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

两个加载器由同一份 common 模块构建。

## 运行前置

- **NeoForge**：无需任何额外前置模组。
- **Fabric**：必须安装 Fabric API 与 Forge Config API Port（前置依赖）；[Mod Menu](https://modrinth.com/mod/modmenu)（或其他类似模组）为可选，用于在游戏内打开配置界面。

## 构建

需要 JDK 以及网络连接（用于下载 Gradle 依赖）。

```bash
./gradlew :ModernUI-NeoForge:build     # 产物：neoforge/build/libs/*-universal.jar
./gradlew :ModernUI-Fabric:build       # 产物：fabric/build/libs/*-universal.jar
./gradlew build                        # 同时构建两个加载器
```

可安装的文件是各模块 `build/libs/` 下的 `*-universal.jar`。

## 目录结构

```text
common/     共享代码、Mixin、着色器与资源
neoforge/   NeoForge 入口与加载器相关适配
fabric/     Fabric 入口与加载器相关适配
libs/       随仓库携带的构建期依赖（ModernUI-Fonts）
assets/     Blockbench 模型等美术源文件
```

## 致谢

- [BloCamLimb](https://github.com/BloCamLimb) —— Modern UI 原作者，本项目的全部基础框架均来自其仓库。
- [DragonHua](https://github.com/DragonHua) —— Fabric 版移植作者，本仓库的 Fabric 端适配即基于其移植成果。

本仓库在其二人工作之上完成了 NeoForge 端移植，并进一步适配至更新的 Minecraft 版本，将两端合并为统一的多加载器工程。

## 开源协议

以 GNU Lesser General Public License v3.0 or later（LGPL-3.0-or-later）发布，完整条款见 [LICENSE](LICENSE)。