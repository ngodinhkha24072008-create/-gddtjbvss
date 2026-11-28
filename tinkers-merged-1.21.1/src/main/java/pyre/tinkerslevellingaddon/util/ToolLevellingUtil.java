package pyre.tinkerslevellingaddon.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import pyre.tinkerslevellingaddon.ImprovableModule;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.network.LevelUpPacket;
import pyre.tinkerslevellingaddon.network.Messages;
import modernmods.modernfoundry.common.TinkerTags.Items;
import modernmods.modernfoundry.library.tools.SlotType;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.tools.data.ModifierIds;

public class ToolLevellingUtil {
   public static final String NONE = "none";
   public static final String UPGRADE = "upgrade";
   public static final String ABILITY = "ability";
   public static final String SOUL = "soul";
   public static final String DEFENSE = "defense";
   public static final String DURABILITY = "durability";
   public static final String ATTACK_DAMAGE = "attackDamage";
   public static final String ATTACK_SPEED = "attackSpeed";
   public static final String MINING_SPEED = "miningSpeed";
   public static final String ARMOR = "armor";
   public static final String ARMOR_TOUGHNESS = "armorToughness";
   public static final String KNOCKBACK_RESISTANCE = "knockbackResistance";
   public static final String DRAW_SPEED = "drawSpeed";
   public static final String VELOCITY = "velocity";
   public static final String ACCURACY = "accuracy";
   public static final String PROJECTILE_DAMAGE = "projectileDamage";
   public static final String BLOCK_AMOUNT = "blockAmount";
   public static final String BLOCK_ANGLE = "blockAngle";
   private static final Map<String, SlotType> TOOL_SLOT_TYPES = new LinkedHashMap<>();
   private static final Map<String, SlotType> ARMOR_SLOT_TYPES = new LinkedHashMap<>();
   private static final Map<String, SlotType> ALL_SLOT_TYPES = new HashMap<>(TOOL_SLOT_TYPES);
   private static final Map<String, FloatToolStat> TOOL_STAT_TYPES = new LinkedHashMap<>();
   private static final Map<String, FloatToolStat> RANGED_STAT_TYPES = new LinkedHashMap<>();
   private static final Map<String, FloatToolStat> ARMOR_STAT_TYPES = new LinkedHashMap<>();
   private static final Map<String, FloatToolStat> STAFF_STAT_TYPES = new LinkedHashMap<>();
   private static final Map<String, FloatToolStat> SHIELD_STAT_TYPES = new LinkedHashMap<>();
   private static final Map<String, FloatToolStat> ALL_STAT_TYPES = new HashMap<>(TOOL_STAT_TYPES);
   private static final Random RANDOM = new Random();

   public static Set<String> getToolSlotTypes() {
      return TOOL_SLOT_TYPES.keySet();
   }

   public static Set<String> getRangedSlotTypes() {
      return TOOL_SLOT_TYPES.keySet();
   }

   public static Set<String> getArmorSlotTypes() {
      return ARMOR_SLOT_TYPES.keySet();
   }

   public static Set<String> getStaffSlotTypes() {
      return TOOL_SLOT_TYPES.keySet();
   }

   public static Set<String> getShieldSlotTypes() {
      return TOOL_SLOT_TYPES.keySet();
   }

   public static Set<String> getToolStatTypes() {
      return TOOL_STAT_TYPES.keySet();
   }

   public static Set<String> getRangedStatTypes() {
      return RANGED_STAT_TYPES.keySet();
   }

   public static Set<String> getArmorStatTypes() {
      return ARMOR_STAT_TYPES.keySet();
   }

   public static Set<String> getStaffStatTypes() {
      return STAFF_STAT_TYPES.keySet();
   }

   public static Set<String> getShieldStatTypes() {
      return SHIELD_STAT_TYPES.keySet();
   }

   public static boolean isSlotsLevellingEnabled(IToolContext context) {
      if (isShield(context)) {
         return Config.shieldSlotGainingMethod.get() != Config.GainingMethod.NONE;
      } else if (isStaff(context)) {
         return Config.staffSlotGainingMethod.get() != Config.GainingMethod.NONE;
      } else if (isArmor(context)) {
         return Config.armorSlotGainingMethod.get() != Config.GainingMethod.NONE;
      } else {
         return isRanged(context)
            ? Config.rangedSlotGainingMethod.get() != Config.GainingMethod.NONE
            : Config.toolsSlotGainingMethod.get() != Config.GainingMethod.NONE;
      }
   }

