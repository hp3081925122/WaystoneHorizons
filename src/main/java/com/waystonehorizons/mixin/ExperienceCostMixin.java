package com.waystonehorizons.mixin;

import com.waystonehorizons.WarpStoneRequirements;
import net.blay09.mods.shogi.common.effect.cost.DamageItem;
import net.blay09.mods.shogi.common.effect.cost.ExperienceLevelCost;
import net.blay09.mods.shogi.common.effect.cost.ExperiencePointsCost;
import net.blay09.mods.shogi.effect.ShogiEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {ExperienceLevelCost.class, ExperiencePointsCost.class, DamageItem.class}, remap = false)
public abstract class ExperienceCostMixin {
    @Redirect(method = "apply(Lnet/blay09/mods/shogi/context/ShogiContext;)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Lnet/blay09/mods/shogi/effect/ShogiEffect;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object adjustCost(ShogiEffect<?> expression, Object context) {
        return WarpStoneRequirements.evaluateCost(expression, context, (Object) this instanceof ExperienceLevelCost, (Object) this instanceof DamageItem);
    }
}
