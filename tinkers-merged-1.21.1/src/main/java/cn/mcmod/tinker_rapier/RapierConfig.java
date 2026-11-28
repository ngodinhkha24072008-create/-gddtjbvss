package cn.mcmod.tinker_rapier;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;

public class RapierConfig {
   public static ModConfigSpec COMMON_CONFIG;
   public static DoubleValue RAPIER_ATTACK_BOUNS;

   static {
      Builder COMMON_BUILDER = new Builder();
      COMMON_BUILDER.comment("General settings").push("general");
      RAPIER_ATTACK_BOUNS = COMMON_BUILDER.comment("the sting attack multiplier of Rapiers").defineInRange("rapier_attack_bouns", 1.0, 0.01, 10.0);
      COMMON_BUILDER.pop();
      COMMON_CONFIG = COMMON_BUILDER.build();
   }
}
