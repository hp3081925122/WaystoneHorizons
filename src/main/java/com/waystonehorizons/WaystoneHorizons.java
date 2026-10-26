package com.waystonehorizons;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(WaystoneHorizons.MOD_ID)
public final class WaystoneHorizons {
    public static final String MOD_ID = "waystonehorizons";

    public WaystoneHorizons(IEventBus modEventBus, ModContainer modContainer) {
        HorizonItems.ITEMS.register(modEventBus);
        modEventBus.addListener(HorizonItems::addCreativeItems);
        modEventBus.addListener(HorizonItems::modifyComponents);
        modContainer.registerConfig(ModConfig.Type.SERVER, HorizonConfig.SPEC);
        net.blay09.mods.waystones.api.event.WaystoneTeleportEvent.After.EVENT.register(HorizonCooldowns::afterTeleport);
    }
}
