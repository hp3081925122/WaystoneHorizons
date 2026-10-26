package com.waystonehorizons.mixin;

import com.waystonehorizons.WarpStoneRequirements;
import net.blay09.mods.waystones.InternalMethodsImpl;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InternalMethodsImpl.class, remap = false)
public abstract class WaystonesRequirementsMixin {
    @Inject(method = "resolveRequirements", at = @At("RETURN"), cancellable = true, remap = false)
    private void waystonehorizons$adjustRequirements(WaystoneTeleportContext context, CallbackInfoReturnable<WarpRequirement> callback) {
        callback.setReturnValue(WarpStoneRequirements.adjust(context, callback.getReturnValue()));
    }
}
