package com.waystonehorizons.gametest;

import com.waystonehorizons.AdvancedWarpStoneItem;
import com.waystonehorizons.HorizonConfig;
import com.waystonehorizons.HorizonItems;
import com.waystonehorizons.WarpStoneRequirements;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.api.WaystoneStyle;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.core.WaystoneImpl;
import net.blay09.mods.waystones.core.WaystoneTeleportContextImpl;
import net.blay09.mods.waystones.item.ModItems;
import net.blay09.mods.waystones.requirement.CombinedRequirement;
import net.blay09.mods.waystones.requirement.CooldownRequirement;
import net.blay09.mods.waystones.requirement.ExperienceLevelRequirement;
import net.blay09.mods.waystones.requirement.ExperiencePointsRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("waystonehorizons")
@PrefixGameTestTemplate(false)
public final class WarpStoneTests {
    @GameTest(template = "gametest/empty", timeoutTicks = 200)
    public static void costsCooldownsRecipesAndTeleport(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.experienceLevel = 10;
        player.experienceProgress = 0;
        player.totalExperience = 160;
        List<String> savedRules = WaystonesConfig.getActive().teleports.warpRequirements;
        boolean savedCooldowns = WaystonesConfig.getActive().teleports.enableCooldowns;
        int savedDiamondCooldown = HorizonConfig.DIAMOND_COOLDOWN.get();
        try {
            WaystonesConfig.getActive().teleports.enableCooldowns = true;
            AdvancedWarpStoneItem[] stones = {HorizonItems.DIAMOND_WARP_STONE.get(), HorizonItems.NETHERITE_WARP_STONE.get(), HorizonItems.NETHER_STAR_WARP_STONE.get()};
            int[] cooldowns = {150, 60, 30};
            int[] pointCosts = {75, 52, 31};
            for (int tier = 0; tier < stones.length; tier++) {
                WaystoneTeleportContext context = context(player, new ItemStack(stones[tier]));
                WaystonesConfig.getActive().teleports.warpRequirements = List.of("add_level_cost(4)");
                WarpRequirement resolved = WaystonesAPI.resolveRequirements(context);
                helper.assertTrue(levelCost(resolved) == 3 - tier, "Incorrect level discount for tier " + tier);
                helper.assertTrue(cooldown(resolved).getCooldownSeconds() == cooldowns[tier], "Incorrect default cooldown for tier " + tier);
                helper.assertTrue(cooldown(resolved).getCooldownKey().equals(WarpStoneRequirements.COOLDOWN_KEY), "Cooldown must be shared between tiers");
                WaystonesConfig.getActive().teleports.warpRequirements = List.of("add_xp_cost(100)");
                helper.assertTrue(pointCost(WaystonesAPI.resolveRequirements(context)) == pointCosts[tier], "Incorrect point discount for tier " + tier);
                WaystonesConfig.getActive().teleports.warpRequirements = List.of("add_level_cost(0)", "add_xp_cost(0)");
                WarpRequirement free = WaystonesAPI.resolveRequirements(context);
                helper.assertTrue(levelCost(free) == 0 && pointCost(free) == 0, "Zero cost must remain zero");
                WaystonesConfig.getActive().teleports.warpRequirements = List.of("add_level_cost(1)");
                WarpRequirement clamped = WaystonesAPI.resolveRequirements(context);
                helper.assertTrue(levelCost(clamped) == 0, "Level cost must never be negative");
                flat(clamped).stream().filter(ExperienceLevelRequirement.class::isInstance).forEach(cost -> cost.consume(player));
                helper.assertTrue(player.experienceLevel == 10, "Free teleport must not grant levels");
                WaystonesConfig.getActive().teleports.warpRequirements = List.of("add_xp_cost(1)");
                helper.assertTrue(pointCost(WaystonesAPI.resolveRequirements(context)) == 0, "Point cost must never be negative");
                helper.assertTrue(new ItemStack(stones[tier]).is(net.blay09.mods.waystones.tag.ModItemTags.WARP_STONES), "Missing warp stone tag");
            }

            WaystonesConfig.getActive().teleports.warpRequirements = List.of("add_level_cost(4)", "add_xp_cost(100)");
            WarpRequirement mixed = WaystonesAPI.resolveRequirements(context(player, new ItemStack(stones[1])));
            helper.assertTrue(levelCost(mixed) == 2 && pointCost(mixed) == 100, "Mixed costs must not receive the discount twice");
            WaystonesConfig.getActive().teleports.warpRequirements = List.of("add_level_cost(4)");
            WarpRequirement vanilla = WaystonesAPI.resolveRequirements(context(player, new ItemStack(ModItems.warpStone)));
            helper.assertTrue(levelCost(vanilla) == 4 && flat(vanilla).stream().noneMatch(CooldownRequirement.class::isInstance), "Vanilla stone must be unchanged");
            WaystoneTeleportContext diamondContext = context(player, new ItemStack(stones[0]));
            HorizonConfig.DIAMOND_COOLDOWN.set(7);
            helper.assertTrue(cooldown(WaystonesAPI.resolveRequirements(diamondContext)).getCooldownSeconds() == 7, "Configured cooldown not applied");
            HorizonConfig.DIAMOND_COOLDOWN.set(0);
            helper.assertTrue(flat(WaystonesAPI.resolveRequirements(diamondContext)).stream().noneMatch(CooldownRequirement.class::isInstance), "Zero must disable addon cooldown");
            HorizonConfig.DIAMOND_COOLDOWN.set(savedDiamondCooldown);
            WaystonesConfig.getActive().teleports.enableCooldowns = false;
            helper.assertTrue(flat(WaystonesAPI.resolveRequirements(diamondContext)).stream().noneMatch(CooldownRequirement.class::isInstance), "Upstream cooldown toggle must be respected");
            WaystonesConfig.getActive().teleports.enableCooldowns = true;
            checkRecipes(helper);
            checkCooldownAndTeleport(helper);
            helper.succeed();
        } finally {
            HorizonConfig.DIAMOND_COOLDOWN.set(savedDiamondCooldown);
            WaystonesConfig.getActive().teleports.warpRequirements = savedRules;
            WaystonesConfig.getActive().teleports.enableCooldowns = savedCooldowns;
        }
    }

