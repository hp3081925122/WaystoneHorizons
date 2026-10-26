package com.waystonehorizons;

import java.util.List;
import net.blay09.mods.waystones.item.WarpStoneItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class AdvancedWarpStoneItem extends WarpStoneItem {
    private final ModConfigSpec.IntValue cooldown;
    private final int levelDiscount;

    public AdvancedWarpStoneItem(Properties properties, ModConfigSpec.IntValue cooldown, int levelDiscount) {
        super(properties);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.waystonehorizons.cooldown", getCooldownSeconds()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.waystonehorizons.level_discount", levelDiscount).withStyle(ChatFormatting.GREEN));
    }
}
