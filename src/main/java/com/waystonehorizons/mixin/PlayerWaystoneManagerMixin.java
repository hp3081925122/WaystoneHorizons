package com.waystonehorizons.mixin;

import com.mojang.datafixers.util.Either;
import com.waystonehorizons.AdvancedWarpStoneItem;
import com.waystonehorizons.HorizonCooldowns;
import com.waystonehorizons.HorizonSelectionMenu;
import java.util.List;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.api.IWaystoneTeleportContext;
import net.blay09.mods.waystones.api.WaystoneTeleportError;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.core.WarpMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerWaystoneManager.class, remap = false)
public abstract class PlayerWaystoneManagerMixin {
    @Inject(method = "findWarpItem(Lnet/minecraft/world/entity/Entity;Lnet/blay09/mods/waystones/core/WarpMode;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private static void resolveSource(Entity entity, WarpMode mode, CallbackInfoReturnable<ItemStack> callback) {
        if (mode == WarpMode.WARP_STONE && entity instanceof Player player && player.containerMenu instanceof HorizonSelectionMenu menu) {
            callback.setReturnValue(menu.getWarpItem(player));
        }
    }

    @Inject(method = "getExperienceLevelCost", at = @At("RETURN"), cancellable = true)
    private static void reduceExperience(Entity entity, IWaystone target, WarpMode mode, IWaystoneTeleportContext context, CallbackInfoReturnable<Integer> callback) {
        if (mode != WarpMode.WARP_STONE || !(entity instanceof Player)) {
            return;
        }
        ItemStack stack = context.getWarpItem().isEmpty() ? PlayerWaystoneManager.findWarpItem(entity, mode) : context.getWarpItem();
        if (stack.getItem() instanceof AdvancedWarpStoneItem item) {
            callback.setReturnValue((int) Math.max(0L, (long) callback.getReturnValue() - item.getLevelDiscount()));
        }
    }

    @Inject(method = "canUseWarpStone", at = @At("HEAD"), cancellable = true)
    private static void checkCooldown(Player player, ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (stack.getItem() instanceof AdvancedWarpStoneItem) {
            callback.setReturnValue(player.getAbilities().instabuild || HorizonCooldowns.getRemaining(player) <= 0L);
        }
    }

    @Inject(method = "applyCooldown", at = @At("HEAD"), cancellable = true)
    private static void keepOriginalCooldown(WarpMode mode, Player player, int cooldown, CallbackInfo callback) {
        if (mode == WarpMode.WARP_STONE && PlayerWaystoneManager.findWarpItem(player, mode).getItem() instanceof AdvancedWarpStoneItem) {
            callback.cancel();
        }
    }

    @Inject(method = "tryTeleport", at = @At("RETURN"))
    private static void finishTeleport(IWaystoneTeleportContext context, CallbackInfoReturnable<Either<List<Entity>, WaystoneTeleportError>> callback) {
        if (callback.getReturnValue().left().isPresent()) {
            HorizonCooldowns.afterTeleport(context);
        }
    }
}
