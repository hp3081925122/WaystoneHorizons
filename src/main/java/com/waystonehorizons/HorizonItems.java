package com.waystonehorizons;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HorizonItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WaystoneHorizons.MOD_ID);
    public static final DeferredItem<AdvancedWarpStoneItem> DIAMOND_WARP_STONE = ITEMS.register("diamond_warp_stone",
            () -> new AdvancedWarpStoneItem(new Item.Properties(), HorizonConfig.DIAMOND_COOLDOWN, 1));
    public static final DeferredItem<AdvancedWarpStoneItem> NETHERITE_WARP_STONE = ITEMS.register("netherite_warp_stone",
            () -> new AdvancedWarpStoneItem(new Item.Properties().fireResistant(), HorizonConfig.NETHERITE_COOLDOWN, 2));
    public static final DeferredItem<AdvancedWarpStoneItem> NETHER_STAR_WARP_STONE = ITEMS.register("nether_star_warp_stone",
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