   public static boolean isStatsLevellingEnabled(IToolContext context) {
      if (isShield(context)) {
         return Config.shieldStatGainingMethod.get() != Config.GainingMethod.NONE;
      } else if (isStaff(context)) {
         return Config.staffStatGainingMethod.get() != Config.GainingMethod.NONE;
      } else if (isArmor(context)) {
         return Config.armorStatGainingMethod.get() != Config.GainingMethod.NONE;
      } else {
         return isRanged(context)
            ? Config.rangedStatGainingMethod.get() != Config.GainingMethod.NONE
            : Config.toolsStatGainingMethod.get() != Config.GainingMethod.NONE;
      }
   }

   public static boolean canPredictNextSlot(ToolStack tool) {
      if (isShield(tool)) {
         return Config.shieldSlotGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
      } else if (isStaff(tool)) {
         return Config.staffSlotGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
      } else if (isArmor(tool)) {
         return Config.armorSlotGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
      } else {
         return isRanged(tool)
            ? Config.rangedSlotGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER
            : Config.toolsSlotGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
      }
   }

   public static boolean canPredictNextStat(ToolStack tool) {
      if (isShield(tool)) {
         return Config.shieldStatGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
      } else if (isStaff(tool)) {
         return Config.staffStatGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
      } else if (isArmor(tool)) {
         return Config.armorStatGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
      } else {
         return isRanged(tool)
            ? Config.rangedStatGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER
            : Config.toolsStatGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
      }
   }

   public static String getSlot(IToolStackView tool, int level) {
      if (isShield(tool)) {
         return switch ((Config.GainingMethod)Config.shieldSlotGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getShieldSlotForLevel(level);
            case RANDOM -> getRandomShieldSlot();
         };
      } else if (isStaff(tool)) {
         return switch ((Config.GainingMethod)Config.staffSlotGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getStaffSlotForLevel(level);
            case RANDOM -> getRandomStaffSlot();
         };
      } else if (isArmor(tool)) {
         return switch ((Config.GainingMethod)Config.armorSlotGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getArmorSlotForLevel(level);
            case RANDOM -> getRandomArmorSlot();
         };
      } else if (isRanged(tool)) {
         return switch ((Config.GainingMethod)Config.rangedSlotGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getRangedSlotForLevel(level);
            case RANDOM -> getRandomRangedSlot();
         };
      } else {
         return switch ((Config.GainingMethod)Config.toolsSlotGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getToolSlotForLevel(level);
            case RANDOM -> getRandomToolSlot();
         };
      }
   }

   public static String getStat(IToolStackView tool, int level) {
      if (isShield(tool)) {
         return switch ((Config.GainingMethod)Config.shieldStatGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getShieldStatForLevel(level);
            case RANDOM -> getRandomShieldStat();
         };
      } else if (isStaff(tool)) {
         return switch ((Config.GainingMethod)Config.staffStatGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getStaffStatForLevel(level);
            case RANDOM -> getRandomStaffStat();
         };
      } else if (isArmor(tool)) {
         return switch ((Config.GainingMethod)Config.armorStatGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getArmorStatForLevel(level);
            case RANDOM -> getRandomArmorStat();
         };
      } else if (isRanged(tool)) {
         return switch ((Config.GainingMethod)Config.rangedStatGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getRangedStatForLevel(level);
            case RANDOM -> getRandomRangedStat();
         };
      } else {
         return switch ((Config.GainingMethod)Config.toolsStatGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getToolStatForLevel(level);
            case RANDOM -> getRandomToolStat();
         };
      }
   }

   public static double getStatValue(IToolContext tool, String stat) {
      return getStatValue(tool, ALL_STAT_TYPES.get(stat));
   }

   public static double getStatValue(IToolContext tool, FloatToolStat stat) {
      if (isShield(tool)) {
         return Config.getShieldStatValue(stat);
      } else if (isStaff(tool)) {
         return Config.getStaffStatValue(stat);
      } else if (isArmor(tool)) {
         return Config.getArmorStatValue(stat);
      } else {
         return isRanged(tool) ? Config.getRangedStatValue(stat) : Config.getToolStatValue(stat);
      }
   }

   public static List<SlotType> parseSlotsHistory(String historyString) {
      ArrayList<SlotType> result = new ArrayList<>();
      if (!historyString.isBlank()) {
         for (String slotString : historyString.split(";")) {
            if (!slotString.equals("none")) {
               SlotType slot = ALL_SLOT_TYPES.get(slotString);
               if (slot == null) {
                  throw new UnsupportedOperationException("Unsupported slot type " + slotString);
               }

               result.add(slot);
            }
         }
      }

      return result;
   }

