package com.waystonehorizons;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HorizonConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue DIAMOND_COOLDOWN;
    public static final ForgeConfigSpec.IntValue NETHERITE_COOLDOWN;
    public static final ForgeConfigSpec.IntValue NETHER_STAR_COOLDOWN;
    public static final ForgeConfigSpec.BooleanValue ENABLE_COOLDOWNS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DURABILITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("传送石冷却设置，单位为秒，0 表示无冷却。", "Warp stone cooldowns in seconds. Set to 0 to disable.").push("cooldowns");
        DIAMOND_COOLDOWN = builder.comment("钻石传送石冷却秒数。", "Diamond warp stone cooldown in seconds.")
                .defineInRange("diamond", 150, 0, Integer.MAX_VALUE);
        NETHERITE_COOLDOWN = builder.comment("下界合金传送石冷却秒数。", "Netherite warp stone cooldown in seconds.")
                .defineInRange("netherite", 60, 0, Integer.MAX_VALUE);
        NETHER_STAR_COOLDOWN = builder.comment("下界之星传送石冷却秒数。", "Nether star warp stone cooldown in seconds.")
                .defineInRange("nether_star", 30, 0, Integer.MAX_VALUE);
        builder.pop();
        ENABLE_COOLDOWNS = builder.comment("启用附属传送石冷却。", "Enable addon warp stone cooldowns.")
                .define("enableCooldowns", true);
        ENABLE_DURABILITY = builder.comment("成功传送消耗一点耐久。", "Consume one durability after a successful teleport.")
                .define("enableDurability", true);
        SPEC = builder.build();
    }

    private HorizonConfig() {
    }
}
