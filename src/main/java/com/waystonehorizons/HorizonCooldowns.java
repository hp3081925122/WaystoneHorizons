package com.waystonehorizons;

import net.blay09.mods.shogi.common.effect.server.cooldown.CooldownInformation;
import net.blay09.mods.waystones.api.event.WaystoneTeleportEvent;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class HorizonCooldowns {
    public static final Identifier COOLDOWN_KEY = Identifier.fromNamespaceAndPath(WaystoneHorizons.MOD_ID, "warp_stone");
    private static final String NBT_KEY = "waystonehorizons:cooldown_until";

    private HorizonCooldowns() {
    }

    public static long getRemainingMillis(Player player) {
        long until = player.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG).getLongOr(NBT_KEY, 0L);
        return Math.max(0L, until - System.currentTimeMillis());
    }

    public static void setUntil(Player player, long until) {
        CompoundTag data = player.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        data.putLong(NBT_KEY, until);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
    }

    public static CooldownInformation information(Player player, int seconds) {
        long ticks = (getRemainingMillis(player) + 49L) / 50L;
        return new CooldownInformation(COOLDOWN_KEY, ticks, (int) Math.min(Integer.MAX_VALUE, seconds * 20L), System.currentTimeMillis(), 50000000L);
    }

    public static void afterTeleport(WaystoneTeleportEvent.After event) {
        var context = event.getContext();
        if (!(context.getEntity() instanceof Player player) || !(context.getWarpItem().getItem() instanceof AdvancedWarpStoneItem stone)) {
            return;
        }
        if (WaystonesConfig.getActive().rules.enableCooldowns && stone.getCooldownSeconds() > 0) {
            setUntil(player, System.currentTimeMillis() + stone.getCooldownSeconds() * 1000L);
        }
        if (WaystonesConfig.getActive().rules.enableDurability) {
            var hand = context.getWarpHand();
            if (hand != null) {
                context.getWarpItem().hurtAndBreak(1, player, hand.asEquipmentSlot());
            }
        }
    }
}