   public static List<FloatToolStat> parseStatsHistory(String historyString) {
      ArrayList<FloatToolStat> result = new ArrayList<>();
      if (!historyString.isBlank()) {
         for (String statString : historyString.split(";")) {
            if (!statString.equals("none")) {
               FloatToolStat stat = ALL_STAT_TYPES.get(statString);
               if (stat == null) {
                  throw new UnsupportedOperationException("Unsupported stat type " + statString);
               }

               result.add(stat);
            }
         }
      }

      return result;
   }

   public static boolean canLevelUp(int level) {
      return (Integer)Config.maxLevel.get() == 0 || (Integer)Config.maxLevel.get() > level;
   }

   public static int getXpNeededForLevel(int level, boolean isBroadTool) {
      double base = ((Integer)Config.baseExperience.get()).intValue();
      double multiplier = (Double)Config.requiredXpMultiplier.get();
      double experienceNeeded = base * Math.pow(multiplier, Math.max(1, level) - 1.0);
      if (isBroadTool) {
         experienceNeeded *= Config.broadToolRequiredXpMultiplier.get();
      }

      return (int)Math.min(experienceNeeded, 2.147483647E9);
   }

   public static void addExperience(IToolStackView tool, int amount) {
      if (tool != null) {
         ModDataNBT data = tool.getPersistentData();
         int currentLevel = data.getInt(ImprovableModule.LEVEL_KEY);
         int currentExperience = data.getInt(ImprovableModule.EXPERIENCE_KEY) + amount;
         boolean isBroadTool = isBroadTool(tool);

         for (int experienceNeeded = getXpNeededForLevel(currentLevel + 1, isBroadTool);
            currentExperience >= experienceNeeded;
            data.putInt(ImprovableModule.PENDING_LEVEL_UP_KEY, currentLevel)
         ) {
            if (!canLevelUp(currentLevel)) {
               return;
            }

            data.putInt(ImprovableModule.LEVEL_KEY, ++currentLevel);
            currentExperience -= experienceNeeded;
            experienceNeeded = getXpNeededForLevel(currentLevel + 1, isBroadTool);
            String slotName = getSlot(tool, currentLevel);
            if (slotName != null) {
               appendHistory(ImprovableModule.SLOT_HISTORY_KEY, slotName, data);
            }

            String statName = getStat(tool, currentLevel);
            if (statName != null) {
               appendHistory(ImprovableModule.STAT_HISTORY_KEY, statName, data);
            }
         }

         data.putInt(ImprovableModule.EXPERIENCE_KEY, currentExperience);
      }
   }

   public static void processPendingLevelUp(ServerPlayer player, ItemStack itemStack) {
      if (ToolUtil.hasImprovable(itemStack)) {
         ToolStack tool = ToolStack.from(itemStack);
         ToolDataNBT data = tool.getPersistentData();
         if (data.contains(ImprovableModule.PENDING_LEVEL_UP_KEY)) {
            ToolStack toolCopy = tool.copy();
            toolCopy.getPersistentData().putInt(ImprovableModule.EXPERIENCE_KEY, 0);
            Messages.sendToPlayer(new LevelUpPacket(data.getInt(ImprovableModule.PENDING_LEVEL_UP_KEY), toolCopy.createStack().getDisplayName()), player);
            data.remove(ImprovableModule.PENDING_LEVEL_UP_KEY);
            tool.rebuildStats();
         }
      }
   }

   public static int getThornsBonus(IToolStackView tool) {
      int thornsLevel = tool.getModifierLevel(ModifierIds.thorns);
      if ((Boolean)Config.enableThornsXp.get() && thornsLevel != 0) {
         return RANDOM.nextFloat() < thornsLevel * 0.15F ? 1 + RANDOM.nextInt((Integer)Config.bonusThornsXp.get() + 1) : 0;
      } else {
         return 0;
      }
   }

   public static boolean isStaff(IToolContext tool) {
      return tool.hasTag(Items.STAFFS);
   }

   public static boolean isArmor(IToolContext tool) {
      return tool.hasTag(Items.ARMOR);
   }

   public static boolean isRanged(IToolContext tool) {
      return tool.hasTag(Items.RANGED);
   }

   public static boolean isShield(IToolContext tool) {
      return tool.hasTag(Items.SHIELDS);
   }

   public static boolean isBroadTool(IToolStackView tool) {
      return tool.getMaterials().size() > 3 || isStaff(tool);
   }

   public static TextColor getSlotColor(String slotName) {
      return ALL_SLOT_TYPES.get(slotName).getColor();
   }