    private static WaystoneTeleportContext context(Player player, ItemStack stack) {
        Waystone target = new WaystoneImpl(WaystoneTypes.WAYSTONE, UUID.randomUUID(), player.level().dimension(), player.blockPosition().offset(100, 0, 0), WaystoneOrigin.PLAYER, player.getUUID());
        return new WaystoneTeleportContextImpl(player, target).setWarpItem(stack);
    }

    private static List<WarpRequirement> flat(WarpRequirement requirement) {
        List<WarpRequirement> result = new ArrayList<>();
        if (requirement instanceof CombinedRequirement combined) {
            combined.getRequirements().forEach(child -> result.addAll(flat(child)));
        } else {
            result.add(requirement);
        }
        return result;
    }

    private static int levelCost(WarpRequirement requirement) {
        return flat(requirement).stream().filter(ExperienceLevelRequirement.class::isInstance).map(ExperienceLevelRequirement.class::cast).mapToInt(ExperienceLevelRequirement::getLevels).sum();
    }

    private static int pointCost(WarpRequirement requirement) {
        return flat(requirement).stream().filter(ExperiencePointsRequirement.class::isInstance).map(ExperiencePointsRequirement.class::cast).mapToInt(ExperiencePointsRequirement::getPoints).sum();
    }

    private static CooldownRequirement cooldown(WarpRequirement requirement) {
        return flat(requirement).stream().filter(CooldownRequirement.class::isInstance).map(CooldownRequirement.class::cast).filter(value -> value.getCooldownKey().equals(WarpStoneRequirements.COOLDOWN_KEY)).findFirst().orElseThrow();
    }

