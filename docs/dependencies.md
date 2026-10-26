# 1.20.1 Forge 依赖与源码核对

| 依赖 | 版本 | CurseMaven |
| --- | --- | --- |
| Waystones | 14.1.21 | `curse.maven:waystones-245755:8849482` |
| Balm | 7.3.44 | `curse.maven:balm-531761:8969708` |
| JEI（开发） | 模板选定版本 | `curse.maven:jei-238222:8778011` |
| JEC（开发） | 模板选定版本 | `curse.maven:just-enough-characters-250702:8771525` |
| Jade（开发） | 模板选定版本 | `curse.maven:jade-324717:8479276` |
| ALI（开发） | 模板选定版本 | `curse.maven:advanced-loot-info-1205426:7451552` |

[Waystones 文件](https://www.curseforge.com/minecraft/mc-mods/waystones/files/8849482)；[Balm 文件](https://www.curseforge.com/minecraft/mc-mods/balm/files/8969708)。必需前置为 Waystones、Balm、Forge、Minecraft。版本要求按实际 API 需求设定最低值，不设置最高版本限制。

构建使用本机 1201-forge 模板、官方 Mojang 映射、Java 17、Forge 47.4.23。模板原有四项辅助依赖均保留。

实现前已读取 Waystones 14.1.21 与 Balm 7.3.44 实际 JAR 的反编译或 javap 签名，并核对 Waystones 官方 `1.20.1` 分支。重点包括 `WarpStoneItem`、`PlayerWaystoneManager`、`WaystoneSelectionMenu`、`SelectWaystoneMessage`、`IWaystoneTeleportContext`、`BalmNetworking`、`BalmMenuProvider`。Minecraft/Forge 的菜单工厂、注册、配置、NBT 锻造与耐久 API 已对照本地 1.20.1 官方映射源码，且由目标 47.4.23 的 compileJava 再验证。

不将上游反编译源码、运行缓存或前置 JAR 提交进仓库。
