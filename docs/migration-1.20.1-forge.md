# Minecraft 1.20.1 Forge 迁移记录

开始日期：2026-10-26。此文档在迁移代码修改前建立。

## 分支与工作区

- 源版本：Minecraft 1.21.1、NeoForge 21.1.233、Java 21，版本号 0.0.1。
- 目标版本：Minecraft 1.20.1、Forge 47.4.23、Java 17，版本号保持 0.0.1。
- 目标分支：`1201forge`。
- 独立 Git 工作树：`WaystoneHorizons/forge-1.20.1-WaystoneHorizons`。
- 首先提交并推送现有源版本，再从该提交创建分支及工作树。
- 构建配置从本机 `1201-forge` 模板建立，保留模板辅助开发依赖。

## 功能基线与不可简化项

| 项目 | 必须保留的行为 |
| --- | --- |
| 钻石传送石 | 默认冷却 150 秒，经验费用减少 1 级 |
| 下界合金传送石 | 默认冷却 60 秒，经验费用减少 2 级；物品防火 |
| 下界之星传送石 | 默认冷却 30 秒，经验费用减少 3 级 |
| 冷却配置 | 服务端配置分别控制三个秒数，允许 0，同步客户端；三种共享玩家冷却，换物品不能绕过 |
| 经验下限 | 费用最小为 0，免单不能返还经验；兼容目标上游真实收费单位 |
| 混合费用 | 如上游同时支持等级与点数，减免额度仅使用一次，其他限制不移除 |
| 上游行为 | 保留蓄力、受伤打断、原有粒子、音效、目标选择、传送限制、耐久以及配置总开关 |
| 工作台配方 | 四角紫水晶碎片、四边末影珍珠、中心钻石或下界之星 |
| 锻造配方 | 下界合金升级模板 + 钻石传送石 + 下界合金锭，保留名称、附魔、耐久等物品数据 |
| 物品图标 | 直接保留已修正的原版传送石换色色板贴图，禁止重新设计形状 |
| 本地化 | 中文、英文名称与提示同时保留 |

## 适配调查与实施原则

1. 直接检查目标 Waystones 正式 JAR 与源码，再确定费用、冷却、GUI 和网络挂钩点，不能照搬新版本内部类。
2. 使用目标 Forge 注册与配置接口；1.20.1 物品使用 NBT，不使用 1.21 数据组件 API。
3. 数据目录回迁为 `recipes`、`advancements`、`tags/items`、`structures`，配方结果使用 `item` 字段，资源包格式按 1.20.1 设置。
4. 费用与冷却的显示和实际服务端校验必须一致，不能只改变提示或传送后的经验值。
5. 新版本独有而目标上游没有的架构，需在附属中补齐等价行为；不能以旧上游缺少接口为由删除功能。
6. Java 不添加注释；配置说明采用中英文。临时脚本、解压、反编译和测试日志全部放在 `D:\AI临时文件`。

## 验收门槛

- [x] 目标依赖源码、元数据及方法签名确认。
- [x] 主实现、配方、标签、贴图、本地化与配置完整迁移。
- [x] 编译与构建通过。
- [x] 逐项复核是否存在实现简化，发现后补齐。
- [x] 代码完整后，交由子代理完成服务端功能回归。
- [x] 代码完整后，交由子代理使用 Render Preview 完成客户端与渲染对照。
- [x] 确认最终 JAR 无测试类、无前置模组打包，记录 Git 提交及远程分支。

## 当前状态

代码、资源和文档迁移完成，编译、reobf 构建、子代理服务端集成测试及 Render Preview 固定展示渲染验收均已通过。

## 代码迁移阶段

- 已从源版建立 Git 基线与 `1201forge` 独立工作树。
- 基于本机 1201-forge 模板迁移至 Forge 47.4.23、Java 17；保留 JEI、JEC、Jade、ALI 开发依赖。
- 数据目录恢复为 recipes、advancements、tags/items、structures；配方结果恢复 item 键，pack 格式 15。
- 旧版 Waystones 使用等级收费和 WarpMode；减免注入其统一费用入口，保留距离、维度、牵绳和其他倍率，最终结果钳制到 0。旧上游不存在新版经验点收费 DSL，不增加或伪造该收费系统。
- 继承上游 WarpStoneItem，复用蓄力、动作、受伤打断、粒子；保留源版 128 耐久、附魔光效、耐久显示及成功后消耗。旧版没有 enableDurability/enableCooldowns，附属配置提供对应开关。
- 复用上游选择菜单、屏幕与选择消息；菜单绑定源手和 ItemStack，防止主副手不一致或开菜单后更换物品导致费用错配。
- 玩家持久 NBT 保存独立共享冷却；通过 Balm 同步，登入、重生、换维度再同步；使用 long 毫秒避免最大配置溢出。原石冷却不被写入。
- 三张换色图标原样复制；下界合金防火及锻造完整 NBT 继承保留。
- 主代码 compileJava 已通过；已补目标版本 GameTest，复核与服务端/客户端测试进行中。

