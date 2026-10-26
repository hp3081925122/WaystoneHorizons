# Waystone Horizons · 跃石远境

Waystones 的附属模组，新增三种保留原版轮廓的手持传送石。

| 物品 | 默认冷却 | 经验减免 | 配方 |
| --- | --- | --- | --- |
| 钻石传送石 | 150 秒 | 1 级 | 四角紫水晶碎片，四边末影珍珠，中心钻石 |
| 下界合金传送石 | 60 秒 | 2 级 | 下界合金升级锻造模板 + 钻石传送石 + 下界合金锭 |
| 下界之星传送石 | 30 秒 | 3 级 | 四角紫水晶碎片，四边末影珍珠，中心下界之星 |

费用为 `max(0, Waystones 原等级费用 - 减免等级)`，不会增加经验。Waystones 14.1.21 本身只按等级收费；界面显示和服务端实际扣费共用计算入口。普通传送石、传送卷轴、背包按钮与传送方块的费用保持上游行为。

三档物品共用独立的玩家冷却组，换物品不能绕过冷却，普通传送石不受此组影响。冷却记录随玩家保存，重进世界、跨维度和死亡重生后会同步。传送失败不会启动冷却或消耗耐久。

继承上游蓄力时长、持握动作、各阶段粒子、受伤打断及传送限制，沿用上游目标选择界面与传送实现。菜单绑定打开时实际使用的手和物品，双手持有不同档位时按所选物品计费，期间换掉物品会使菜单失效。保留 128 点耐久、成功传送消耗 1 点耐久、附魔光效和耐久条；下界合金传送石防火，锻造继承名称、附魔、耐久及其他完整 NBT。

三张纹理直接保留 1.21.1 版本的原版传送石换色贴图，透明区域、黑色描边及像素位置均一致。

## 配置

存档 `serverconfig/waystonehorizons-server.toml`，服务器配置会同步给客户端：

```toml
enableCooldowns = true
enableDurability = true

[cooldowns]
diamond = 150
netherite = 60
nether_star = 30
```

冷却单位为秒，范围 `0..2147483647`，单项设为 0 即无附属冷却。旧版 Waystones 没有新版的全局冷却与耐久开关，因此本附属提供对应的 `enableCooldowns`、`enableDurability`。Waystones 自身的原石冷却设置继续只作用于原物品。

## 开发环境

| 项目 | 版本 |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.23 |
| Java | 17 |
| Waystones | 14.1.21 |
| Balm | 7.3.44 |
| 本模组 | 0.0.1 |

工作树为 `WaystoneHorizons/forge-1.20.1-WaystoneHorizons`，分支 `1201forge`；`main` 为 1.21.1 NeoForge 基线。

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-17.0.18'
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runGameTestServer -PwithGameTests
```

正式构建输出 `build/libs/forge-1.20.1-WaystoneHorizons-0.0.1.jar`。Waystones 与 Balm 使用 CurseMaven；JEI、JEC、Jade、ALI 为模板保留的开发辅助依赖，不写入必需前置列表，也不打包到成品中。

`src/gametest` 仅在显式指定 `-PwithGameTests` 时参与编译；正式 JAR 排除测试类和结构。测试覆盖三档减免及实际传送、费用非负、共享冷却与原石隔离、配置与开关、主副手菜单绑定、未激活目标拒绝、完整配方及锻造 NBT 继承、耐久与防火。

[迁移记录](docs/migration-1.20.1-forge.md) · [依赖与源码记录](docs/dependencies.md)

服务端已由子代理完成实际 GameTest：1 项综合测试通过，包含三档实际传送与内部多项功能断言。客户端已用 Render Preview 实际验证三档固定展示渲染，与原版轮廓一致；手持、背包和客户端菜单交互未在真实世界中实测。完整证据见迁移文档。
