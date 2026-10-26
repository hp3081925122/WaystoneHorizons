package com.waystonehorizons;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HorizonItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WaystoneHorizons.MOD_ID);
    public static final DeferredItem<AdvancedWarpStoneItem> DIAMOND_WARP_STONE = ITEMS.registerItem("diamond_warp_stone",
            properties -> new AdvancedWarpStoneItem(properties, HorizonConfig.DIAMOND_COOLDOWN, 1));
    public static final DeferredItem<AdvancedWarpStoneItem> NETHERITE_WARP_STONE = ITEMS.registerItem("netherite_warp_stone",
            properties -> new AdvancedWarpStoneItem(properties.fireResistant(), HorizonConfig.NETHERITE_COOLDOWN, 2));
    public static final DeferredItem<AdvancedWarpStoneItem> NETHER_STAR_WARP_STONE = ITEMS.registerItem("nether_star_warp_stone",
            properties -> new AdvancedWarpStoneItem(properties, HorizonConfig.NETHER_STAR_COOLDOWN, 3));

    private HorizonItems() {
    }

    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        for (var stone : java.util.List.of(DIAMOND_WARP_STONE, NETHERITE_WARP_STONE, NETHER_STAR_WARP_STONE)) {
            event.modify(stone.get(), (components, context, item) -> components.set(DataComponents.MAX_DAMAGE, 128));
        }
    }

    public static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(DIAMOND_WARP_STONE.get());
            event.accept(NETHERITE_WARP_STONE.get());
            event.accept(NETHER_STAR_WARP_STONE.get());
        }
    }
}
