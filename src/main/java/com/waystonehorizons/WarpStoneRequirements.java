package com.waystonehorizons;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.blay09.mods.shogi.coercion.Coercion;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.context.executor.EffectExecutor;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.config.rules.WaystonesEffectExecutors;
import net.blay09.mods.waystones.core.WaystoneTeleportContextImpl;
import net.minecraft.world.entity.player.Player;

public final class WarpStoneRequirements {
    private static final ThreadLocal<DiscountSession> ACTIVE = new ThreadLocal<>();

    private WarpStoneRequirements() {
    }

    public static Either<?, ?> resolve(Supplier<? extends Either<?, ?>> resolver, Object value) {
        if (!(value instanceof WaystoneTeleportContextImpl context)
                || !(context.getWarpItem().getItem() instanceof AdvancedWarpStoneItem stone)
                || !(context.getEntity() instanceof Player player)) {
            return resolver.get();
        }
        EffectExecutor originalExecutor = context.executor();
        DiscountSession previous = ACTIVE.get();
        DiscountSession session = new DiscountSession();
        ACTIVE.set(session);
        try {
            context.setExecutor(WaystonesEffectExecutors.simulated());
            session.probing = true;
            resolver.get();
            context.setExecutor(originalExecutor);
            session.probing = false;
            session.levelsLeft = stone.getLevelDiscount();
            int unusedLevels = (int) Math.max(0L, stone.getLevelDiscount() - session.totalLevels);
            session.pointsLeft = experienceValue(player, unusedLevels);
            Either<?, ?> result = resolver.get();
            List<Object> information = new ArrayList<>(result.map(Coercion.LIST, Coercion.LIST));
            boolean failed = result.right().isPresent();
            if (stone.getCooldownSeconds() > 0 && WaystonesConfig.getActive().rules.enableCooldowns) {
                information.add(HorizonCooldowns.information(player, stone.getCooldownSeconds()));
                failed |= HorizonCooldowns.getRemainingMillis(player) > 0;
            }
            return failed ? Either.right(information) : Either.left(information);
        } finally {
            context.setExecutor(originalExecutor);
            if (previous == null) {
                ACTIVE.remove();
            } else {
                ACTIVE.set(previous);
            }
        }
    }

    public static Object evaluateCost(ShogiEffect<?> expression, Object context, boolean levels, boolean damage) {
        Either<?, ?> result = expression.apply((ShogiContext) context);
        DiscountSession session = ACTIVE.get();
        if (session == null) {
            return result;
        }
        return result.mapLeft(value -> {
            int cost = Coercion.NON_NEGATIVE_INT.apply(value);
            if (damage) {
                return 0;
            }
            if (session.probing) {
                if (levels) {
                    session.totalLevels += cost;
                }
                return 0;
            }
            if (levels) {
                int discounted = Math.max(0, cost - session.levelsLeft);
                session.levelsLeft = Math.max(0, session.levelsLeft - cost);
                return discounted;
            }
            int discounted = (int) Math.max(0L, cost - session.pointsLeft);
            session.pointsLeft = Math.max(0L, session.pointsLeft - cost);
            return discounted;
        });
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

    private static final class DiscountSession {
        private boolean probing;
        private long totalLevels;
        private int levelsLeft;
        private long pointsLeft;
    }
}
