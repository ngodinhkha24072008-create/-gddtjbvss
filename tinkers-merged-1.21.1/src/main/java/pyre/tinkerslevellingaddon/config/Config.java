package pyre.tinkerslevellingaddon.config;

import com.electronwill.nightconfig.core.EnumGetMethod;
import java.util.List;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig.Type;
import pyre.tinkerslevellingaddon.setup.Sounds;
import pyre.tinkerslevellingaddon.util.ToolLevellingUtil;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

public class Config {
   private static final List<String> DEFAULT_ARMOR_SLOTS_ORDER = List.of("upgrade", "defense", "upgrade", "ability", "defense");
   private static final List<String> DEFAULT_ARMOR_SLOTS_RANDOM_POOL = List.of("upgrade", "upgrade", "defense", "defense", "ability");
   private static final List<String> DEFAULT_RANGED_SLOTS_ORDER = List.of("upgrade", "upgrade", "upgrade", "ability", "upgrade");
   private static final List<String> DEFAULT_RANGED_SLOTS_RANDOM_POOL = List.of("upgrade", "upgrade", "upgrade", "upgrade", "ability");
   private static final List<String> DEFAULT_SHIELD_SLOTS_ORDER = List.of("upgrade", "defense", "upgrade", "defense", "ability");
   private static final List<String> DEFAULT_SHIELD_SLOTS_RANDOM_POOL = List.of("upgrade", "defense", "upgrade", "defense", "ability");
   private static final List<String> DEFAULT_STAFF_SLOTS_ORDER = List.of("upgrade", "upgrade", "upgrade", "defense", "ability");
   private static final List<String> DEFAULT_STAFF_SLOTS_RANDOM_POOL = List.of("upgrade", "upgrade", "upgrade", "defense", "ability");
   private static final List<String> DEFAULT_TOOLS_SLOTS_ORDER = List.of("upgrade", "upgrade", "upgrade", "ability", "upgrade");
   private static final List<String> DEFAULT_TOOLS_SLOTS_RANDOM_POOL = List.of("upgrade", "upgrade", "upgrade", "upgrade", "ability");
   private static final List<String> DEFAULT_ARMOR_STATS_ORDER = List.of("durability", "armor", "armorToughness", "knockbackResistance");
   private static final List<String> DEFAULT_ARMOR_STATS_RANDOM_POOL = List.of("durability", "armor", "armorToughness", "knockbackResistance");
   private static final List<String> DEFAULT_RANGED_STATS_ORDER = List.of("durability", "drawSpeed", "velocity", "accuracy", "projectileDamage");
   private static final List<String> DEFAULT_RANGED_STATS_RANDOM_POOL = List.of("durability", "drawSpeed", "velocity", "accuracy", "projectileDamage");
   private static final List<String> DEFAULT_SHIELD_STATS_ORDER = List.of("durability", "blockAmount", "blockAngle", "armorToughness", "knockbackResistance");
   private static final List<String> DEFAULT_SHIELD_STATS_RANDOM_POOL = List.of(
      "durability", "blockAmount", "blockAngle", "armorToughness", "knockbackResistance"
   );
   private static final List<String> DEFAULT_STAFF_STATS_ORDER = List.of("durability", "drawSpeed", "velocity", "accuracy", "projectileDamage", "armor");
   private static final List<String> DEFAULT_STAFF_STATS_RANDOM_POOL = List.of("durability", "drawSpeed", "velocity", "accuracy", "projectileDamage", "armor");
   private static final List<String> DEFAULT_TOOLS_STATS_ORDER = List.of("durability", "attackDamage", "attackSpeed", "miningSpeed");
   private static final List<String> DEFAULT_TOOLS_STATS_RANDOM_POOL = List.of("durability", "attackDamage", "attackSpeed", "miningSpeed");
   public static final ModConfigSpec SERVER_CONFIG;
   public static final ModConfigSpec CLIENT_CONFIG;
   public static EnumValue<Config.GainingMethod> armorSlotGainingMethod;
   public static EnumValue<Config.GainingMethod> armorStatGainingMethod;
   public static EnumValue<Config.GainingMethod> rangedSlotGainingMethod;
   public static EnumValue<Config.GainingMethod> rangedStatGainingMethod;
   public static EnumValue<Config.GainingMethod> shieldSlotGainingMethod;
   public static EnumValue<Config.GainingMethod> shieldStatGainingMethod;
   public static EnumValue<Config.GainingMethod> staffSlotGainingMethod;
   public static EnumValue<Config.GainingMethod> staffStatGainingMethod;
   public static EnumValue<Config.GainingMethod> toolsSlotGainingMethod;
   public static EnumValue<Config.GainingMethod> toolsStatGainingMethod;
   public static IntValue maxLevel;
   public static IntValue baseExperience;
   public static DoubleValue requiredXpMultiplier;
   public static DoubleValue broadToolRequiredXpMultiplier;
   private static ConfigValue<List<? extends String>> armorSlotTypeRandomPool;
   private static ConfigValue<List<? extends String>> armorSlotTypeOrder;
   private static ConfigValue<List<? extends String>> rangedSlotTypeRandomPool;
   private static ConfigValue<List<? extends String>> rangedSlotTypeOrder;
   private static ConfigValue<List<? extends String>> shieldSlotTypeRandomPool;
   private static ConfigValue<List<? extends String>> shieldSlotTypeOrder;
   private static ConfigValue<List<? extends String>> staffSlotTypeRandomPool;
   private static ConfigValue<List<? extends String>> staffSlotTypeOrder;
   private static ConfigValue<List<? extends String>> toolsSlotTypeRandomPool;
   private static ConfigValue<List<? extends String>> toolsSlotTypeOrder;
   private static ConfigValue<List<? extends String>> armorStatTypeRandomPool;
   private static ConfigValue<List<? extends String>> armorStatTypeOrder;
   private static ConfigValue<List<? extends String>> rangedStatTypeRandomPool;
   private static ConfigValue<List<? extends String>> rangedStatTypeOrder;
   private static ConfigValue<List<? extends String>> shieldStatTypeRandomPool;
   private static ConfigValue<List<? extends String>> shieldStatTypeOrder;
   private static ConfigValue<List<? extends String>> staffStatTypeRandomPool;
   private static ConfigValue<List<? extends String>> staffStatTypeOrder;
   private static ConfigValue<List<? extends String>> toolsStatTypeRandomPool;
   private static ConfigValue<List<? extends String>> toolsStatTypeOrder;
   public static DoubleValue armorArmorValue;
   public static DoubleValue armorArmorToughnessValue;
   public static IntValue armorDurabilityValue;
   public static DoubleValue armorKnockbackResistanceValue;
   public static DoubleValue rangedAccuracyValue;
   public static DoubleValue rangedAttackDamageValue;
   public static DoubleValue rangedAttackSpeedValue;
   public static DoubleValue rangedDrawSpeedValue;
   public static IntValue rangedDurabilityValue;
   public static DoubleValue rangedProjectileDamageValue;
   public static DoubleValue rangedVelocityValue;
   public static DoubleValue shieldArmorToughnessValue;
   public static DoubleValue shieldBlockAmountValue;
   public static IntValue shieldBlockAngleValue;
   public static IntValue shieldDurabilityValue;
   public static DoubleValue shieldKnockbackResistanceValue;
   public static DoubleValue staffAccuracyValue;
   public static DoubleValue staffArmorValue;
   public static DoubleValue staffDrawSpeedValue;
   public static IntValue staffDurabilityValue;
   public static DoubleValue staffProjectileDamageValue;
   public static DoubleValue staffVelocityValue;
   public static DoubleValue toolAttackDamageValue;
   public static DoubleValue toolAttackSpeedValue;
   public static IntValue toolDurabilityValue;
   public static DoubleValue toolMiningSpeedValue;
   public static BooleanValue damageBlocked;
   public static BooleanValue damageDealt;
   public static BooleanValue damageTaken;
   public static BooleanValue enablePvp;
   public static IntValue flyingTime;
   public static BooleanValue enableAttackingXp;
   public static BooleanValue enableBlockingDamageXp;
   public static BooleanValue enableBonkingXp;
   public static BooleanValue enableBrushingXp;
   public static BooleanValue enableBucketingXp;
   public static BooleanValue enableFirestarterXp;
   public static BooleanValue enableFishingXp;
   public static BooleanValue enableFlamewakeXp;
   public static BooleanValue enableFlingingXp;
   public static BooleanValue enableFlyingXp;
   public static BooleanValue enableFrostWalkerXp;
   public static BooleanValue enableGlowingXp;
   public static BooleanValue enableGrapplingXp;
   public static BooleanValue enableHarvestingXp;
   public static BooleanValue enableMeltingXp;
   public static BooleanValue enableMiningXp;
   public static BooleanValue enablePathMakerXp;
   public static BooleanValue enablePathMakingXp;
   public static BooleanValue enablePlowingXp;
   public static BooleanValue enableScrappingXp;
   public static BooleanValue enableShearingXp;
   public static BooleanValue enableShootingXp;
   public static BooleanValue enableSlimeballXp;
   public static BooleanValue enableSlurpingXp;
   public static BooleanValue enableSnowdriftXp;
   public static BooleanValue enableSpillingXp;
   public static BooleanValue enableSpittingXp;
   public static BooleanValue enableSplashingXp;
   public static BooleanValue enableSpringingXp;
   public static BooleanValue enableStrippingXp;
   public static BooleanValue enableTakingDamageXp;
   public static BooleanValue enableThornsXp;
   public static BooleanValue enableTillingXp;
   public static BooleanValue enableWarpingXp;
   public static BooleanValue enableWaxingOffXp;
   public static IntValue bonusAttackingXp;
   public static IntValue bonusBlockingDamageXp;
   public static IntValue bonusBonkingXp;
   public static IntValue bonusBrushingXp;
   public static IntValue bonusBucketingXp;
   public static IntValue bonusFirestarterXp;
   public static IntValue bonusFishingXp;
   public static IntValue bonusFlamewakeXp;
   public static IntValue bonusFlingingXp;
   public static IntValue bonusFlyingXp;
   public static IntValue bonusFrostWalkerXp;
   public static IntValue bonusGlowingXp;
   public static IntValue bonusGrapplingXp;
   public static IntValue bonusHarvestingXp;
   public static IntValue bonusMeltingXp;
   public static IntValue bonusMiningXp;
   public static IntValue bonusPathMakerXp;
   public static IntValue bonusPathMakingXp;
   public static IntValue bonusPlowingXp;
   public static IntValue bonusScrappingXp;
   public static IntValue bonusShearingXp;
   public static IntValue bonusShootingXp;
   public static IntValue bonusSlimeballXp;
   public static IntValue bonusSlurpingXp;
   public static IntValue bonusSnowdriftXp;
   public static IntValue bonusSpillingXp;
   public static IntValue bonusSpittingXp;
   public static IntValue bonusSplashingXp;
   public static IntValue bonusSpringingXp;
   public static IntValue bonusStrippingXp;
   public static IntValue bonusTakingDamageXp;
   public static IntValue bonusThornsXp;
   public static IntValue bonusTillingXp;
   public static IntValue bonusWarpingXp;
   public static IntValue bonusWaxingOffXp;
   public static BooleanValue enableLevelUpMessage;
   public static BooleanValue squashLevelPluses;
   public static EnumValue<Config.LevelUpSound> levelUpSound;

