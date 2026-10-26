package com.waystonehorizons.client;

import com.waystonehorizons.HorizonMenus;
import com.waystonehorizons.WaystoneHorizons;
import net.blay09.mods.waystones.client.gui.screen.WaystoneSelectionScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = WaystoneHorizons.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HorizonClient {
    private HorizonClient() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(HorizonMenus.WARP_STONE_SELECTION.get(), WaystoneSelectionScreen::new));
    }
}
