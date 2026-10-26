# Waystone Horizons · 跃石远境

Waystones 附属模组，新增三档手持传送石，保持原版传送石轮廓并替换色板。

| 物品 | 默认冷却 | 经验减免 | 配方 |
| --- | --- | --- | --- |
| 钻石传送石 | 150 秒 | 1 级 | 四角紫水晶碎片，四边末影珍珠，中心钻石 |
| 下界合金传送石 | 60 秒 | 2 级 | 下界合金升级锻造模板 + 钻石传送石 + 下界合金锭 |
| 下界之星传送石 | 30 秒 | 3 级 | 四角紫水晶碎片，四边末影珍珠，中心下界之星 |

保留上游蓄力动作、所有粒子阶段、受伤打断、充能音效、目标选择与传送限制；附属物品为128耐久，成功传送消耗1点耐久，下界合金传送石防火。锻造继承基底的完整组件，包括名称、附魔和损耗。三张16×16贴图与1.21.1基线逐字节一致。

## 费用与冷却

等级费用为 `max(0, 原费用 - 减免等级)`。点数收费按玩家等级和经验条进度折算相应等级的经验价值。等级与点数混合收费时，先减等级费用，再把剩余额度用于点数；规则顺序或多条费用不会重复优惠。免费传送不返还经验。

适配 Waystones 26.1.2 的 Shogi 规则系统，在生成原生费用信息、校验支付能力和实际消费前统一调整，保留其他收费、条件和拒绝结果。为了使混合收费始终按等级优先，会先使用上游模拟执行器做一次不消费的规则求值，再通过原执行器执行真实费用计算；规则解析继续使用上游缓存。

三档传送石共享独立的玩家冷却组，换档不能绕过；普通上游传送石及上游新材质传送石保持原有行为。冷却使用持久化的long毫秒到期时间，支持重进世界、重生及最大配置范围。原生菜单从服务端同步剩余冷却和费用信息。

存档 `serverconfig/waystonehorizons-server.toml`，服务器配置同步客户端：

```toml
[cooldowns]
diamond = 150
netherite = 60
nether_star = 30
```

单位为秒，范围 `0..2147483647`，0表示不添加本附属冷却。尊重 Waystones `rules.enableCooldowns` 和 `rules.enableDurability` 总开关，创造模式的支付能力豁免继续由上游处理。保持本附属每次1点损耗，因此附属物品不采用新版上游默认80点损耗；其他物品与规则效果不受此适配影响。

## 开发与前置

| 项目 | 版本 |
| --- | --- |
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.95 |
| Java | 25 |
| 本模组 | 0.0.1 |
| Waystones | 26.1.2.16 |
| Balm | 26.1.2.15 |
| Shogi | 26.1.2.12 |

三个前置通过 CurseMaven 引入。Shogi API 仅从作者官方 Maven 作为编译依赖，运行时由 Shogi 自带嵌入 API 提供，不重复打包。JEI、JEC、Jade、ALI 保留为模板开发辅助依赖，专用服务端任务不把它们加入运行时；它们不列为正式前置。

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\jdk-25.0.4+7'
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runGameTestServer -PwithGameTests
```

产物为 `build/libs/neoforge-26.1.2-WaystoneHorizons-0.0.1.jar`。工作树路径 `WaystoneHorizons/neoforge-26.1.2-WaystoneHorizons`，分支 `2612neoforge`。

`src/gametest` 仅在 `-PwithGameTests` 时参与开发编译，正式JAR排除测试类与结构。测试使用26.1.2的注册式GameTest入口；服务端已由子代理实际运行并通过2项必需测试，其中1项为本附属综合回归。

[迁移与验证记录](docs/migration-26.1.2-neoforge.md) · [依赖记录](docs/dependencies.md)