   private static void generalConfig(Builder builder) {
      builder.comment("General addon settings").push("general");
      armorSlotGainingMethod = builder.comment("Method of gaining modifier slots for armor.")
         .translation("config.tinkerslevellingaddon.general.armor_slot_gaining_method")
         .defineEnum("armorSlotGainingMethod", Config.GainingMethod.PREDEFINED_ORDER, EnumGetMethod.NAME_IGNORECASE, Config.GainingMethod.values());
      rangedSlotGainingMethod = builder.comment("Method of gaining modifier slots for ranged weapons.")
         .translation("config.tinkerslevellingaddon.general.ranged_slot_gaining_method")
         .defineEnum("rangedSlotGainingMethod", Config.GainingMethod.PREDEFINED_ORDER, EnumGetMethod.NAME_IGNORECASE, Config.GainingMethod.values());
      shieldSlotGainingMethod = builder.comment("Method of gaining modifier slots for shields.")
         .translation("config.tinkerslevellingaddon.general.shield_slot_gaining_method")
         .defineEnum("shieldSlotGainingMethod", Config.GainingMethod.PREDEFINED_ORDER, EnumGetMethod.NAME_IGNORECASE, Config.GainingMethod.values());
      staffSlotGainingMethod = builder.comment("Method of gaining modifier slots for staffs.")
         .translation("config.tinkerslevellingaddon.general.staff_slot_gaining_method")
         .defineEnum("staffSlotGainingMethod", Config.GainingMethod.PREDEFINED_ORDER, EnumGetMethod.NAME_IGNORECASE, Config.GainingMethod.values());
      toolsSlotGainingMethod = builder.comment("Method of gaining modifier slots for tools.")
         .translation("config.tinkerslevellingaddon.general.tools_slot_gaining_method")
         .defineEnum("toolsSlotGainingMethod", Config.GainingMethod.PREDEFINED_ORDER, EnumGetMethod.NAME_IGNORECASE, Config.GainingMethod.values());
      slotsConfig(builder);
      armorStatGainingMethod = builder.comment("Method of gaining stats for armor.")
         .translation("config.tinkerslevellingaddon.general.armor_stat_gaining_method")
         .defineEnum("armorStatGainingMethod", Config.GainingMethod.NONE, EnumGetMethod.NAME_IGNORECASE, Config.GainingMethod.values());
      rangedStatGainingMethod = builder.comment("Method of gaining stats for tools.")
         .translation("config.tinkerslevellingaddon.general.ranged_stat_gaining_method")
         .defineEnum("rangedStatGainingMethod", Config.GainingMethod.NONE, EnumGetMethod.NAME_IGNORECASE, Config.GainingMethod.values());
      shieldStatGainingMethod = builder.comment("Method of gaining stats for shields.")
         .translation("config.tinkerslevellingaddon.general.shield_stat_gaining_method")
         .defineEnum("shieldStatGainingMethod", Config.GainingMethod.NONE, EnumGetMethod.NAME_IGNORECASE, Config.GainingMethod.values());
      staffStatGainingMethod = builder.comment("Method of gaining stats for staffs.")
         .translation("config.tinkerslevellingaddon.general.staff_stat_gaining_method")
         .defineEnum("staffStatGainingMethod", Config.GainingMethod.NONE, EnumGetMethod.NAME_IGNORECASE, Config.GainingMethod.values());
      toolsStatGainingMethod = builder.comment("Method of gaining stats for tools.")
         .translation("config.tinkerslevellingaddon.general.tools_stat_gaining_method")
         .defineEnum("toolsStatGainingMethod", Config.GainingMethod.NONE, EnumGetMethod.NAME_IGNORECASE, Config.GainingMethod.values());
      statsConfig(builder);
      maxLevel = builder.comment("Maximum tool level that could be achieved. If set to 0 there is no upper limit.")
         .translation("config.tinkerslevellingaddon.general.max_level")
         .defineInRange("maxLevel", 5, 0, Integer.MAX_VALUE);
      baseExperience = builder.comment("Base amount of experience required to reach next level.")
         .translation("config.tinkerslevellingaddon.general.base_experience")
         .defineInRange("baseExperience", 500, 1, Integer.MAX_VALUE);
      requiredXpMultiplier = builder.comment("How much the amount of experience required to reach next level will be multiplied per level.")
         .translation("config.tinkerslevellingaddon.general.required_xp_multiplier")
         .defineInRange("requiredXpMultiplier", 2.0, 1.0, 10.0);
      broadToolRequiredXpMultiplier = builder.comment("Additional multiplier for broad tools for experience required to level up.")
         .translation("config.tinkerslevellingaddon.general.broad_tool_required_xp_multiplier")
         .defineInRange("broadToolRequiredXpMultiplier", 3.0, 1.0, 10.0);
      builder.pop();
   }

