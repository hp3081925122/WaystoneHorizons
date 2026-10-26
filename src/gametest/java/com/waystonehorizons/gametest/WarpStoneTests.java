package com.waystonehorizons.gametest;

import com.waystonehorizons.AdvancedWarpStoneItem;
import com.waystonehorizons.HorizonConfig;
import com.waystonehorizons.HorizonCooldowns;
import com.waystonehorizons.HorizonItems;
import com.waystonehorizons.HorizonSelectionMenu;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.UUID;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.api.IWaystoneTeleportContext;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.api.WaystoneStyle;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.config.WaystonesConfigData;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.core.WarpMode;
import net.blay09.mods.waystones.core.Waystone;
import net.blay09.mods.waystones.core.WaystoneTeleportContext;
import net.blay09.mods.waystones.core.WaystoneTypes;
import net.blay09.mods.waystones.item.ModItems;
import net.blay09.mods.waystones.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;

@GameTestHolder("waystonehorizons")
@PrefixGameTestTemplate(false)
public final class WarpStoneTests {
    private static final Logger LOGGER = LogUtils.getLogger();

    @GameTest(template = "gametest/empty", timeoutTicks = 200)
    public static void costsCooldownsRecipesAndTeleport(GameTestHelper helper) {
        try {
            runChecks(helper);
        } catch (RuntimeException | Error error) {
            LOGGER.error("Waystone Horizons integration test failed", error);
            throw error;
        }
    }

