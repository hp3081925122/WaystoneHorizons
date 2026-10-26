package com.waystonehorizons.mixin;

import com.mojang.datafixers.util.Either;
import com.waystonehorizons.WarpStoneRequirements;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.waystones.core.WaystoneTeleportContextImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WaystoneTeleportContextImpl.class, remap = false)
public abstract class WaystonesRequirementsMixin {
    @Redirect(method = "getRequirements", at = @At(value = "INVOKE", target = "Lnet/blay09/mods/shogi/ShogiValue;get(Ljava/lang/Object;)Lcom/mojang/datafixers/util/Either;"))
    private Either<?, ?> adjustRequirements(ShogiValue<Object, ?> value, Object context) {
        return WarpStoneRequirements.resolve(() -> value.get(context), context);
    }
}