   private static void slotsConfig(Builder builder) {
      builder.comment("Modifier slots settings").push("slots");
      armorSlotTypeOrder = builder.comment(
            new String[]{
               "List of modifier slot types (in order) that will be awarded when leveling up armor. If level is higher than list size the mod will start over.",
               "If empty default order will be used (" + String.join(", ", DEFAULT_ARMOR_SLOTS_ORDER) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getArmorSlotTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.slots.armor_slot_type_order")
         .defineList("armorSlotTypeOrder", DEFAULT_ARMOR_SLOTS_ORDER, t -> ToolLevellingUtil.getArmorSlotTypes().contains(t));
      armorSlotTypeRandomPool = builder.comment(
            new String[]{
               "Set of modifier slot types from which random slot will be awarded when leveling up armor.",
               "If empty default pool will be used (" + String.join(", ", DEFAULT_ARMOR_SLOTS_RANDOM_POOL) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getArmorSlotTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.modifiers.armor_slot_type_random_pool")
         .defineList("armorSlotTypeRandomPool", DEFAULT_ARMOR_SLOTS_RANDOM_POOL, t -> ToolLevellingUtil.getArmorSlotTypes().contains(t));
      rangedSlotTypeOrder = builder.comment(
            new String[]{
               "List of modifier slot types (in order) that will be awarded when leveling up ranged weapons. If level is higher than list size the mod will start over.",
               "If empty default order will be used (" + String.join(", ", DEFAULT_RANGED_SLOTS_ORDER) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getToolSlotTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.slots.ranged_slot_type_order")
         .defineList("rangedSlotTypeOrder", DEFAULT_RANGED_SLOTS_ORDER, t -> ToolLevellingUtil.getRangedSlotTypes().contains(t));
      rangedSlotTypeRandomPool = builder.comment(
            new String[]{
               "Set of modifier slot types from which random slot will be awarded when leveling up ranged weapons.",
               "If empty default pool will be used ("
                  + String.join(", ", DEFAULT_RANGED_SLOTS_RANDOM_POOL)
                  + "). 80% chance for upgrade and 20% chance for ability.",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getToolSlotTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.slots.ranged_slot_type_random_pool")
         .defineList("rangedSlotTypeRandomPool", DEFAULT_RANGED_SLOTS_RANDOM_POOL, t -> ToolLevellingUtil.getRangedSlotTypes().contains(t));
      shieldSlotTypeOrder = builder.comment(
            new String[]{
               "List of modifier slot types (in order) that will be awarded when leveling up shields. If level is higher than list size the mod will start over.",
               "If empty default order will be used (" + String.join(", ", DEFAULT_SHIELD_SLOTS_ORDER) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getShieldSlotTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.slots.shield_slot_type_order")
         .defineList("shieldSlotTypeOrder", DEFAULT_SHIELD_SLOTS_ORDER, t -> ToolLevellingUtil.getShieldSlotTypes().contains(t));
      shieldSlotTypeRandomPool = builder.comment(
            new String[]{
               "Set of modifier slot types from which random slot will be awarded when leveling up shields.",
               "If empty default pool will be used (" + String.join(", ", DEFAULT_SHIELD_SLOTS_RANDOM_POOL) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getShieldSlotTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.modifiers.shield_slot_type_random_pool")
         .defineList("shieldSlotTypeRandomPool", DEFAULT_SHIELD_SLOTS_RANDOM_POOL, t -> ToolLevellingUtil.getShieldSlotTypes().contains(t));
      staffSlotTypeOrder = builder.comment(
            new String[]{
               "List of modifier slot types (in order) that will be awarded when leveling up staffs. If level is higher than list size the mod will start over.",
               "If empty default order will be used (" + String.join(", ", DEFAULT_STAFF_SLOTS_ORDER) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getStaffSlotTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.slots.staff_slot_type_order")
         .defineList("staffSlotTypeOrder", DEFAULT_STAFF_SLOTS_ORDER, t -> ToolLevellingUtil.getStaffSlotTypes().contains(t));
      staffSlotTypeRandomPool = builder.comment(
            new String[]{
               "Set of modifier slot types from which random slot will be awarded when leveling up staffs.",
               "If empty default pool will be used (" + String.join(", ", DEFAULT_STAFF_SLOTS_RANDOM_POOL) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getArmorSlotTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.modifiers.staff_slot_type_random_pool")
         .defineList("staffSlotTypeRandomPool", DEFAULT_STAFF_SLOTS_RANDOM_POOL, t -> ToolLevellingUtil.getStaffSlotTypes().contains(t));
      toolsSlotTypeOrder = builder.comment(
            new String[]{
               "List of modifier slot types (in order) that will be awarded when leveling up tools. If level is higher than list size the mod will start over.",
               "If empty default order will be used (" + String.join(", ", DEFAULT_TOOLS_SLOTS_ORDER) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getToolSlotTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.slots.tools_slot_type_order")
         .defineList("toolsSlotTypeOrder", DEFAULT_TOOLS_SLOTS_ORDER, t -> ToolLevellingUtil.getToolSlotTypes().contains(t));
      toolsSlotTypeRandomPool = builder.comment(
            new String[]{
               "Set of modifier slot types from which random slot will be awarded when leveling up tools.",
               "If empty default pool will be used ("
                  + String.join(", ", DEFAULT_TOOLS_SLOTS_RANDOM_POOL)
                  + "). 80% chance for upgrade and 20% chance for ability.",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getToolSlotTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.slots.tools_slot_type_random_pool")
         .defineList("toolsSlotTypeRandomPool", DEFAULT_TOOLS_SLOTS_RANDOM_POOL, t -> ToolLevellingUtil.getToolSlotTypes().contains(t));
      builder.pop();
   }

   private static void statsConfig(Builder builder) {
      builder.comment("Stats settings").push("stats");
      armorStatTypeOrder = builder.comment(
            new String[]{
               "List of stat types (in order) that will be awarded when leveling up armor. If level is higher than list size the mod will start over.",
               "If empty default order will be used (" + String.join(", ", DEFAULT_ARMOR_STATS_ORDER) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getArmorStatTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.stats.armor_stat_type_order")
         .defineList("armorStatTypeOrder", DEFAULT_ARMOR_STATS_ORDER, t -> ToolLevellingUtil.getArmorStatTypes().contains(t));
      armorStatTypeRandomPool = builder.comment(
            new String[]{
               "Set of stat types from which random stat will be awarded when leveling up armor.",
               "If empty default pool will be used (" + String.join(", ", DEFAULT_ARMOR_STATS_RANDOM_POOL) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getArmorStatTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.stats.armor_stat_type_random_pool")
         .defineList("armorStatTypeRandomPool", DEFAULT_ARMOR_STATS_RANDOM_POOL, t -> ToolLevellingUtil.getArmorStatTypes().contains(t));
      armorStatsValuesConfig(builder);
      rangedStatTypeOrder = builder.comment(
            new String[]{
               "List of stat types (in order) that will be awarded when leveling up ranged weapons. If level is higher than list size the mod will start over.",
               "If empty default rotation will be used (" + String.join(", ", DEFAULT_RANGED_STATS_ORDER) + ").",
               "Possible values: " + String.join(", ", ToolLevellingUtil.getRangedStatTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.stats.ranged_stat_type_order")
         .defineList("rangedStatTypeRotation", DEFAULT_RANGED_STATS_ORDER, t -> ToolLevellingUtil.getRangedStatTypes().contains(t));
      rangedStatTypeRandomPool = builder.comment(
            new String[]{
               "Set of stat types from which random stat will be awarded when leveling up ranged weapons.",
               "If empty default pool will be used (" + String.join(", ", DEFAULT_RANGED_STATS_RANDOM_POOL) + ").",
               "Possible values: " + String.join(", ", ToolLevellingUtil.getRangedStatTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.stats.ranged_stat_type_random_pool")
         .defineList("rangedStatTypeRandomPool", DEFAULT_RANGED_STATS_RANDOM_POOL, t -> ToolLevellingUtil.getRangedStatTypes().contains(t));
      rangedStatsValuesConfig(builder);
      shieldStatTypeOrder = builder.comment(
            new String[]{
               "List of stat types (in order) that will be awarded when leveling up shields. If level is higher than list size the mod will start over.",
               "If empty default order will be used (" + String.join(", ", DEFAULT_SHIELD_STATS_ORDER) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getShieldStatTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.stats.shield_stat_type_order")
         .defineList("shieldStatTypeOrder", DEFAULT_SHIELD_STATS_ORDER, t -> ToolLevellingUtil.getShieldStatTypes().contains(t));
      shieldStatTypeRandomPool = builder.comment(
            new String[]{
               "Set of stat types from which random stat will be awarded when leveling up shields.",
               "If empty default pool will be used (" + String.join(", ", DEFAULT_SHIELD_STATS_RANDOM_POOL) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getShieldStatTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.stats.shield_stat_type_random_pool")
         .defineList("shieldStatTypeRandomPool", DEFAULT_SHIELD_STATS_RANDOM_POOL, t -> ToolLevellingUtil.getShieldStatTypes().contains(t));
      shieldStatsValuesConfig(builder);
      staffStatTypeOrder = builder.comment(
            new String[]{
               "List of stat types (in order) that will be awarded when leveling up staffs. If level is higher than list size the mod will start over.",
               "If empty default order will be used (" + String.join(", ", DEFAULT_STAFF_STATS_ORDER) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getStaffStatTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.stats.staff_stat_type_order")
         .defineList("staffStatTypeOrder", DEFAULT_STAFF_STATS_ORDER, t -> ToolLevellingUtil.getStaffStatTypes().contains(t));
      staffStatTypeRandomPool = builder.comment(
            new String[]{
               "Set of stat types from which random stat will be awarded when leveling up staffs.",
               "If empty default pool will be used (" + String.join(", ", DEFAULT_STAFF_STATS_RANDOM_POOL) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getStaffStatTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.stats.staff_stat_type_random_pool")
         .defineList("staffStatTypeRandomPool", DEFAULT_STAFF_STATS_RANDOM_POOL, t -> ToolLevellingUtil.getStaffStatTypes().contains(t));
      staffStatsValuesConfig(builder);
      toolsStatTypeOrder = builder.comment(
            new String[]{
               "List of stat types (in order) that will be awarded when leveling up tools. If level is higher than list size the mod will start over.",
               "If empty default order will be used (" + String.join(", ", DEFAULT_TOOLS_STATS_ORDER) + ").",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getToolStatTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.stats.tools_stat_type_order")
         .defineList("toolsStatTypeOrder", DEFAULT_TOOLS_STATS_ORDER, t -> ToolLevellingUtil.getToolStatTypes().contains(t));
      toolsStatTypeRandomPool = builder.comment(
            new String[]{
               "Set of stat types from which random stat will be awarded when leveling up tools.",
               "If empty default pool will be used (" + String.join(", ", DEFAULT_TOOLS_STATS_RANDOM_POOL) + "). 25% chance for every stat.",
               "Allowed values: " + String.join(", ", ToolLevellingUtil.getToolStatTypes())
            }
         )
         .translation("config.tinkerslevellingaddon.general.stats.tools_stat_type_random_pool")
         .defineList("toolsStatTypeRandomPool", DEFAULT_TOOLS_STATS_RANDOM_POOL, t -> ToolLevellingUtil.getToolStatTypes().contains(t));
      toolStatsValuesConfig(builder);
      builder.pop();
   }

   private static void armorStatsValuesConfig(Builder builder) {
      builder.comment("Armor stat values rewarded on level ups").push("armorValues");
      armorArmorValue = builder.translation("tooltip.tinkerslevellingaddon.stats.armor").defineInRange("armor", 0.25, 0.1, 10.0);
      armorArmorToughnessValue = builder.translation("tooltip.tinkerslevellingaddon.stats.armor_toughness").defineInRange("armorToughness", 0.1, 0.1, 10.0);
      armorDurabilityValue = builder.translation("tooltip.tinkerslevellingaddon.stats.durability").defineInRange("durability", 50, 1, 1000);
      armorKnockbackResistanceValue = builder.translation("tooltip.tinkerslevellingaddon.stats.knockback_resistance")
         .defineInRange("knockbackResistance", 0.1, 0.1, 1.0);
      builder.pop();
   }

   private static void rangedStatsValuesConfig(Builder builder) {
      builder.comment("Ranged weapons stat values rewarded on level ups").push("rangedValues");
      rangedAccuracyValue = builder.translation("tooltip.tinkerslevellingaddon.stat.accuracy").defineInRange("accuracy", 0.01, 0.01, 1.0);
      rangedAttackDamageValue = builder.translation("tooltip.tinkerslevellingaddon.stat.attack_damage").defineInRange("attackDamage", 0.5, 0.1, 10.0);
      rangedAttackSpeedValue = builder.translation("tooltip.tinkerslevellingaddon.stat.attack_speed").defineInRange("attackSpeed", 0.25, 0.1, 10.0);
      rangedDrawSpeedValue = builder.translation("tooltip.tinkerslevellingaddon.stat.draw_speed").defineInRange("drawSpeed", 0.1, 0.1, 10.0);
      rangedDurabilityValue = builder.translation("tooltip.tinkerslevellingaddon.stat.durability").defineInRange("durability", 30, 1, 1000);
      rangedProjectileDamageValue = builder.translation("tooltip.tinkerslevellingaddon.stat.projectile_damage")
         .defineInRange("projectileDamage", 0.1, 0.1, 10.0);
      rangedVelocityValue = builder.translation("tooltip.tinkerslevellingaddon.stat.velocity").defineInRange("velocity", 0.1, 0.1, 10.0);
      builder.pop();
   }

   private static void shieldStatsValuesConfig(Builder builder) {
      builder.comment("Shield stat values rewarded on level ups").push("shieldValues");
      shieldArmorToughnessValue = builder.translation("tooltip.tinkerslevellingaddon.stats.armor_toughness").defineInRange("armorToughness", 0.1, 0.1, 10.0);
      shieldBlockAmountValue = builder.translation("tooltip.tinkerslevellingaddon.stats.block_amount").defineInRange("blockAmount", 5.0, 0.1, 100.0);
      shieldBlockAngleValue = builder.translation("tooltip.tinkerslevellingaddon.stats.block_angle").defineInRange("blockAngle", 2, 1, 360);
      shieldDurabilityValue = builder.translation("tooltip.tinkerslevellingaddon.stat.durability").defineInRange("durability", 30, 1, 1000);
      shieldKnockbackResistanceValue = builder.translation("tooltip.tinkerslevellingaddon.stats.knockback_resistance")
         .defineInRange("knockbackResistance", 0.1, 0.1, 1.0);
      builder.pop();
   }

   private static void staffStatsValuesConfig(Builder builder) {
      builder.comment("Staff stat values rewarded on level ups").push("staffValues");
      staffAccuracyValue = builder.translation("tooltip.tinkerslevellingaddon.stat.accuracy").defineInRange("accuracy", 0.01, 0.01, 1.0);
      staffArmorValue = builder.translation("tooltip.tinkerslevellingaddon.stats.armor").defineInRange("armor", 0.25, 0.1, 10.0);
      staffDrawSpeedValue = builder.translation("tooltip.tinkerslevellingaddon.stat.draw_speed").defineInRange("drawSpeed", 0.1, 0.1, 10.0);
      staffDurabilityValue = builder.translation("tooltip.tinkerslevellingaddon.stat.durability").defineInRange("durability", 30, 1, 1000);
      staffProjectileDamageValue = builder.translation("tooltip.tinkerslevellingaddon.stat.projectile_damage")
         .defineInRange("projectileDamage", 0.1, 0.1, 10.0);
      staffVelocityValue = builder.translation("tooltip.tinkerslevellingaddon.stat.velocity").defineInRange("velocity", 0.1, 0.1, 10.0);
      builder.pop();
   }

   private static void toolStatsValuesConfig(Builder builder) {
      builder.comment("Tool stat values rewarded on level ups").push("toolValues");
      toolAttackDamageValue = builder.translation("tooltip.tinkerslevellingaddon.stats.attack_damage").defineInRange("attackDamage", 0.5, 0.1, 10.0);
      toolAttackSpeedValue = builder.translation("tooltip.tinkerslevellingaddon.stats.attack_speed").defineInRange("attackSpeed", 0.25, 0.1, 10.0);
      toolDurabilityValue = builder.translation("tooltip.tinkerslevellingaddon.stats.durability").defineInRange("durability", 50, 1, 1000);
      toolMiningSpeedValue = builder.translation("tooltip.tinkerslevellingaddon.stats.mining_speed").defineInRange("miningSpeed", 1.0, 0.1, 10.0);
      builder.pop();
   }

   private static void toolLevellingConfig(Builder builder) {
      builder.comment("Tool levelling settings").push("toolLevelling");
      damageDealt = builder.comment(
            new String[]{"Melee only!", "If true, base experience value gained for attacking is equal to damage dealt (rounded to whole number), otherwise 1."}
         )
         .translation("config.tinkerslevellingaddon.levelling.damage_dealt")
         .define("damageDealt", true);
      damageTaken = builder.comment("If true, base experience value gained for taking damage is equal to damage taken (rounded to whole number), otherwise 1.")
         .translation("config.tinkerslevellingaddon.levelling.damage_taken")
         .define("damageTaken", true);
      damageBlocked = builder.comment(
            "If true, base experience value gained for blocking damage is equal to damage blocked (rounded to whole number), otherwise 1."
         )
         .translation("config.tinkerslevellingaddon.levelling.damage_blocked")
         .define("damageBlocked", true);
      enablePvp = builder.comment("If true, allows to gain experience from dealing damage to or taking damage from other players.")
         .translation("config.tinkerslevellingaddon.levelling.enable_pvp")
         .define("pvp", true);
      flyingTime = builder.comment(
            new String[]{"The time (in ticks) of continuous flight using an elytra after which experience points will be awarded.", "20 ticks = 1 second"}
         )
         .translation("config.tinkerslevellingaddon.levelling.flying_time")
         .defineInRange("flyingTime", 300, 1, 1200);
      actionsConfig(builder);
      bonusesConfig(builder);
      builder.pop();
   }

   private static void actionsConfig(Builder builder) {
      builder.comment("If true, given action will yield tool experience.").push("actions");
      enableAttackingXp = builder.translation("config.tinkerslevellingaddon.levelling.attacking").define("attacking", true);
      enableBlockingDamageXp = builder.translation("config.tinkerslevellingaddon.levelling.blocking_damage").define("blockingDamage", true);
      enableBonkingXp = builder.translation("config.tinkerslevellingaddon.levelling.bonking").define("bonking", true);
      enableBrushingXp = builder.translation("config.tinkerslevellingaddon.levelling.brushing").define("brushing", true);
      enableBucketingXp = builder.comment("Placing fluids in world only, no container filling, no fluid pickup.")
         .translation("config.tinkerslevellingaddon.levelling.bucketing")
         .define("bucketing", true);
      enableFirestarterXp = builder.translation("config.tinkerslevellingaddon.levelling.firestarter").define("firestarter", true);
      enableFishingXp = builder.translation("config.tinkerslevellingaddon.levelling.fishing").define("fishing", true);
      enableFlamewakeXp = builder.translation("config.tinkerslevellingaddon.levelling.flamewake").define("flamewake", true);
      enableFlingingXp = builder.translation("config.tinkerslevellingaddon.levelling.flinging").define("flinging", true);
      enableFlyingXp = builder.translation("config.tinkerslevellingaddon.levelling.flying").define("flying", true);
      enableFrostWalkerXp = builder.translation("config.tinkerslevellingaddon.levelling.frost_walker").define("frost_walker", true);
      enableGlowingXp = builder.translation("config.tinkerslevellingaddon.levelling.glowing").define("glowing", true);
      enableGrapplingXp = builder.translation("config.tinkerslevellingaddon.levelling.grappling").define("grappling", true);
      enableHarvestingXp = builder.translation("config.tinkerslevellingaddon.levelling.harvesting").define("harvesting", true);
      enableMeltingXp = builder.translation("config.tinkerslevellingaddon.levelling.melting").define("melting", true);
      enableMiningXp = builder.translation("config.tinkerslevellingaddon.levelling.mining").define("mining", true);
      enablePathMakerXp = builder.translation("config.tinkerslevellingaddon.levelling.path_maker").define("path_maker", true);
      enablePathMakingXp = builder.translation("config.tinkerslevellingaddon.levelling.path_making").define("pathMaking", true);
      enablePlowingXp = builder.translation("config.tinkerslevellingaddon.levelling.plowing").define("plowing", true);
      enableScrappingXp = builder.translation("config.tinkerslevellingaddon.levelling.scrapping").define("scrapping", true);
      enableShearingXp = builder.translation("config.tinkerslevellingaddon.levelling.shearing").define("shearing", true);
      enableShootingXp = builder.comment("Applies to ranged weapons like longbows and crossbows.")
         .translation("config.tinkerslevellingaddon.levelling.shooting")
         .define("shooting", true);
      enableSlimeballXp = builder.translation("config.tinkerslevellingaddon.levelling.slimeball").define("slimeball", true);
      enableSlurpingXp = builder.translation("config.tinkerslevellingaddon.levelling.slurping").define("slurping", true);
      enableSnowdriftXp = builder.translation("config.tinkerslevellingaddon.levelling.snowdrift").define("snowdrift", true);
      enableSpillingXp = builder.translation("config.tinkerslevellingaddon.levelling.spilling").define("spilling", true);
      enableSpittingXp = builder.translation("config.tinkerslevellingaddon.levelling.spitting").define("spitting", true);
      enableSplashingXp = builder.translation("config.tinkerslevellingaddon.levelling.splashing").define("splashing", true);
      enableSpringingXp = builder.translation("config.tinkerslevellingaddon.levelling.springing").define("springing", true);
      enableStrippingXp = builder.translation("config.tinkerslevellingaddon.levelling.stripping").define("stripping", true);
      enableTakingDamageXp = builder.comment("Applies to armor only.")
         .translation("config.tinkerslevellingaddon.levelling.taking_damage")
         .define("takingDamage", true);
      enableThornsXp = builder.comment("Applies to armor only. Thorns modifier gives 15% chance per level to gain experience.")
         .translation("config.tinkerslevellingaddon.levelling.thorns")
         .define("thorns", true);
      enableTillingXp = builder.translation("config.tinkerslevellingaddon.levelling.tilling").define("tilling", true);
      enableWarpingXp = builder.translation("config.tinkerslevellingaddon.levelling.warping").define("warping", true);
      enableWaxingOffXp = builder.translation("config.tinkerslevellingaddon.levelling.waxing_off").define("waxingOff", true);
      builder.pop();
   }

   private static void bonusesConfig(Builder builder) {
      builder.comment("Amount of bonus experience for performing actions. If set to 0 there is no bonus experience.").push("bonuses");
      bonusAttackingXp = builder.translation("config.tinkerslevellingaddon.levelling.attacking").defineInRange("attacking", 0, 0, Integer.MAX_VALUE);
      bonusBlockingDamageXp = builder.translation("config.tinkerslevellingaddon.levelling.blocking_damage")
         .defineInRange("blockingDamage", 0, 0, Integer.MAX_VALUE);
      bonusBonkingXp = builder.translation("config.tinkerslevellingaddon.levelling.bonking").defineInRange("bonking", 0, 0, Integer.MAX_VALUE);
      bonusBrushingXp = builder.translation("config.tinkerslevellingaddon.levelling.brushing").defineInRange("brushing", 0, 0, Integer.MAX_VALUE);
      bonusBucketingXp = builder.translation("config.tinkerslevellingaddon.levelling.bucketing").defineInRange("bucketing", 0, 0, Integer.MAX_VALUE);
      bonusFirestarterXp = builder.translation("config.tinkerslevellingaddon.levelling.firestarter").defineInRange("firestarter", 0, 0, Integer.MAX_VALUE);
      bonusFishingXp = builder.translation("config.tinkerslevellingaddon.levelling.fishing").defineInRange("fishing", 0, 0, Integer.MAX_VALUE);
      bonusFlamewakeXp = builder.translation("config.tinkerslevellingaddon.levelling.flamewake").defineInRange("flamewake", 0, 0, Integer.MAX_VALUE);
      bonusFlingingXp = builder.translation("config.tinkerslevellingaddon.levelling.flinging").defineInRange("flinging", 0, 0, Integer.MAX_VALUE);
      bonusFlyingXp = builder.translation("config.tinkerslevellingaddon.levelling.flying").defineInRange("flying", 0, 0, Integer.MAX_VALUE);
      bonusFrostWalkerXp = builder.translation("config.tinkerslevellingaddon.levelling.frost_walker").defineInRange("frost_walker", 0, 0, Integer.MAX_VALUE);
      bonusGlowingXp = builder.translation("config.tinkerslevellingaddon.levelling.glowing").defineInRange("glowing", 0, 0, Integer.MAX_VALUE);
      bonusGrapplingXp = builder.translation("config.tinkerslevellingaddon.levelling.grappling").defineInRange("grappling", 0, 0, Integer.MAX_VALUE);
      bonusHarvestingXp = builder.translation("config.tinkerslevellingaddon.levelling.harvesting").defineInRange("harvesting", 0, 0, Integer.MAX_VALUE);
      bonusMeltingXp = builder.translation("config.tinkerslevellingaddon.levelling.melting").defineInRange("melting", 0, 0, Integer.MAX_VALUE);
      bonusMiningXp = builder.translation("config.tinkerslevellingaddon.levelling.mining").defineInRange("mining", 0, 0, Integer.MAX_VALUE);
      bonusPathMakerXp = builder.translation("config.tinkerslevellingaddon.levelling.path_maker").defineInRange("path_maker", 0, 0, Integer.MAX_VALUE);
      bonusPathMakingXp = builder.translation("config.tinkerslevellingaddon.levelling.path_making").defineInRange("pathMaking", 0, 0, Integer.MAX_VALUE);
      bonusPlowingXp = builder.translation("config.tinkerslevellingaddon.levelling.plowing").defineInRange("plowing", 0, 0, Integer.MAX_VALUE);
      bonusScrappingXp = builder.translation("config.tinkerslevellingaddon.levelling.scrapping").defineInRange("scrapping", 0, 0, Integer.MAX_VALUE);
      bonusShearingXp = builder.translation("config.tinkerslevellingaddon.levelling.shearing").defineInRange("shearing", 0, 0, Integer.MAX_VALUE);
      bonusShootingXp = builder.translation("config.tinkerslevellingaddon.levelling.shooting").defineInRange("shooting", 0, 0, Integer.MAX_VALUE);
      bonusSlimeballXp = builder.translation("config.tinkerslevellingaddon.levelling.slimeball").defineInRange("slimeball", 0, 0, Integer.MAX_VALUE);
      bonusSlurpingXp = builder.translation("config.tinkerslevellingaddon.levelling.slurping").defineInRange("slurping", 0, 0, Integer.MAX_VALUE);
      bonusSnowdriftXp = builder.translation("config.tinkerslevellingaddon.levelling.snowdrift").defineInRange("snowdrift", 0, 0, Integer.MAX_VALUE);
      bonusSpillingXp = builder.translation("config.tinkerslevellingaddon.levelling.spilling").defineInRange("spilling", 0, 0, Integer.MAX_VALUE);
      bonusSpittingXp = builder.translation("config.tinkerslevellingaddon.levelling.spitting").defineInRange("spitting", 0, 0, Integer.MAX_VALUE);
      bonusSplashingXp = builder.translation("config.tinkerslevellingaddon.levelling.splashing").defineInRange("splashing", 0, 0, Integer.MAX_VALUE);
      bonusSpringingXp = builder.translation("config.tinkerslevellingaddon.levelling.springing").defineInRange("springing", 0, 0, Integer.MAX_VALUE);
      bonusStrippingXp = builder.translation("config.tinkerslevellingaddon.levelling.stripping").defineInRange("stripping", 0, 0, Integer.MAX_VALUE);
      bonusTakingDamageXp = builder.translation("config.tinkerslevellingaddon.levelling.taking_damage").defineInRange("takingDamage", 0, 0, Integer.MAX_VALUE);
      bonusThornsXp = builder.comment(
            new String[]{
               "As with Thorns damage calculations, this is the upper bound of the bonus experience that could be granted.",
               "For example, for the value of 3 (default), we get: 1 (base xp) + 0 to 3 (bonus xp) = 1 to 4 (result xp)"
            }
         )
         .translation("config.tinkerslevellingaddon.levelling.thorns")
         .defineInRange("thorns", 3, 0, Integer.MAX_VALUE);
      bonusTillingXp = builder.translation("config.tinkerslevellingaddon.levelling.tilling").defineInRange("tilling", 0, 0, Integer.MAX_VALUE);
      bonusWarpingXp = builder.translation("config.tinkerslevellingaddon.levelling.warping").defineInRange("warping", 0, 0, Integer.MAX_VALUE);
      bonusWaxingOffXp = builder.translation("config.tinkerslevellingaddon.levelling.waxing_off").defineInRange("waxingOff", 0, 0, Integer.MAX_VALUE);
      builder.pop();
   }

   private static void clientConfig(Builder builder) {
      builder.comment("Client only settings").push("client");
      enableLevelUpMessage = builder.comment("If true, shows chat message on tool level ups.")
         .translation("config.tinkerslevellingaddon.client.messages")
         .define("message", true);
      squashLevelPluses = builder.comment(
            new String[]{
               "If true, uses alternative level name suffix for high level tools.",
               "Instead of appending '+' signs for each consecutive level appearance, +1, +2, +3, etc. notation will be used."
            }
         )
         .translation("config.tinkerslevellingaddon.client.squash_pluses")
         .define("squashPluses", false);
      levelUpSound = builder.comment("")
         .translation("config.tinkerslevellingaddon.client.sound")
         .defineEnum("sound", Config.LevelUpSound.SNARE_DRUM, EnumGetMethod.NAME_IGNORECASE, Config.LevelUpSound.values());
   }

   public static void init() {
      net.neoforged.fml.ModLoadingContext.get().getActiveContainer().registerConfig(Type.SERVER, SERVER_CONFIG);
      net.neoforged.fml.ModLoadingContext.get().getActiveContainer().registerConfig(Type.CLIENT, CLIENT_CONFIG);
   }

   public static List<String> getArmorSlotsOrder() {
      List<? extends String> slotsOrder = (List<? extends String>)armorSlotTypeOrder.get();
      if (slotsOrder.isEmpty()) {
         slotsOrder = DEFAULT_ARMOR_SLOTS_ORDER;
      }

      return java.util.List.copyOf(slotsOrder);
   }

   public static List<String> getArmorSlotsRandomPool() {
      List<? extends String> slotsRandomPool = (List<? extends String>)armorSlotTypeRandomPool.get();
      if (slotsRandomPool.isEmpty()) {
         slotsRandomPool = DEFAULT_ARMOR_SLOTS_RANDOM_POOL;
      }

      return java.util.List.copyOf(slotsRandomPool);
   }

   public static List<String> getArmorStatsOrder() {
      List<? extends String> statsOrder = (List<? extends String>)armorStatTypeOrder.get();
      if (statsOrder.isEmpty()) {
         statsOrder = DEFAULT_ARMOR_STATS_ORDER;
      }

      return java.util.List.copyOf(statsOrder);
   }

   public static List<String> getArmorStatsRandomPool() {
      List<? extends String> statsRandomPool = (List<? extends String>)armorStatTypeRandomPool.get();
      if (statsRandomPool.isEmpty()) {
         statsRandomPool = DEFAULT_ARMOR_STATS_RANDOM_POOL;
      }

      return java.util.List.copyOf(statsRandomPool);
   }

   public static List<String> getRangedSlotsOrder() {
      List<? extends String> slotsOrder = (List<? extends String>)rangedSlotTypeOrder.get();
      if (slotsOrder.isEmpty()) {
         slotsOrder = DEFAULT_RANGED_SLOTS_ORDER;
      }

      return java.util.List.copyOf(slotsOrder);
   }

   public static List<String> getRangedSlotsRandomPool() {
      List<? extends String> slotsRandomPool = (List<? extends String>)rangedSlotTypeRandomPool.get();
      if (slotsRandomPool.isEmpty()) {
         slotsRandomPool = DEFAULT_RANGED_SLOTS_RANDOM_POOL;
      }

      return java.util.List.copyOf(slotsRandomPool);
   }

   public static List<String> getRangedStatsOrder() {
      List<? extends String> statsOrder = (List<? extends String>)rangedStatTypeOrder.get();
      if (statsOrder.isEmpty()) {
         statsOrder = DEFAULT_RANGED_STATS_ORDER;
      }

      return java.util.List.copyOf(statsOrder);
   }

   public static List<String> getRangedStatsRandomPool() {
      List<? extends String> statsRandomPool = (List<? extends String>)rangedStatTypeRandomPool.get();
      if (statsRandomPool.isEmpty()) {
         statsRandomPool = DEFAULT_RANGED_STATS_RANDOM_POOL;
      }

      return java.util.List.copyOf(statsRandomPool);
   }

   public static List<String> getShieldSlotsOrder() {
      List<? extends String> slotsOrder = (List<? extends String>)shieldSlotTypeOrder.get();
      if (slotsOrder.isEmpty()) {
         slotsOrder = DEFAULT_SHIELD_SLOTS_ORDER;
      }

      return java.util.List.copyOf(slotsOrder);
   }

   public static List<String> getShieldSlotsRandomPool() {
      List<? extends String> slotsRandomPool = (List<? extends String>)shieldSlotTypeRandomPool.get();
      if (slotsRandomPool.isEmpty()) {
         slotsRandomPool = DEFAULT_SHIELD_SLOTS_RANDOM_POOL;
      }

      return java.util.List.copyOf(slotsRandomPool);
   }

   public static List<String> getShieldStatsOrder() {
      List<? extends String> statsOrder = (List<? extends String>)shieldStatTypeOrder.get();
      if (statsOrder.isEmpty()) {
         statsOrder = DEFAULT_SHIELD_STATS_ORDER;
      }

      return java.util.List.copyOf(statsOrder);
   }

   public static List<String> getShieldStatsRandomPool() {
      List<? extends String> statsRandomPool = (List<? extends String>)shieldStatTypeRandomPool.get();
      if (statsRandomPool.isEmpty()) {
         statsRandomPool = DEFAULT_SHIELD_STATS_RANDOM_POOL;
      }

      return java.util.List.copyOf(statsRandomPool);
   }

   public static List<String> getStaffSlotsOrder() {
      List<? extends String> slotsOrder = (List<? extends String>)staffSlotTypeOrder.get();
      if (slotsOrder.isEmpty()) {
         slotsOrder = DEFAULT_STAFF_SLOTS_ORDER;
      }

      return java.util.List.copyOf(slotsOrder);
   }

   public static List<String> getStaffSlotsRandomPool() {
      List<? extends String> slotsRandomPool = (List<? extends String>)staffSlotTypeRandomPool.get();
      if (slotsRandomPool.isEmpty()) {
         slotsRandomPool = DEFAULT_STAFF_SLOTS_RANDOM_POOL;
      }

      return java.util.List.copyOf(slotsRandomPool);
   }

   public static List<String> getStaffStatsOrder() {
      List<? extends String> statsOrder = (List<? extends String>)staffStatTypeOrder.get();
      if (statsOrder.isEmpty()) {
         statsOrder = DEFAULT_STAFF_STATS_ORDER;
      }

      return java.util.List.copyOf(statsOrder);
   }

   public static List<String> getStaffStatsRandomPool() {
      List<? extends String> statsRandomPool = (List<? extends String>)staffStatTypeRandomPool.get();
      if (statsRandomPool.isEmpty()) {
         statsRandomPool = DEFAULT_STAFF_STATS_RANDOM_POOL;
      }

      return java.util.List.copyOf(statsRandomPool);
   }

   public static List<String> getToolsSlotsOrder() {
      List<? extends String> slotsOrder = (List<? extends String>)toolsSlotTypeOrder.get();
      if (slotsOrder.isEmpty()) {
         slotsOrder = DEFAULT_TOOLS_SLOTS_ORDER;
      }

      return java.util.List.copyOf(slotsOrder);
   }

   public static List<String> getToolsSlotsRandomPool() {
      List<? extends String> slotsRandomPool = (List<? extends String>)toolsSlotTypeRandomPool.get();
      if (slotsRandomPool.isEmpty()) {
         slotsRandomPool = DEFAULT_TOOLS_SLOTS_RANDOM_POOL;
      }

      return java.util.List.copyOf(slotsRandomPool);
   }

   public static List<String> getToolsStatsOrder() {
      List<? extends String> statsOrder = (List<? extends String>)toolsStatTypeOrder.get();
      if (statsOrder.isEmpty()) {
         statsOrder = DEFAULT_TOOLS_STATS_ORDER;
      }

      return java.util.List.copyOf(statsOrder);
   }

   public static List<String> getToolsStatsRandomPool() {
      List<? extends String> statsRandomPool = (List<? extends String>)toolsStatTypeRandomPool.get();
      if (statsRandomPool.isEmpty()) {
         statsRandomPool = DEFAULT_TOOLS_STATS_RANDOM_POOL;
      }

      return java.util.List.copyOf(statsRandomPool);
   }

   public static double getArmorStatValue(FloatToolStat stat) {
      if (stat.equals(ToolStats.DURABILITY)) {
         return ((Integer)armorDurabilityValue.get()).intValue();
      } else if (stat.equals(ToolStats.ARMOR)) {
         return (Double)armorArmorValue.get();
      } else if (stat.equals(ToolStats.ARMOR_TOUGHNESS)) {
         return (Double)armorArmorToughnessValue.get();
      } else {
         return stat.equals(ToolStats.KNOCKBACK_RESISTANCE) ? (Double)armorKnockbackResistanceValue.get() : 0.0;
      }
   }

   public static double getRangedStatValue(FloatToolStat stat) {
      if (stat.equals(ToolStats.DRAW_SPEED)) {
         return (Double)rangedDrawSpeedValue.get();
      } else if (stat.equals(ToolStats.VELOCITY)) {
         return (Double)rangedVelocityValue.get();
      } else if (stat.equals(ToolStats.ACCURACY)) {
         return (Double)rangedAccuracyValue.get();
      } else if (stat.equals(ToolStats.PROJECTILE_DAMAGE)) {
         return (Double)rangedProjectileDamageValue.get();
      } else if (stat.equals(ToolStats.DURABILITY)) {
         return ((Integer)rangedDurabilityValue.get()).intValue();
      } else if (stat.equals(ToolStats.ATTACK_DAMAGE)) {
         return (Double)rangedAttackDamageValue.get();
      } else {
         return stat.equals(ToolStats.ATTACK_SPEED) ? (Double)rangedAttackSpeedValue.get() : 0.0;
      }
   }

   public static double getShieldStatValue(FloatToolStat stat) {
      if (stat.equals(ToolStats.ARMOR_TOUGHNESS)) {
         return (Double)shieldArmorToughnessValue.get();
      } else if (stat.equals(ToolStats.BLOCK_AMOUNT)) {
         return (Double)shieldBlockAmountValue.get();
      } else if (stat.equals(ToolStats.BLOCK_ANGLE)) {
         return ((Integer)shieldBlockAngleValue.get()).intValue();
      } else if (stat.equals(ToolStats.DURABILITY)) {
         return ((Integer)shieldDurabilityValue.get()).intValue();
      } else {
         return stat.equals(ToolStats.KNOCKBACK_RESISTANCE) ? (Double)shieldKnockbackResistanceValue.get() : 0.0;
      }
   }

   public static double getStaffStatValue(FloatToolStat stat) {
      if (stat.equals(ToolStats.DRAW_SPEED)) {
         return (Double)staffDrawSpeedValue.get();
      } else if (stat.equals(ToolStats.VELOCITY)) {
         return (Double)staffVelocityValue.get();
      } else if (stat.equals(ToolStats.ACCURACY)) {
         return (Double)staffAccuracyValue.get();
      } else if (stat.equals(ToolStats.PROJECTILE_DAMAGE)) {
         return (Double)staffProjectileDamageValue.get();
      } else if (stat.equals(ToolStats.DURABILITY)) {
         return ((Integer)staffDurabilityValue.get()).intValue();
      } else {
         return stat.equals(ToolStats.ARMOR) ? (Double)staffArmorValue.get() : 0.0;
      }
   }

   public static double getToolStatValue(FloatToolStat stat) {
      if (stat.equals(ToolStats.DURABILITY)) {
         return ((Integer)toolDurabilityValue.get()).intValue();
      } else if (stat.equals(ToolStats.ATTACK_DAMAGE)) {
         return (Double)toolAttackDamageValue.get();
      } else if (stat.equals(ToolStats.ATTACK_SPEED)) {
         return (Double)toolAttackSpeedValue.get();
      } else {
         return stat.equals(ToolStats.MINING_SPEED) ? (Double)toolMiningSpeedValue.get() : 0.0;
      }
   }

   static {
      Builder serverConfigBuilder = new Builder();
      generalConfig(serverConfigBuilder);
      toolLevellingConfig(serverConfigBuilder);
      SERVER_CONFIG = serverConfigBuilder.build();
      Builder clientConfigBuilder = new Builder();
      clientConfig(clientConfigBuilder);
      CLIENT_CONFIG = clientConfigBuilder.build();
   }

   public static enum GainingMethod {
      NONE,
      PREDEFINED_ORDER,
      RANDOM;
   }

   public static enum LevelUpSound {
      NONE(null),
      CHIME(Sounds.CHIME.getSound()),
      SNARE_DRUM(Sounds.SNARE_DRUM.getSound()),
      YAY(Sounds.YAY.getSound()),
      BONK(modernmods.modernfoundry.common.Sounds.BONK.getSound());

      private final SoundEvent soundEvent;

      private LevelUpSound(SoundEvent soundEvent) {
         this.soundEvent = soundEvent;
      }

      public SoundEvent getSoundEvent() {
         return this.soundEvent;
      }
   }
}