    private static void runChecks(GameTestHelper helper) {
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), "horizon-test"));
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);
        WaystonesConfigData.XpCost xp = WaystonesConfig.getActive().xpCost;
        double[] savedXp = {xp.minimumBaseXpCost, xp.maximumBaseXpCost, xp.warpStoneXpCostMultiplier, xp.globalWaystoneXpCostMultiplier};
        int savedBlocks = xp.blocksPerXpLevel;
        boolean savedInverse = xp.inverseXpCost;
        int savedDiamond = HorizonConfig.DIAMOND_COOLDOWN.get();
        boolean savedCooldowns = HorizonConfig.ENABLE_COOLDOWNS.get();
        boolean savedDurability = HorizonConfig.ENABLE_DURABILITY.get();
        try {
            helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player);
            player.setGameMode(GameType.SURVIVAL);
            player.getAbilities().instabuild = false;
            player.experienceLevel = 10;
            xp.minimumBaseXpCost = 4;
            xp.maximumBaseXpCost = 4;
            xp.blocksPerXpLevel = 0;
            xp.inverseXpCost = false;
            xp.warpStoneXpCostMultiplier = 1;
            xp.globalWaystoneXpCostMultiplier = 1;
            HorizonConfig.ENABLE_COOLDOWNS.set(true);
            HorizonConfig.ENABLE_DURABILITY.set(true);
            AdvancedWarpStoneItem[] stones = {HorizonItems.DIAMOND_WARP_STONE.get(), HorizonItems.NETHERITE_WARP_STONE.get(), HorizonItems.NETHER_STAR_WARP_STONE.get()};
            int[] cooldowns = {150, 60, 30};
            IWaystone target = new Waystone(WaystoneTypes.WAYSTONE, UUID.randomUUID(), player.level().dimension(), player.blockPosition().offset(100, 0, 0), WaystoneOrigin.PLAYER, player.getUUID());
            for (int tier = 0; tier < stones.length; tier++) {
                ItemStack stone = new ItemStack(stones[tier]);
                player.setItemInHand(InteractionHand.MAIN_HAND, stone);
                helper.assertTrue(stone.is(ModItemTags.WARP_STONES), "Missing warp stone tag for tier " + tier);
                helper.assertTrue(stone.getMaxDamage() == 128, "Durability must remain 128");
                helper.assertTrue(stones[tier].getCooldownSeconds() == cooldowns[tier], "Incorrect cooldown default for tier " + tier);
                helper.assertTrue(PlayerWaystoneManager.predictExperienceLevelCost(player, target, WarpMode.WARP_STONE, null) == 3 - tier, "Incorrect UI level cost for tier " + tier);
                IWaystoneTeleportContext context = context(player, target, stone);
                helper.assertTrue(PlayerWaystoneManager.getExperienceLevelCost(player, target, WarpMode.WARP_STONE, context) == 3 - tier, "Incorrect server level cost for tier " + tier);
                xp.minimumBaseXpCost = 1;
                helper.assertTrue(PlayerWaystoneManager.getExperienceLevelCost(player, target, WarpMode.WARP_STONE, context) == 0, "Experience cost must not become negative");
                xp.minimumBaseXpCost = 0;
                helper.assertTrue(PlayerWaystoneManager.getExperienceLevelCost(player, target, WarpMode.WARP_STONE, context) == 0, "Zero cost must remain zero");
                xp.minimumBaseXpCost = 4;
                HorizonCooldowns.afterTeleport(context);
                long remaining = HorizonCooldowns.getRemaining(player);
                helper.assertTrue(remaining > cooldowns[tier] * 1000L - 5000 && remaining <= cooldowns[tier] * 1000L, "Incorrect persisted cooldown for tier " + tier);
                for (AdvancedWarpStoneItem other : stones) {
                    helper.assertFalse(PlayerWaystoneManager.canUseWarpStone(player, new ItemStack(other)), "Switching tiers must not bypass cooldown");
                }
                helper.assertTrue(PlayerWaystoneManager.canUseWarpStone(player, new ItemStack(ModItems.warpStone)), "Addon cooldown must not block the original stone");
                HorizonCooldowns.setUntil(player, 0);
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.warpStone));
            helper.assertTrue(PlayerWaystoneManager.predictExperienceLevelCost(player, target, WarpMode.WARP_STONE, null) == 4, "Original stone level cost changed");
            ItemStack diamond = new ItemStack(stones[0]);
            player.setItemInHand(InteractionHand.MAIN_HAND, diamond);
            HorizonConfig.DIAMOND_COOLDOWN.set(7);
            HorizonCooldowns.afterTeleport(context(player, target, diamond));
            helper.assertTrue(HorizonCooldowns.getRemaining(player) > 2000 && HorizonCooldowns.getRemaining(player) <= 7000, "Configured cooldown was ignored");
            HorizonCooldowns.setUntil(player, 0);
            HorizonConfig.DIAMOND_COOLDOWN.set(0);
            HorizonCooldowns.afterTeleport(context(player, target, diamond));
            helper.assertTrue(HorizonCooldowns.getRemaining(player) == 0, "Zero cooldown was ignored");
            HorizonCooldowns.setUntil(player, System.currentTimeMillis() + 100000);
            HorizonConfig.ENABLE_COOLDOWNS.set(false);
            helper.assertTrue(PlayerWaystoneManager.canUseWarpStone(player, diamond), "Cooldown toggle was ignored");
            HorizonConfig.ENABLE_COOLDOWNS.set(true);
            player.getAbilities().instabuild = true;
            helper.assertTrue(PlayerWaystoneManager.canUseWarpStone(player, diamond), "Creative players must bypass active cooldowns");
            player.getAbilities().instabuild = false;
            HorizonCooldowns.setUntil(player, 0);
            HorizonConfig.DIAMOND_COOLDOWN.set(savedDiamond);
            checkMenuBinding(helper, player, target);
            checkRecipes(helper, player);
            checkActualTeleport(helper, player, stones, cooldowns);
            helper.succeed();
        } finally {
            HorizonConfig.DIAMOND_COOLDOWN.set(savedDiamond);
            HorizonConfig.ENABLE_COOLDOWNS.set(savedCooldowns);
            HorizonConfig.ENABLE_DURABILITY.set(savedDurability);
            xp.minimumBaseXpCost = savedXp[0];
            xp.maximumBaseXpCost = savedXp[1];
            xp.warpStoneXpCostMultiplier = savedXp[2];
            xp.globalWaystoneXpCostMultiplier = savedXp[3];
            xp.blocksPerXpLevel = savedBlocks;
            xp.inverseXpCost = savedInverse;
            player.containerMenu = player.inventoryMenu;
            helper.getLevel().getServer().getPlayerList().remove(player);
            player.discard();
            channel.finishAndReleaseAll();
        }
    }

    private static IWaystoneTeleportContext context(Player player, IWaystone target, ItemStack stack) {
        WaystoneTeleportContext context = new WaystoneTeleportContext(player, target, null);
        context.setWarpMode(WarpMode.WARP_STONE);
        context.setWarpItem(stack);
        return context;
    }

    private static void checkMenuBinding(GameTestHelper helper, ServerPlayer player, IWaystone target) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(HorizonItems.DIAMOND_WARP_STONE.get()));
        ItemStack star = new ItemStack(HorizonItems.NETHER_STAR_WARP_STONE.get());
        player.setItemInHand(InteractionHand.OFF_HAND, star);
        HorizonSelectionMenu menu = new HorizonSelectionMenu(12, player, star, InteractionHand.OFF_HAND);
        player.containerMenu = menu;
        helper.assertTrue(PlayerWaystoneManager.findWarpItem(player, WarpMode.WARP_STONE) == star, "Offhand source was replaced by main hand");
        helper.assertTrue(PlayerWaystoneManager.predictExperienceLevelCost(player, target, WarpMode.WARP_STONE, null) == 1, "Menu cost must follow the selected offhand stone");
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(HorizonItems.DIAMOND_WARP_STONE.get()));
        helper.assertFalse(menu.stillValid(player), "Changing source stone must invalidate the menu");
        helper.assertTrue(PlayerWaystoneManager.findWarpItem(player, WarpMode.WARP_STONE).isEmpty(), "Invalid source must not fall back to another hand");
        player.containerMenu = player.inventoryMenu;
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    private static void checkRecipes(GameTestHelper helper, ServerPlayer player) {
        var level = helper.getLevel();
        for (var center : List.of(Items.DIAMOND, Items.NETHER_STAR)) {
            TransientCraftingContainer input = new TransientCraftingContainer(player.inventoryMenu, 3, 3);
            for (int slot = 0; slot < 9; slot++) {
                input.setItem(slot, new ItemStack(slot == 4 ? center : slot % 2 == 0 ? Items.AMETHYST_SHARD : Items.ENDER_PEARL));
            }
            ItemStack output = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow().assemble(input, level.registryAccess());
            helper.assertTrue(output.is(center == Items.DIAMOND ? HorizonItems.DIAMOND_WARP_STONE.get() : HorizonItems.NETHER_STAR_WARP_STONE.get()), "Incorrect shaped recipe");
        }
        ItemStack base = new ItemStack(HorizonItems.DIAMOND_WARP_STONE.get());
        base.setDamageValue(12);
        base.setHoverName(Component.literal("Upgrade test"));
        base.enchant(Enchantments.UNBREAKING, 2);
        base.getOrCreateTag().putString("migration_test", "preserved");
        SimpleContainer input = new SimpleContainer(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), base, new ItemStack(Items.NETHERITE_INGOT));
        ItemStack output = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level).orElseThrow().assemble(input, level.registryAccess());
        helper.assertTrue(output.is(HorizonItems.NETHERITE_WARP_STONE.get()), "Incorrect smithing output");
        helper.assertTrue(base.getTag().equals(output.getTag()), "Smithing must preserve the complete base NBT including damage, name and enchantments");
        helper.assertTrue(output.getItem().isFireResistant(), "Netherite stone must be fire resistant");
        input.setItem(0, ItemStack.EMPTY);
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level).isEmpty(), "Smithing template is required");
    }

    private static void checkActualTeleport(GameTestHelper helper, ServerPlayer player, AdvancedWarpStoneItem[] stones, int[] cooldowns) {
        BlockPos targetPos = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), 3);
        IWaystone target = WaystonesAPI.placeWaystone(helper.getLevel(), targetPos, new WaystoneStyle(new ResourceLocation("waystones", "waystone"))).orElseThrow();
        for (int tier = 0; tier < stones.length; tier++) {
            HorizonCooldowns.setUntil(player, 0);
            ItemStack stone = new ItemStack(stones[tier]);
            player.setItemInHand(InteractionHand.MAIN_HAND, stone);
            player.experienceLevel = 10;
            player.teleportTo(targetPos.getX() + 10, targetPos.getY(), targetPos.getZ() + 10);
            if (tier == 0) {
                IWaystoneTeleportContext denied = WaystonesAPI.createDefaultTeleportContext(player, target, WarpMode.WARP_STONE, null).left().orElseThrow();
                helper.assertTrue(WaystonesAPI.tryTeleport(denied).right().isPresent(), "Unactivated waystone must remain unavailable");
                helper.assertTrue(HorizonCooldowns.getRemaining(player) == 0 && stone.getDamageValue() == 0, "Rejected teleport must not start cooldown or consume durability");
                PlayerWaystoneManager.activateWaystone(player, target);
                player.experienceLevel = 0;
                IWaystoneTeleportContext unaffordable = WaystonesAPI.createDefaultTeleportContext(player, target, WarpMode.WARP_STONE, null).left().orElseThrow();
                helper.assertTrue(WaystonesAPI.tryTeleport(unaffordable).right().isPresent(), "Insufficient experience must still reject teleport");
                helper.assertTrue(HorizonCooldowns.getRemaining(player) == 0 && stone.getDamageValue() == 0, "Unaffordable teleport must not charge cooldown or durability");
                player.experienceLevel = 10;
            }
            IWaystoneTeleportContext context = WaystonesAPI.createDefaultTeleportContext(player, target, WarpMode.WARP_STONE, null).left().orElseThrow();
            helper.assertTrue(context.getXpCost() == 3 - tier, "Actual teleport cost disagrees with the UI");
            var result = WaystonesAPI.tryTeleport(context);
            helper.assertTrue(result.left().isPresent(), "Actual teleport failed for tier " + tier + ": " + result.right());
            helper.assertTrue(player.distanceToSqr(targetPos.getCenter()) < 25, "Player did not reach the target");
            helper.assertTrue(player.experienceLevel == 7 + tier, "Actual reduced experience charge is wrong");
            helper.assertTrue(stone.getDamageValue() == 1, "Successful teleport must consume one durability");
            helper.assertTrue(HorizonCooldowns.getRemaining(player) > cooldowns[tier] * 1000L - 5000, "Successful teleport must start its configured cooldown");
            helper.assertTrue(PlayerWaystoneManager.getWarpStoneCooldownLeft(player) <= 0, "Addon teleport must not start the original cooldown");
            helper.assertTrue(WaystonesAPI.tryTeleport(context).right().isPresent(), "Server must reject repeat teleport during cooldown");
        }
        HorizonCooldowns.setUntil(player, 0);
        HorizonConfig.ENABLE_DURABILITY.set(false);
        HorizonConfig.DIAMOND_COOLDOWN.set(0);
        WaystonesConfig.getActive().xpCost.minimumBaseXpCost = 0;
        ItemStack freeStone = new ItemStack(stones[0]);
        player.setItemInHand(InteractionHand.MAIN_HAND, freeStone);
        player.experienceLevel = 0;
        IWaystoneTeleportContext free = WaystonesAPI.createDefaultTeleportContext(player, target, WarpMode.WARP_STONE, null).left().orElseThrow();
        helper.assertTrue(free.getXpCost() == 0 && WaystonesAPI.tryTeleport(free).left().isPresent(), "Zero experience must permit a free teleport");
        helper.assertTrue(player.experienceLevel == 0, "Free teleport must not grant experience");
        helper.assertTrue(freeStone.getDamageValue() == 0, "Disabled durability must not damage the stone");
        helper.assertTrue(HorizonCooldowns.getRemaining(player) == 0, "Zero cooldown must remain zero after an actual teleport");
    }
}
