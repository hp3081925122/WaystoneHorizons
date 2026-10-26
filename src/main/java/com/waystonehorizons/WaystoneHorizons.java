package com.waystonehorizons;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WaystoneHorizons.MOD_ID)
public final class WaystoneHorizons {
    public static final String MOD_ID = "waystonehorizons";

    public WaystoneHorizons() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        HorizonItems.ITEMS.register(modEventBus);
        HorizonMenus.MENUS.register(modEventBus);
        modEventBus.addListener(HorizonItems::addCreativeItems);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, HorizonConfig.SPEC);
        HorizonCooldowns.registerNetworking();
        MinecraftForge.EVENT_BUS.addListener(HorizonCooldowns::onLogin);
        MinecraftForge.EVENT_BUS.addListener(HorizonCooldowns::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(HorizonCooldowns::onDimensionChange);
    }
}