    private static void checkRecipes(GameTestHelper helper) {
        var level = helper.getLevel();
        for (var center : List.of(Items.DIAMOND, Items.NETHER_STAR)) {
            List<ItemStack> grid = new ArrayList<>();
            for (int slot = 0; slot < 9; slot++) {
                grid.add(new ItemStack(slot == 4 ? center : slot % 2 == 0 ? Items.AMETHYST_SHARD : Items.ENDER_PEARL));
            }
            CraftingInput input = CraftingInput.of(3, 3, grid);
            ItemStack output = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow().value().assemble(input, level.registryAccess());
            helper.assertTrue(output.is(center == Items.DIAMOND ? HorizonItems.DIAMOND_WARP_STONE.get() : HorizonItems.NETHER_STAR_WARP_STONE.get()), "Incorrect shaped recipe");
        }
        ItemStack base = new ItemStack(HorizonItems.DIAMOND_WARP_STONE.get());
        base.setDamageValue(12);
        base.set(DataComponents.CUSTOM_NAME, Component.literal("Upgrade test"));
        SmithingRecipeInput input = new SmithingRecipeInput(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), base, new ItemStack(Items.NETHERITE_INGOT));
        ItemStack output = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level).orElseThrow().value().assemble(input, level.registryAccess());
        helper.assertTrue(output.is(HorizonItems.NETHERITE_WARP_STONE.get()), "Smithing output mismatch");
        helper.assertTrue(output.getDamageValue() == 12 && Component.literal("Upgrade test").equals(output.get(DataComponents.CUSTOM_NAME)), "Smithing must preserve components");
        SmithingRecipeInput wrong = new SmithingRecipeInput(ItemStack.EMPTY, base, new ItemStack(Items.NETHERITE_INGOT));
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, wrong, level).isEmpty(), "Smithing must require its template");
    }

    private static void checkCooldownAndTeleport(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.setGameMode(GameType.SURVIVAL);
            player.getAbilities().instabuild = false;
            player.experienceLevel = 10;
            player.experienceProgress = 0;
            player.totalExperience = 160;
            ItemStack stone = new ItemStack(HorizonItems.DIAMOND_WARP_STONE.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, stone);
            CooldownRequirement requirement = cooldown(WaystonesAPI.resolveRequirements(context(player, stone)));
            requirement.consume(player);
            long remaining = requirement.getCooldownMillisLeft(player);
            helper.assertTrue(remaining > 145000 && remaining <= 150000, "Cooldown persistence mismatch");
            helper.assertFalse(requirement.canAfford(player), "Active cooldown must block teleport");
            WarpRequirement star = WaystonesAPI.resolveRequirements(context(player, new ItemStack(HorizonItems.NETHER_STAR_WARP_STONE.get())));
            helper.assertFalse(star.canAfford(player), "Switching tiers must not bypass cooldown");
            PlayerWaystoneManager.setCooldownUntil(player, WarpStoneRequirements.COOLDOWN_KEY, System.currentTimeMillis() - 1);
            helper.assertTrue(requirement.canAfford(player), "Expired cooldown must allow teleport");

            BlockPos targetPos = helper.absolutePos(new BlockPos(2, 1, 2));
            helper.getLevel().setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), 3);
            Waystone target = WaystonesAPI.placeWaystone(helper.getLevel(), targetPos, new WaystoneStyle(ResourceLocation.fromNamespaceAndPath("waystones", "waystone"))).orElseThrow();
            WaystonesAPI.activateWaystone(player, target);
            player.teleportTo(targetPos.getX() + 10, targetPos.getY(), targetPos.getZ() + 10);
            WaystoneTeleportContext context = WaystonesAPI.createUncheckedDefaultTeleportContext(player, target, value -> value.setWarpItem(stone).setWarpHand(InteractionHand.MAIN_HAND)).left().orElseThrow();
            var result = WaystonesAPI.tryTeleport(context);
            helper.assertTrue(result.left().isPresent(), "Actual waypoint teleport failed: " + result.right());
            helper.assertTrue(player.distanceToSqr(targetPos.getCenter()) < 25, "Player did not reach the target");
            helper.assertTrue(player.experienceLevel == 7, "Actual teleport must consume the reduced three levels");
            helper.assertTrue(PlayerWaystoneManager.getCooldownMillisLeft(player, WarpStoneRequirements.COOLDOWN_KEY) > 145000, "Actual teleport must start cooldown");
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
            player.discard();
        }
    }
}