   public static TextColor getStatColor(String statName) {
      return ALL_STAT_TYPES.get(statName).getColor();
   }

   private static String getToolSlotForLevel(int level) {
      List<String> toolsSlotsOrder = Config.getToolsSlotsOrder();
      return toolsSlotsOrder.get((level - 1) % toolsSlotsOrder.size());
   }

   private static String getRandomToolSlot() {
      List<String> toolsSlotsRandomPool = Config.getToolsSlotsRandomPool();
      return toolsSlotsRandomPool.get(RANDOM.nextInt(toolsSlotsRandomPool.size()));
   }

   private static String getRangedSlotForLevel(int level) {
      List<String> rangedSlotsOrder = Config.getRangedSlotsOrder();
      return rangedSlotsOrder.get((level - 1) % rangedSlotsOrder.size());
   }

   private static String getRandomRangedSlot() {
      List<String> rangedSlotsRandomPool = Config.getRangedSlotsRandomPool();
      return rangedSlotsRandomPool.get(RANDOM.nextInt(rangedSlotsRandomPool.size()));
   }

   private static String getArmorSlotForLevel(int level) {
      List<String> armorSlotsOrder = Config.getArmorSlotsOrder();
      return armorSlotsOrder.get((level - 1) % armorSlotsOrder.size());
   }

   private static String getRandomArmorSlot() {
      List<String> armorSlotsRandomPool = Config.getArmorSlotsRandomPool();
      return armorSlotsRandomPool.get(RANDOM.nextInt(armorSlotsRandomPool.size()));
   }

   private static String getStaffSlotForLevel(int level) {
      List<String> staffSlotsOrder = Config.getStaffSlotsOrder();
      return staffSlotsOrder.get((level - 1) % staffSlotsOrder.size());
   }

   private static String getRandomStaffSlot() {
      List<String> staffSlotsRandomPool = Config.getStaffSlotsRandomPool();
      return staffSlotsRandomPool.get(RANDOM.nextInt(staffSlotsRandomPool.size()));
   }

   private static String getShieldSlotForLevel(int level) {
      List<String> shieldSlotsOrder = Config.getShieldSlotsOrder();
      return shieldSlotsOrder.get((level - 1) % shieldSlotsOrder.size());
   }

   private static String getRandomShieldSlot() {
      List<String> shieldSlotsRandomPool = Config.getShieldSlotsRandomPool();
      return shieldSlotsRandomPool.get(RANDOM.nextInt(shieldSlotsRandomPool.size()));
   }

   private static String getToolStatForLevel(int level) {
      List<String> toolsStatsOrder = Config.getToolsStatsOrder();
      return toolsStatsOrder.get((level - 1) % toolsStatsOrder.size());
   }

   private static String getRandomToolStat() {
      List<String> toolsStatsRandomPool = Config.getToolsStatsRandomPool();
      return toolsStatsRandomPool.get(RANDOM.nextInt(toolsStatsRandomPool.size()));
   }

   private static String getRangedStatForLevel(int level) {
      List<String> rangedStatsRotation = Config.getRangedStatsOrder();
      return rangedStatsRotation.get((level - 1) % rangedStatsRotation.size());
   }

   private static String getRandomRangedStat() {
      List<String> rangedStatsRandomPool = Config.getRangedStatsRandomPool();
      return rangedStatsRandomPool.get(RANDOM.nextInt(rangedStatsRandomPool.size()));
   }

   private static String getArmorStatForLevel(int level) {
      List<String> armorStatsOrder = Config.getArmorStatsOrder();
      return armorStatsOrder.get((level - 1) % armorStatsOrder.size());
   }

   private static String getRandomArmorStat() {
      List<String> armorStatsRandomPool = Config.getArmorStatsRandomPool();
      return armorStatsRandomPool.get(RANDOM.nextInt(armorStatsRandomPool.size()));
   }

   private static String getStaffStatForLevel(int level) {
      List<String> staffStatsOrder = Config.getStaffStatsOrder();
      return staffStatsOrder.get((level - 1) % staffStatsOrder.size());
   }

   private static String getRandomStaffStat() {
      List<String> staffStatsRandomPool = Config.getStaffStatsRandomPool();
      return staffStatsRandomPool.get(RANDOM.nextInt(staffStatsRandomPool.size()));
   }

   private static String getShieldStatForLevel(int level) {
      List<String> shieldStatsOrder = Config.getShieldStatsOrder();
      return shieldStatsOrder.get((level - 1) % shieldStatsOrder.size());
   }

