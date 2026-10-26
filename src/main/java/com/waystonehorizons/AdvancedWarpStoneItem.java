package com.waystonehorizons;

import java.util.function.Consumer;
import net.blay09.mods.waystones.api.WarpStoneTypes;
import net.blay09.mods.waystones.item.WarpStoneItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class AdvancedWarpStoneItem extends WarpStoneItem {
    private final ModConfigSpec.IntValue cooldown;
    private final int levelDiscount;

    public AdvancedWarpStoneItem(Properties properties, ModConfigSpec.IntValue cooldown, int levelDiscount) {
        super(WarpStoneTypes.UNSCOPED, properties);
        this.cooldown = cooldown;
        this.levelDiscount = levelDiscount;
    }

    public int getCooldownSeconds() {
        return HorizonConfig.SPEC.isLoaded() ? cooldown.get() : cooldown.getDefault();
    }

    public int getLevelDiscount() {
        return levelDiscount;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tooltip.waystonehorizons.cooldown", getCooldownSeconds()).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.waystonehorizons.level_discount", levelDiscount).withStyle(ChatFormatting.GREEN));
    }
}
