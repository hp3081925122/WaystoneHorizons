package com.waystonehorizons;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.waystones.api.IWaystoneTeleportContext;
import net.blay09.mods.waystones.core.WarpMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class HorizonCooldowns {
    private static final String KEY = "waystonehorizons:cooldown_until";

    private HorizonCooldowns() {
    }

    public static void registerNetworking() {
        Balm.getNetworking().registerClientboundPacket(new ResourceLocation(WaystoneHorizons.MOD_ID, "cooldown"),
                CooldownSync.class, CooldownSync::encode, CooldownSync::decode, CooldownSync::handle);
    }

    public static long getRemaining(Player player) {
        if (HorizonConfig.SPEC.isLoaded() && !HorizonConfig.ENABLE_COOLDOWNS.get()) {
            return 0L;
        }
        return Math.max(0L, player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getLong(KEY) - System.currentTimeMillis());
    }

    public static void setUntil(Player player, long until) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putLong(KEY, until);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void sync(Player player) {
        if (player instanceof ServerPlayer) {
            Balm.getNetworking().sendTo(player, new CooldownSync(player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getLong(KEY)));
        }
    }

    public static void afterTeleport(IWaystoneTeleportContext context) {
        if (context.getWarpMode() != WarpMode.WARP_STONE || !(context.getEntity() instanceof Player player)
                || !(context.getWarpItem().getItem() instanceof AdvancedWarpStoneItem item)) {
            return;
        }
        if (HorizonConfig.ENABLE_COOLDOWNS.get() && item.getCooldownSeconds() > 0) {
            setUntil(player, System.currentTimeMillis() + item.getCooldownSeconds() * 1000L);
            sync(player);
        }
        if (HorizonConfig.ENABLE_DURABILITY.get()) {
            ItemStack stack = context.getWarpItem();
            InteractionHand hand = player.getOffhandItem() == stack ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(hand));
        }
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        sync(event.getEntity());
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync(event.getEntity());
    }

    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync(event.getEntity());
    }

    public record CooldownSync(long until) {
        public static void encode(CooldownSync message, FriendlyByteBuf data) {
            data.writeLong(message.until);
        }

        public static CooldownSync decode(FriendlyByteBuf data) {
            return new CooldownSync(data.readLong());
        }

        public static void handle(Player player, CooldownSync message) {
            setUntil(player, message.until);
        }
    }
}
