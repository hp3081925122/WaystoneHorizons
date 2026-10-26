package com.waystonehorizons;

import java.util.List;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.waystones.compat.Compat;
import net.blay09.mods.waystones.item.WarpStoneItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

public final class AdvancedWarpStoneItem extends WarpStoneItem {
    private final ForgeConfigSpec.IntValue cooldown;
    private final int levelDiscount;

    public AdvancedWarpStoneItem(Properties properties, ForgeConfigSpec.IntValue cooldown, int levelDiscount) {
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
    public int getMaxDamage(ItemStack stack) {
        return 128;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.isDamaged();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / getMaxDamage(stack));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild && HorizonCooldowns.getRemaining(player) > 0) {
            player.displayClientMessage(Component.translatable("chat.waystones.warpstone_not_charged").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!player.isUsingItem() && !level.isClientSide) {
            level.playSound(null, player, SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.1F, 2.0F);
        }
        if (getUseDuration(stack) <= 0 || Compat.isVivecraftInstalled) {
            openSelection(stack, player, hand);
        } else {
            player.startUsingItem(hand);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            openSelection(stack, player, player.getUsedItemHand());
        }
        return stack;
    }

    private void openSelection(ItemStack stack, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer) || (!player.getAbilities().instabuild && HorizonCooldowns.getRemaining(player) > 0)) {
            return;
        }
        Balm.getNetworking().openMenu(player, new BalmMenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.waystones.waystone_selection");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player owner) {
                return new HorizonSelectionMenu(id, owner, stack, hand);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayer owner, FriendlyByteBuf data) {
                data.writeItem(stack);
                data.writeEnum(hand);
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.waystonehorizons.cooldown", getCooldownSeconds()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.waystonehorizons.level_discount", levelDiscount).withStyle(ChatFormatting.GREEN));
        Player player = Balm.getProxy().getClientPlayer();
        if (player != null) {
            long remaining = HorizonCooldowns.getRemaining(player);
            if (remaining > 0) {
                tooltip.add(Component.translatable("tooltip.waystones.cooldown_left", (remaining + 999L) / 1000L).withStyle(ChatFormatting.GOLD));
            }
        }
    }
}
