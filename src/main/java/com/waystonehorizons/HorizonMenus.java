package com.waystonehorizons;

import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HorizonMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, WaystoneHorizons.MOD_ID);
    public static final RegistryObject<MenuType<WaystoneSelectionMenu>> WARP_STONE_SELECTION = MENUS.register("warp_stone_selection",
            () -> IForgeMenuType.create((id, inventory, data) -> new HorizonSelectionMenu(id, inventory.player, data.readItem(), data.readEnum(net.minecraft.world.InteractionHand.class))));

    private HorizonMenus() {
    }
}
