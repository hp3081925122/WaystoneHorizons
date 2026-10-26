package com.waystonehorizons;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HorizonConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue DIAMOND_COOLDOWN;
    public static final ModConfigSpec.IntValue NETHERITE_COOLDOWN;
    public static final ModConfigSpec.IntValue NETHER_STAR_COOLDOWN;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("传送石冷却设置，单位为秒，0 表示无冷却。", "Warp stone cooldowns in seconds. Set to 0 to disable.").push("cooldowns");
        DIAMOND_COOLDOWN = builder.comment("钻石传送石冷却秒数。", "Diamond warp stone cooldown in seconds.")
                .defineInRange("diamond", 150, 0, Integer.MAX_VALUE);
        NETHERITE_COOLDOWN = builder.comment("下界合金传送石冷却秒数。", "Netherite warp stone cooldown in seconds.")
                .defineInRange("netherite", 60, 0, Integer.MAX_VALUE);
        NETHER_STAR_COOLDOWN = builder.comment("下界之星传送石冷却秒数。", "Nether star warp stone cooldown in seconds.")
                .defineInRange("nether_star", 30, 0, Integer.MAX_VALUE);
        builder.pop();
        SPEC = builder.build();
    }

    private HorizonConfig() {
    }
}
