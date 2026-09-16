# Modern UI — Unofficial 26.2 Port

An **unofficial, multi-loader port** of [Modern UI](https://github.com/BloCamLimb/ModernUI) (`ModernUI-MC`) to **Minecraft 26.2**.
Modern UI is a desktop-style UI framework (view system, text layout engine, Arc3D renderer) that replaces and extends
Minecraft's GUI/rendering stack.

> This is a community port. All credit for the framework itself goes to **BloCamLimb** and the Modern UI contributors.
> Please report port-specific issues here, not upstream.

## Status

| | |
|---|---|
| Minecraft | 26.2 |
| Java | 25 |
| Loaders | NeoForge `26.2.0.88` · Fabric Loader `0.19.3+` |
| Mod version | `3.13.0.8-SNAPSHOT` |
| Core library | `dev.icyllis:modernui-core:3.13.0` (unmodified, from Maven) |
| License | LGPL-3.0-or-later |

Both loaders are built from one shared `common` module. **Forge is not supported.**

## Requirements

- **NeoForge**: only NeoForge itself.
- **Fabric**: [Fabric API](https://modrinth.com/mod/fabric-api) and
  [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) (required),
  [Mod Menu](https://modrinth.com/mod/modmenu) (optional, adds an in-game config entry).

## Building

JDK 25 and network access (for Gradle dependencies) are required.

```bash
./gradlew :ModernUI-NeoForge:build     # -> neoforge/build/libs/*-universal.jar
./gradlew :ModernUI-Fabric:build       # -> fabric/build/libs/*-universal.jar
./gradlew build                        # both loaders
```

The installable artifacts are the `*-universal.jar` files.

## Layout

```
common/     shared code, mixins, shaders and resources
neoforge/   NeoForge entrypoints and loader-specific patches
fabric/     Fabric entrypoints and loader-specific patches
libs/       locally bundled build-time artifacts (ModernUI-Fonts)
```

## 中文说明

本仓库是 **Modern UI 的 26.2 非官方移植**，同时支持 **NeoForge** 与 **Fabric**（不支持 Forge），
共用 `common` 模块。Fabric 端需要 **Fabric API** 与 **Forge Config API Port** 作为前置，
**Mod Menu** 为可选。构建产物为各模块 `build/libs/` 下的 `*-universal.jar`。

原框架版权归 **BloCamLimb** 所有；本项目基于上游代码移植，仅做版本适配。

## License

Licensed under the **GNU Lesser General Public License v3.0 or later** (LGPL-3.0-or-later).
See [LICENSE](LICENSE) for the full text.