   private static String getRandomShieldStat() {
      List<String> shieldStatsRandomPool = Config.getShieldStatsRandomPool();
      return shieldStatsRandomPool.get(RANDOM.nextInt(shieldStatsRandomPool.size()));
   }

   private static void appendHistory(ResourceLocation historyKey, String value, ModDataNBT data) {
      String modifierHistory = data.getString(historyKey);
      modifierHistory = modifierHistory + value + ";";
      data.putString(historyKey, modifierHistory);
   }

   private ToolLevellingUtil() {
   }

   static {
      TOOL_SLOT_TYPES.put("none", null);
      TOOL_SLOT_TYPES.put("upgrade", SlotType.UPGRADE);
      TOOL_SLOT_TYPES.put("ability", SlotType.ABILITY);
      TOOL_SLOT_TYPES.put("soul", SlotType.SOUL);
      TOOL_SLOT_TYPES.put("defense", SlotType.DEFENSE);
      ARMOR_SLOT_TYPES.put("none", null);
      ARMOR_SLOT_TYPES.put("upgrade", SlotType.UPGRADE);
      ARMOR_SLOT_TYPES.put("ability", SlotType.ABILITY);
      ARMOR_SLOT_TYPES.put("soul", SlotType.SOUL);
      ARMOR_SLOT_TYPES.put("defense", SlotType.DEFENSE);
      ALL_SLOT_TYPES.putAll(ARMOR_SLOT_TYPES);
      TOOL_STAT_TYPES.put("none", null);
      TOOL_STAT_TYPES.put("durability", ToolStats.DURABILITY);
      TOOL_STAT_TYPES.put("attackDamage", ToolStats.ATTACK_DAMAGE);
      TOOL_STAT_TYPES.put("attackSpeed", ToolStats.ATTACK_SPEED);
      TOOL_STAT_TYPES.put("miningSpeed", ToolStats.MINING_SPEED);
      RANGED_STAT_TYPES.put("none", null);
      RANGED_STAT_TYPES.put("drawSpeed", ToolStats.DRAW_SPEED);
      RANGED_STAT_TYPES.put("velocity", ToolStats.VELOCITY);
      RANGED_STAT_TYPES.put("accuracy", ToolStats.ACCURACY);
      RANGED_STAT_TYPES.put("projectileDamage", ToolStats.PROJECTILE_DAMAGE);
      RANGED_STAT_TYPES.put("durability", ToolStats.DURABILITY);
      RANGED_STAT_TYPES.put("attackDamage", ToolStats.ATTACK_DAMAGE);
      RANGED_STAT_TYPES.put("attackSpeed", ToolStats.ATTACK_SPEED);
      ARMOR_STAT_TYPES.put("none", null);
      ARMOR_STAT_TYPES.put("durability", ToolStats.DURABILITY);
      ARMOR_STAT_TYPES.put("armor", ToolStats.ARMOR);
      ARMOR_STAT_TYPES.put("armorToughness", ToolStats.ARMOR_TOUGHNESS);
      ARMOR_STAT_TYPES.put("knockbackResistance", ToolStats.KNOCKBACK_RESISTANCE);
      STAFF_STAT_TYPES.put("none", null);
      STAFF_STAT_TYPES.put("drawSpeed", ToolStats.DRAW_SPEED);
      STAFF_STAT_TYPES.put("velocity", ToolStats.VELOCITY);
      STAFF_STAT_TYPES.put("accuracy", ToolStats.ACCURACY);
      STAFF_STAT_TYPES.put("projectileDamage", ToolStats.PROJECTILE_DAMAGE);
      STAFF_STAT_TYPES.put("durability", ToolStats.DURABILITY);
      STAFF_STAT_TYPES.put("armor", ToolStats.ARMOR);
      SHIELD_STAT_TYPES.put("none", null);
      SHIELD_STAT_TYPES.put("durability", ToolStats.DURABILITY);
      SHIELD_STAT_TYPES.put("armorToughness", ToolStats.ARMOR_TOUGHNESS);
      SHIELD_STAT_TYPES.put("knockbackResistance", ToolStats.KNOCKBACK_RESISTANCE);
      SHIELD_STAT_TYPES.put("blockAmount", ToolStats.BLOCK_AMOUNT);
      SHIELD_STAT_TYPES.put("blockAngle", ToolStats.BLOCK_ANGLE);
      ALL_STAT_TYPES.putAll(RANGED_STAT_TYPES);
      ALL_STAT_TYPES.putAll(ARMOR_STAT_TYPES);
      ALL_STAT_TYPES.putAll(STAFF_STAT_TYPES);
      ALL_STAT_TYPES.putAll(SHIELD_STAT_TYPES);
   }
}
