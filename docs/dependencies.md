# 26.1.2 NeoForge 依赖

| 依赖 | 版本 | 坐标 |
| --- | --- | --- |
| Waystones | 26.1.2.16 | curse.maven:waystones-245755:8837358 |
| Balm | 26.1.2.15 | curse.maven:balm-531761:8969765 |
| Shogi | 26.1.2.12 | curse.maven:shogi-1475746:8978486 |
| Shogi API（仅编译） | 26.1.2.12 | net.blay09.mods:shogi-api:26.1.2.12 |

官方发布页：[Waystones](https://www.curseforge.com/minecraft/mc-mods/waystones/files/8837358)、[Balm](https://www.curseforge.com/minecraft/mc-mods/balm/files/8969765)、[Shogi](https://www.curseforge.com/minecraft/mc-mods/shogi/files/8978486)。Shogi API 官方来源及用法：[项目文档](https://github.com/TwelveIterations/Shogi)，仓库 https://maven.twelveiterations.com/repository/maven-public/ 。

最低要求按实际使用的接口与已验证依赖声明，不加最高版本限制。Waystones本身要求Balm至少26.1.2.12和Shogi至少26.1.2.8；本附属依赖Shogi26.1.2.12提供的API。Shogi API已由Shogi内嵌，本附属不再次嵌入。

保留本机2612-neoforge模板的JEI(8771077)、JEC(8413185)、Jade(8651070)、ALI(8169069)四项开发依赖，专用服务端只保留编译引用。NeoForge26.1.2.95、ModDevGradle2.0.140、Gradle9.4.0、Java25，无混淆映射转换。

接口实现前已读取正式JAR反编译与字节码签名，包括WaystoneTeleportContextImpl、WaystonesRules、WarpStoneItem、WaystonesEffectExecutors、Shogi费用与冷却、EffectExecutor/模拟器/延迟执行器。Minecraft与NeoForge API依据本地2612sources和2612neoforge-sources，并通过目标编译与服务端实际运行验证。
