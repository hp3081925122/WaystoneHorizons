package com.waystonehorizons.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.waystonehorizons.AdvancedWarpStoneItem;
import com.waystonehorizons.HorizonConfig;
import com.waystonehorizons.HorizonCooldowns;
import com.waystonehorizons.HorizonItems;
import com.waystonehorizons.WaystoneHorizons;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.blay09.mods.shogi.common.CachedShogiRule;
import net.blay09.mods.shogi.common.effect.cost.ExperienceLevelCostInformation;
import net.blay09.mods.shogi.common.effect.cost.ExperiencePointsCostInformation;
import net.blay09.mods.shogi.common.effect.server.cooldown.CooldownInformation;
import net.blay09.mods.waystones.api.WarpStoneTypes;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneKinds;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.config.WaystonesRules;
import net.blay09.mods.waystones.core.WaystoneImpl;
import net.blay09.mods.waystones.core.WaystoneTeleportContextImpl;
import net.blay09.mods.waystones.item.ModItems;
import net.blay09.mods.waystones.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.BuiltinTestFunctions;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = WaystoneHorizons.MOD_ID)
public final class WarpStoneTests {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("integration"));
        var data = new TestData<>(environment, id("gametest/empty"), 400, 0, true);
        event.registerTest(id("costs_cooldowns_recipes_teleport"), new FunctionGameTestInstance(BuiltinTestFunctions.ALWAYS_PASS, data) {
            @Override
            public void run(GameTestHelper helper) {
                try {
                    runChecks(helper);
                } catch (Throwable error) {
                    LOGGER.error("Waystone Horizons 26.1.2 integration test failed", error);
                    throw new RuntimeException(error);
                }
            }
        });
    }

    private static void runChecks(GameTestHelper helper) throws ReflectiveOperationException {
        var level = helper.getLevel();
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "horizon-test"), false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);
        var config = WaystonesConfig.getActive().rules;
        var savedRules = config.warpRequirements;
        var savedSettings = config.warpSettings;
        boolean savedCooldowns = config.enableCooldowns;
        boolean savedDurability = config.enableDurability;
        int savedDiamond = HorizonConfig.DIAMOND_COOLDOWN.get();
        try {
            level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
            player.setGameMode(GameType.SURVIVAL);
            player.getAbilities().instabuild = false;
            player.experienceLevel = 10;
            player.experienceProgress = 0;
            config.enableCooldowns = true;
            config.enableDurability = true;
            config.warpSettings = List.of();
            Waystone target = new WaystoneImpl(WaystoneKinds.WAYSTONE, UUID.randomUUID(), level.dimension(), player.blockPosition().offset(100, 0, 0), WaystoneOrigin.PLAYER, player.getUUID());
            AdvancedWarpStoneItem[] stones = {HorizonItems.DIAMOND_WARP_STONE.get(), HorizonItems.NETHERITE_WARP_STONE.get(), HorizonItems.NETHER_STAR_WARP_STONE.get()};
            int[] seconds = {150, 60, 30};
            int[] pointCosts = {75, 52, 31};
            for (int tier = 0; tier < stones.length; tier++) {
                ItemStack stone = new ItemStack(stones[tier]);
                player.setItemInHand(InteractionHand.MAIN_HAND, stone);
                helper.assertTrue(stone.getMaxDamage() == 128 && stone.is(ModItemTags.WARP_STONES), "Durability or warp stone tag missing");
                helper.assertTrue(stones[tier].getCooldownSeconds() == seconds[tier], "Default cooldown changed");
                configure(List.of("xp_level_cost(4)"));
                var requirements = context(player, target, stone);
                helper.assertTrue(levelCost(requirements) == 3 - tier, "Incorrect level cost for tier " + tier);
                CooldownInformation cooldown = information(requirements).stream().filter(CooldownInformation.class::isInstance).map(CooldownInformation.class::cast).findFirst().orElseThrow();
                helper.assertTrue(cooldown.identifier().equals(HorizonCooldowns.COOLDOWN_KEY) && cooldown.requestedTicks() == seconds[tier] * 20, "Incorrect shared cooldown information");
                helper.assertTrue(player.experienceLevel == 10 && stone.getDamageValue() == 0 && HorizonCooldowns.getRemainingMillis(player) == 0, "Requirements simulation must not consume anything");
                configure(List.of("xp_points_cost(100)"));
                helper.assertTrue(pointCost(context(player, target, stone)) == pointCosts[tier], "Incorrect point discount for tier " + tier);
                configure(List.of("xp_level_cost(1)", "xp_points_cost(0)"));
                helper.assertTrue(levelCost(context(player, target, stone)) == 0, "Level fee must clamp to zero");
                configure(List.of("xp_points_cost(1)"));
                helper.assertTrue(pointCost(context(player, target, stone)) == 0, "Point fee must clamp to zero");
            }
            ItemStack netherite = new ItemStack(stones[1]);
            configure(List.of("xp_level_cost(4)", "xp_points_cost(100)"));
            var mixed = context(player, target, netherite);
            helper.assertTrue(levelCost(mixed) == 2 && pointCost(mixed) == 100, "Mixed fees must not receive the discount twice");
            configure(List.of("xp_points_cost(100)", "xp_level_cost(4)"));
            mixed = context(player, target, netherite);
            helper.assertTrue(levelCost(mixed) == 2 && pointCost(mixed) == 100, "Level discount priority must not depend on rule order");
            configure(List.of("xp_level_cost(1)", "xp_level_cost(1)", "xp_points_cost(100)"));
            mixed = context(player, target, new ItemStack(stones[2]));
            helper.assertTrue(levelCost(mixed) == 0 && pointCost(mixed) == 75, "Multiple fees must share a single discount budget");
            configure(List.of("xp_level_cost(4)"));
            var original = context(player, target, new ItemStack((net.minecraft.world.level.ItemLike) ModItems.warpStones.get(WarpStoneTypes.UNSCOPED)));
            helper.assertTrue(levelCost(original) == 4 && information(original).stream().noneMatch(CooldownInformation.class::isInstance), "Original stone must remain unchanged");
            configure(List.of("failure('blocked')"));
            helper.assertTrue(context(player, target, new ItemStack(stones[0])).getRequirements().right().isPresent(), "Other restrictions must remain effective");
            configure(List.of("xp_level_cost(4)"));
            HorizonConfig.DIAMOND_COOLDOWN.set(7);
            helper.assertTrue(information(context(player, target, new ItemStack(stones[0]))).stream().filter(CooldownInformation.class::isInstance).map(CooldownInformation.class::cast).findFirst().orElseThrow().requestedTicks() == 140, "Configured cooldown ignored");
            HorizonConfig.DIAMOND_COOLDOWN.set(0);
            helper.assertTrue(information(context(player, target, new ItemStack(stones[0]))).stream().noneMatch(CooldownInformation.class::isInstance), "Zero cooldown ignored");
            HorizonConfig.DIAMOND_COOLDOWN.set(Integer.MAX_VALUE);
            HorizonCooldowns.setUntil(player, System.currentTimeMillis() + Integer.MAX_VALUE * 1000L);
            helper.assertTrue(HorizonCooldowns.getRemainingMillis(player) > Integer.MAX_VALUE * 999L, "Large cooldown overflowed");
            helper.assertTrue(context(player, target, new ItemStack(stones[0])).getRequirements().right().isPresent(), "Active cooldown must reject warp");
            config.enableCooldowns = false;
            helper.assertTrue(context(player, target, new ItemStack(stones[0])).getRequirements().left().isPresent(), "Global cooldown switch ignored");
            config.enableCooldowns = true;
            player.getAbilities().instabuild = true;
            helper.assertTrue(context(player, target, new ItemStack(stones[0])).getRequirements().left().isPresent(), "Creative cooldown exemption was lost");
            player.getAbilities().instabuild = false;
            HorizonCooldowns.setUntil(player, 0);
            HorizonConfig.DIAMOND_COOLDOWN.set(savedDiamond);
            checkRecipes(helper);
            checkActualTeleport(helper, player, stones, seconds);
            helper.succeed();
        } finally {
            HorizonConfig.DIAMOND_COOLDOWN.set(savedDiamond);
            config.warpSettings = savedSettings;
            configure(savedRules);
            config.enableCooldowns = savedCooldowns;
            config.enableDurability = savedDurability;
            level.getServer().getPlayerList().remove(player);
            player.discard();
            channel.finishAndReleaseAll();
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(WaystoneHorizons.MOD_ID, path);
    }

    private static void configure(List<String> rules) throws ReflectiveOperationException {
        WaystonesConfig.getActive().rules.warpRequirements = rules;
        var field = WaystonesRules.class.getDeclaredField("cachedWarpRequirements");
        field.setAccessible(true);
        ((CachedShogiRule) field.get(null)).invalidate();
    }

    private static WaystoneTeleportContext context(Player player, Waystone target, ItemStack stack) {
        return new WaystoneTeleportContextImpl(player, target).setWarpItem(stack).setWarpHand(InteractionHand.MAIN_HAND);
    }

    private static List<Object> information(WaystoneTeleportContext context) {
        return context.getRequirements().map(List::copyOf, List::copyOf);
    }

    private static int levelCost(WaystoneTeleportContext context) {
        return information(context).stream().filter(ExperienceLevelCostInformation.class::isInstance).map(ExperienceLevelCostInformation.class::cast).mapToInt(ExperienceLevelCostInformation::required).sum();
    }

    private static int pointCost(WaystoneTeleportContext context) {
        return information(context).stream().filter(ExperiencePointsCostInformation.class::isInstance).map(ExperiencePointsCostInformation.class::cast).mapToInt(ExperiencePointsCostInformation::required).sum();
    }

    private static void checkRecipes(GameTestHelper helper) {
        var level = helper.getLevel();
        var recipes = level.getServer().getRecipeManager();
        for (var center : List.of(Items.DIAMOND, Items.NETHER_STAR)) {
            List<ItemStack> grid = new ArrayList<>();
            for (int slot = 0; slot < 9; slot++) {
                grid.add(new ItemStack(slot == 4 ? center : slot % 2 == 0 ? Items.AMETHYST_SHARD : Items.ENDER_PEARL));
            }
            CraftingInput input = CraftingInput.of(3, 3, grid);
            ItemStack output = recipes.getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow().value().assemble(input);
            helper.assertTrue(output.is(center == Items.DIAMOND ? HorizonItems.DIAMOND_WARP_STONE.get() : HorizonItems.NETHER_STAR_WARP_STONE.get()), "Incorrect shaped recipe");
        }
        ItemStack base = new ItemStack(HorizonItems.DIAMOND_WARP_STONE.get());
        base.setDamageValue(12);
        base.set(DataComponents.CUSTOM_NAME, Component.literal("Upgrade test"));
        var enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING), 2);
        base.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        SmithingRecipeInput input = new SmithingRecipeInput(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), base, new ItemStack(Items.NETHERITE_INGOT));
        ItemStack output = recipes.getRecipeFor(RecipeType.SMITHING, input, level).orElseThrow().value().assemble(input);
        helper.assertTrue(output.is(HorizonItems.NETHERITE_WARP_STONE.get()), "Incorrect smithing result");
        helper.assertTrue(base.getComponentsPatch().equals(output.getComponentsPatch()), "Smithing must preserve complete base components");
        helper.assertTrue(output.has(DataComponents.DAMAGE_RESISTANT), "Netherite stone must be fire resistant");
        SmithingRecipeInput missingTemplate = new SmithingRecipeInput(ItemStack.EMPTY, base, new ItemStack(Items.NETHERITE_INGOT));
        helper.assertTrue(recipes.getRecipeFor(RecipeType.SMITHING, missingTemplate, level).isEmpty(), "Smithing template is required");
    }

    private static void checkActualTeleport(GameTestHelper helper, ServerPlayer player, AdvancedWarpStoneItem[] stones, int[] seconds) throws ReflectiveOperationException {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
        Waystone target = WaystonesAPI.placeWaystone(helper.getLevel(), pos, WaystoneTypes.ANDESITE).orElseThrow();
        WaystonesAPI.activateWaystone(player, target);
        configure(List.of("xp_level_cost(4)", "damage_item(80)"));
        for (int tier = 0; tier < stones.length; tier++) {
            HorizonCooldowns.setUntil(player, 0);
            player.experienceLevel = 10;
            ItemStack stack = new ItemStack(stones[tier]);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            player.teleportTo(pos.getX() + 10, pos.getY(), pos.getZ() + 10);
            var context = WaystonesAPI.createUncheckedDefaultTeleportContext(player, target, value -> value.setWarpItem(stack).setWarpHand(InteractionHand.MAIN_HAND)).left().orElseThrow();
            helper.assertTrue(levelCost(context) == 3 - tier, "Actual fee disagrees with menu information");
            helper.assertTrue(stack.getDamageValue() == 0 && player.experienceLevel == 10, "Menu simulation caused side effects");
            var result = WaystonesAPI.tryTeleport(context);
            helper.assertTrue(result.left().isPresent(), "Actual teleport failed: " + result.right());
            helper.assertTrue(player.distanceToSqr(pos.getCenter()) < 25, "Player did not reach the target");
            helper.assertTrue(player.experienceLevel == 7 + tier, "Incorrect actual level charge");
            helper.assertTrue(stack.getDamageValue() == 1, "Must consume one durability instead of the upstream default 80");
            helper.assertTrue(HorizonCooldowns.getRemainingMillis(player) > seconds[tier] * 1000L - 5000, "Successful teleport did not start cooldown");
            var blocked = WaystonesAPI.createUncheckedDefaultTeleportContext(player, target, value -> value.setWarpItem(stack).setWarpHand(InteractionHand.MAIN_HAND)).left().orElseThrow();
            helper.assertTrue(WaystonesAPI.tryTeleport(blocked).right().isPresent(), "Shared cooldown must block repeated server teleport");
        }
        HorizonCooldowns.setUntil(player, 0);
        configure(List.of("xp_level_cost(0)", "xp_points_cost(0)", "damage_item(80)"));
        WaystonesConfig.getActive().rules.enableDurability = false;
        HorizonConfig.DIAMOND_COOLDOWN.set(0);
        player.experienceLevel = 0;
        ItemStack stack = new ItemStack(stones[0]);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var free = WaystonesAPI.createUncheckedDefaultTeleportContext(player, target, value -> value.setWarpItem(stack).setWarpHand(InteractionHand.MAIN_HAND)).left().orElseThrow();
        helper.assertTrue(WaystonesAPI.tryTeleport(free).left().isPresent(), "Free teleport was rejected");
        helper.assertTrue(player.experienceLevel == 0 && stack.getDamageValue() == 0 && HorizonCooldowns.getRemainingMillis(player) == 0, "Free teleport or disabled durability produced side effects");
    }
}
