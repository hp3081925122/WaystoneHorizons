# Waystone Horizons · 跃石远境

Waystones 的附属模组，扩展手持传送石（Warp Stone）的种类、冷却与经验减免。

| 物品 | 默认冷却 | 传送经验减免 | 合成方式 |
| --- | --- | --- | --- |
| 钻石传送石 | 150 秒 | 1 级 | 四角紫水晶碎片、四边末影珍珠、中心钻石 |
| 下界合金传送石 | 60 秒 | 2 级 | 锻造台：下界合金升级锻造模板 + 钻石传送石 + 下界合金锭 |
| 下界之星传送石 | 30 秒 | 3 级 | 四角紫水晶碎片、四边末影珍珠、中心下界之星 |

保留 Waystones 原有蓄力、受伤打断、粒子、目标选择、传送限制及耐久机制。下界合金升级继承基底的名称、附魔、耐久等组件；下界合金传送石本身防火。

三种物品贴图均以 Waystones 原始传送石的 16×16 贴图为基础，仅替换七色色板；原始透明区域、黑色描边、像素坐标及内部明暗分区全部保留。

## 冷却与经验

配置文件为存档中的 `serverconfig/waystonehorizons-server.toml`，服务器配置会同步到客户端：

```toml
[cooldowns]
diamond = 150
netherite = 60
nether_star = 30
```

单位是秒，允许范围 `0..2147483647`，设为 `0` 时该物品无附属冷却。三种传送石共享玩家冷却组，使用时按本次物品的配置启动冷却，换另一种传送石不能绕过已存在的冷却。Waystones 总开关 `enableCooldowns=false` 同样停用附属冷却。独立于原传送石和背包按钮冷却组。

等级收费采用 `max(0, 原费用 - 减免等级)`。Waystones 21.1.46 默认使用经验点收费，此时按玩家当前等级和经验条进度折算相应等级的经验价值，再从点数费用中扣除；等级不足减免量时，以减免等级为折算基准。等级、经验点同时收费时，先减等级费用，再将剩余减免额度用于经验点费用，避免重复优惠。所有结果最低为 0，免单不会返还或增加经验。

例如玩家为 10 级、经验条为空时，三种物品分别减免 25、48、69 个经验点。若 Waystones 原费不足减免量，则免费。传送界面和服务端扣费使用同一计算入口；Waystones 自定义的其他物品费用及限制继续生效。

## 开发环境

| 项目 | 版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.233 |
| Java | 21 |
| 模组版本 | 0.0.1 |
| 模组 ID | `waystonehorizons` |
| Java 包 | `com.waystonehorizons` |
| Waystones | 21.1.46 |
| Balm | 21.0.66 |

目录遵循 `项目名字/模组加载器-mc版本-项目名字`，本工作区为 `WaystoneHorizons/neoforge-1.21.1-WaystoneHorizons`。

## 构建与运行

在此目录打开 PowerShell：

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-21.0.11'
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

三个 Gradle 命令分别用于构建、启动开发客户端和启动开发服务端，按需单独执行。首次服务端启动需要自行阅读并接受 Minecraft EULA。

成品输出到 `build/libs/neoforge-1.21.1-WaystoneHorizons-0.0.1.jar`。

Gradle 沿用本机 `127.0.0.1:7892` 代理；移到其他电脑时按当地环境修改 `gradle.properties`。

## 前置依赖

Waystones 和 Balm 通过 CurseMaven 加入编译及开发运行环境，均在 `neoforge.mods.toml` 中声明为客户端与服务端必装。前置不会被打入本模组的 JAR，正式使用时需单独安装。

- [Waystones 21.1.46，NeoForge 1.21.1](https://www.curseforge.com/minecraft/mc-mods/waystones/files/8969751)
- [Balm 21.0.66，NeoForge 1.21.1](https://www.curseforge.com/minecraft/mc-mods/balm/files/8969738)
- [依赖与源码核对记录](docs/dependencies.md)

## 服务端集成测试

`src/gametest` 包含回归测试与独立空结构，仅在显式传入属性时参与开发编译：

```powershell
.\gradlew.bat runGameTestServer -PwithGameTests
```

测试覆盖三种费用减免、费用非负、零费用不加经验、经验点与等级混合收费、配置冷却、零冷却、总开关、跨物品冷却拦截、原物品行为保持、三张配方、锻造组件继承及实际传送扣费。正式 `build` 不包含测试类和结构。
