# 依赖与源码核对

核对日期：2026-10-26。

| 依赖 | 开发版本 | CurseForge 项目 ID | 文件 ID | Gradle 坐标 |
| --- | --- | --- | --- | --- |
| Waystones | 21.1.46 | 245755 | 8969751 | `curse.maven:waystones-245755:8969751` |
| Balm | 21.0.66 | 531761 | 8969738 | `curse.maven:balm-531761:8969738` |

两份正式 JAR 均已下载并检查 `META-INF/neoforge.mods.toml`，确认是 Minecraft 1.21.1 的 NeoForge 构建。依赖采用 `implementation`，前置模组独立安装，不进行重打包。

Waystones 21.1.46 自身要求 Balm `[21.0.39,)`，本项目沿用该实际最低要求。当前费用集成已使用 21.1.46 的实际类与方法，因此 Waystones 最低要求提高为 `[21.1.46,)`。声明不设最高版本，未测试的 Minecraft 或前置版本不因此获得兼容保证。

## 已核对的上游源码

- [Waystones 1.21.1 分支](https://github.com/TwelveIterations/Waystones/tree/1.21.1)
- [手持传送石 WarpStoneItem](https://github.com/TwelveIterations/Waystones/blob/1.21.1/common/src/main/java/net/blay09/mods/waystones/item/WarpStoneItem.java)
- [NeoForge 前置声明](https://github.com/TwelveIterations/Waystones/blob/1.21.1/neoforge/src/main/resources/META-INF/neoforge.mods.toml)
- [上游版本参数](https://github.com/TwelveIterations/Waystones/blob/1.21.1/gradle.properties)

`WarpStoneItem` 使用 `WaystoneSelectionListBuilder.withTargetsForItem(...)` 接入传送目标列表，并通过传送完成回调处理耐久。后续拓展手持传送石时，应先确认当前二进制与源码一致，再复用这些入口。

本次已直接反编译固定版本正式 JAR：新物品继承 `WarpStoneItem`，未重写其使用与传送逻辑；在 `InternalMethodsImpl.resolveRequirements(...)` 返回处统一处理费用与冷却，确保目标列表和服务端收费一致。使用上游 `ExperienceLevelRequirement`、`ExperiencePointsRequirement` 和 `CooldownRequirement`，其余要求保留原对象。普通辅助代码位于普通包，Mixin 包仅存放注入类。

用户提供的 [MC百科物品页](https://www.mcmod.cn/item/144510.html) 指向 `waystones:warp_stone`，本项目后续扩展对象是这一手持传送物品。
