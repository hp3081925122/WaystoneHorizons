package com.waystonehorizons;

import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.core.WarpMode;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class HorizonSelectionMenu extends WaystoneSelectionMenu {
    private final ItemStack source;
    private final InteractionHand hand;

    public HorizonSelectionMenu(int id, Player player, ItemStack source, InteractionHand hand) {
        super(HorizonMenus.WARP_STONE_SELECTION.get(), WarpMode.WARP_STONE, null, id, PlayerWaystoneManager.getWaystones(player));
        this.source = source.copy();
        this.hand = hand;
    }

    public ItemStack getWarpItem(Player player) {
        ItemStack held = player.getItemInHand(hand);
        return !source.isEmpty() && source.getItem() instanceof AdvancedWarpStoneItem && ItemStack.isSameItemSameTags(source, held) ? held : ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return !getWarpItem(player).isEmpty() && super.stillValid(player);
    }
}
