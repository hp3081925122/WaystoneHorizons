package com.waystonehorizons;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class HorizonItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WaystoneHorizons.MOD_ID);
    public static final RegistryObject<AdvancedWarpStoneItem> DIAMOND_WARP_STONE = ITEMS.register("diamond_warp_stone",
            () -> new AdvancedWarpStoneItem(new Item.Properties(), HorizonConfig.DIAMOND_COOLDOWN, 1));
    public static final RegistryObject<AdvancedWarpStoneItem> NETHERITE_WARP_STONE = ITEMS.register("netherite_warp_stone",
            () -> new AdvancedWarpStoneItem(new Item.Properties().fireResistant(), HorizonConfig.NETHERITE_COOLDOWN, 2));
    public static final RegistryObject<AdvancedWarpStoneItem> NETHER_STAR_WARP_STONE = ITEMS.register("nether_star_warp_stone",
            () -> new AdvancedWarpStoneItem(new Item.Properties(), HorizonConfig.NETHER_STAR_COOLDOWN, 3));

    private HorizonItems() {
    }

    public static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(DIAMOND_WARP_STONE.get());
            event.accept(NETHERITE_WARP_STONE.get());
            event.accept(NETHER_STAR_WARP_STONE.get());
        }
    }
}