## 简化复核与构建核验

- 子代理对照新旧实际上游确认蓄力各阶段粒子（25%、50%、75%、最后一 tick）的种类、数量、位置和速度等价，受伤打断的 Balm 事件链仍有效。
- 已修复复核发现的两处差异：创造模式越过冷却检查、充能音效使用 PORTAL_TRIGGER。冷却仍在成功传送后记入，创造模式耐久沿用原版豁免；等级收费遵循目标上游原生计算。
- 耐久附魔在 1.20.1 通过 EnchantmentCategory.BREAKABLE 和 canBeDepleted 判断，使用目标机制保留，不加入仅 1.21 原版识别的耐久附魔标签。三张纹理逐字节一致；源新版传送石本就恒定附魔闪光，目标保留。
- `compileJava`、带 GameTest 的编译、`build -PwithGameTests` 均成功，生产 JAR 已 reobf；无测试类、测试结构或前置模组类打包，含三配方和三配方解锁进度。
- 首次服务端启动被 JEC 4.6.11 引用纯客户端 SuffixArray 类阻断，实际测试数为 0，即便 Gradle 退出 0 也不记成功。现保留模板四项辅助依赖，专用服务端任务只将它们作为 compileOnly，客户端开发任务继续正常加载；该启动环境问题已解决。

## 服务端验收结果

- 子代理在完整迁移与构建后执行 `gradlew.bat runGameTestServer -PwithGameTests --console plain`，使用 Java 17。
- 第二次已进入 GameTest，但 Forge 的 PlayerList 对原版 GameTestHelper 空网络通道调用 pipeline，导致测试玩家初始化失败。测试改用 ServerPlayer + Connection + Netty EmbeddedChannel，再执行真实 placeNewPlayer；新增英文异常诊断。该测试环境适配不进入生产 JAR。
- 第三次确认实际运行 1 项综合 GameTest，结果 `1 GAME TESTS COMPLETE`、`All 1 required tests passed :)`，Gradle 退出码 0。
- 测试内部覆盖三档界面预测与服务端费用一致、最低 0、0 级真实免费传送不赠送经验、费用不足拒绝、150/60/30 秒及自定义/0/总开关、换档冷却拦截、原石隔离、创造模式冷却豁免、主副手来源锁定、失效来源拒绝、工作台配方、锻造模板和完整 NBT 继承、防火、128 耐久和实际损耗、耐久开关、未激活目标拒绝、三档实际到达目标与扣费。
- 实际运行以 1 个 GameTest 计数，日志不逐条输出内部断言通过信息。
- 完整证据：`D:\AI临时文件\WaystoneHorizons-1201-migration\gametest-run-3.log`。

## 成品与远程

- 仓库：<https://github.com/hp3081925122/WaystoneHorizons>，公开。
- `main` 保存 1.21.1 NeoForge 基线提交 b6efffc；`1201forge` 实现迁移提交 25f6788，已通过系统现有代理推送。
- 成品：`build/libs/forge-1.20.1-WaystoneHorizons-0.0.1.jar`，29,963 字节。
- SHA-256：`1A9833A944BAC1B443B7812DF03AF6E3FB7FE0CE4A86A6EAA7ABC8E89ED87BFB`。
- `gradle.properties` 中中文说明采用标准 Unicode 转义，已解包验证最终 mods.toml 文本为正确 UTF-8，避免 Java Properties 默认编码导致乱码。
- JAR 仅含本附属类与资源，52 个目录/文件条目，三配方、三解锁进度，无测试类、测试结构或前置类。

## 客户端与渲染验收结果

- 子代理在指定 `Render Preview/1.20.1-forge-render-preview` 工作区，通过外部临时 Gradle init 接入最终生产 JAR 与 Waystones 14.1.21、Balm 7.3.44，Forge 47.4.23 / Java 17。未修改预览器或本项目跟踪源码。
- Minecraft 客户端成功加载所有依赖、附属和 Mixin；物品目录含全部三个注册条目。
- 实际使用 Minecraft ItemRenderer 的 FIXED 展示分别渲染三档物品，以及 Waystones 原版 warp_stone 作为参考，四张图片均为 1024×1024。
- 四者轮廓与包围框一致（287,261 到 764,814），前景像素 190668 或 190670；三档只有预期色板变化，未出现缺失贴图、洋红缺失像素、模型解析或材质错误。
- 本次预览为 `server=false, worldAttached=false`，验证范围是固定展示；没有进入真实世界，不能据此声称手持、背包界面及客户端传送菜单交互已实测。
- 截图和诊断 JSON：`D:\AI临时文件\WaystoneHorizons-1201-migration\render-output\20261026-232459-122292\render_{diamond_warp_stone,netherite_warp_stone,nether_star_warp_stone,waystones_reference}.{png,json}`。
- 客户端启动日志：`D:\AI临时文件\WaystoneHorizons-1201-migration\render-preview-client.log`。测试结束后已正常关闭该客户端。
