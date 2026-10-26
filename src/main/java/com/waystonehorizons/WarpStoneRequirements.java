package com.waystonehorizons;

import java.util.ArrayList;
import java.util.List;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.requirement.CombinedRequirement;
import net.blay09.mods.waystones.requirement.CooldownRequirement;
import net.blay09.mods.waystones.requirement.ExperienceLevelRequirement;
import net.blay09.mods.waystones.requirement.ExperiencePointsRequirement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class WarpStoneRequirements {
    public static final ResourceLocation COOLDOWN_KEY = ResourceLocation.fromNamespaceAndPath(WaystoneHorizons.MOD_ID, "warp_stone");

    private WarpStoneRequirements() {
    }

    public static WarpRequirement adjust(WaystoneTeleportContext context, WarpRequirement original) {
        if (!(context.getWarpItem().getItem() instanceof AdvancedWarpStoneItem stone)
                || !(context.getEntity() instanceof Player player)) {
            return original;
        }

        List<WarpRequirement> requirements = new ArrayList<>();
        flatten(original, requirements);
        int remainingDiscount = stone.getLevelDiscount();
        for (int index = 0; index < requirements.size(); index++) {
            if (requirements.get(index) instanceof ExperienceLevelRequirement levels) {
                int originalLevels = Math.max(0, levels.getLevels());
                requirements.set(index, new ExperienceLevelRequirement(Math.max(0, originalLevels - remainingDiscount)));
                remainingDiscount = Math.max(0, remainingDiscount - originalLevels);
            }
        }

        long pointsDiscount = experienceValue(player, remainingDiscount);
        for (int index = 0; index < requirements.size(); index++) {
            if (requirements.get(index) instanceof ExperiencePointsRequirement points) {
                int originalPoints = Math.max(0, points.getPoints());
                requirements.set(index, new ExperiencePointsRequirement((int) Math.max(0L, originalPoints - pointsDiscount)));
                pointsDiscount = Math.max(0L, pointsDiscount - originalPoints);
            }
        }

        int cooldown = stone.getCooldownSeconds();
        if (cooldown > 0 && WaystonesConfig.getActive().teleports.enableCooldowns) {
            requirements.add(new CooldownRequirement(COOLDOWN_KEY, cooldown));
        }
        return new CombinedRequirement(requirements);
    }

    private static void flatten(WarpRequirement requirement, List<WarpRequirement> output) {
        if (requirement instanceof CombinedRequirement combined) {
            combined.getRequirements().forEach(child -> flatten(child, output));
        } else {
            output.add(requirement);
        }
    }

    private static long experienceValue(Player player, int levels) {
        int upperLevel = Math.max(player.experienceLevel, levels);
        long points = 0L;
        for (int offset = 1; offset <= levels; offset++) {
            points += pointsForLevel(upperLevel - offset);
        }
        return points + (long) Math.floor(player.experienceProgress * pointsForLevel(upperLevel))
                - (long) Math.floor(player.experienceProgress * pointsForLevel(upperLevel - levels));
    }

    private static long pointsForLevel(int level) {
        if (level >= 30) {
            return 112L + (level - 30L) * 9L;
        }
        return level >= 15 ? 37L + (level - 15L) * 5L : 7L + level * 2L;
    }
}
