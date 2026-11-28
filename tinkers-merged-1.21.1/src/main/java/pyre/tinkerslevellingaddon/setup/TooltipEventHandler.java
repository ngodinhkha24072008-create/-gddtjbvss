package pyre.tinkerslevellingaddon.setup;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import pyre.tinkerslevellingaddon.ImprovableModule;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.util.ModUtil;
import pyre.tinkerslevellingaddon.util.ToolLevellingUtil;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.helper.TooltipUtil;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

@EventBusSubscriber(
   modid = "tinkerslevellingaddon",
   value = {Dist.CLIENT},
   bus = Bus.GAME
)
public class TooltipEventHandler {
   private static final Component TOOLTIP_HOLD_ALT = ModUtil.makeTranslation(
      "tooltip", "hold_alt", ModUtil.makeTranslation("key", "alt", ImprovableModule.IMPROVABLE_MODIFIER_COLOR).withStyle(s -> s.withItalic(true))
   );
   private static final Component TOOLTIP_MODIFIERS_GAINED = ModUtil.makeTranslation("tooltip", "info.slots", ImprovableModule.IMPROVABLE_MODIFIER_COLOR)
      .withStyle(s -> s.withUnderlined(true));
   private static final Component TOOLTIP_STATS_GAINED = ModUtil.makeTranslation("tooltip", "info.stats", ImprovableModule.IMPROVABLE_MODIFIER_COLOR)
      .withStyle(s -> s.withUnderlined(true));
   private static final Component TOOLTIP_NEXT_LEVEL = ModUtil.makeTranslation("tooltip", "info.next_level", ImprovableModule.IMPROVABLE_MODIFIER_COLOR)
      .withStyle(s -> s.withUnderlined(true));

   @SubscribeEvent
   static void onTooltipEvent(ItemTooltipEvent event) {
      KeyModifier activeModifierKey = KeyModifier.getActiveModifier();
      if (event.getEntity() != null && activeModifierKey != KeyModifier.CONTROL && activeModifierKey != KeyModifier.SHIFT) {
         ItemStack stack = event.getItemStack();
         if (ModifierUtil.getModifierLevel(stack, AddonModifierIds.IMPROVABLE) > 0) {
            for (int i = event.getToolTip().size() - 1; i >= 0; i--) {
               if (event.getToolTip().get(i) == TooltipUtil.TOOLTIP_HOLD_SHIFT || event.getToolTip().get(i) == TooltipUtil.TOOLTIP_HOLD_CTRL) {
                  event.getToolTip().add(i + 1, TOOLTIP_HOLD_ALT);
                  break;
               }
            }

            List<Component> infoEntries = new ArrayList<>();
            ToolStack tool = ToolStack.from(stack);
            if (activeModifierKey == KeyModifier.ALT) {
               infoEntries.add((Component)event.getToolTip().get(0));
               infoEntries.addAll(prepareLevelInfo(tool));
               event.getToolTip().clear();
               event.getToolTip().addAll(infoEntries);
            } else {
               infoEntries = prepareGeneralInfo(tool);

               for (int ix = 2; ix < infoEntries.size() + 2; ix++) {
                  event.getToolTip().add(ix, infoEntries.get(ix - 2));
               }
            }
         }
      }
   }

   private static List<Component> prepareGeneralInfo(ToolStack tool) {
      List<Component> infoEntries = new ArrayList<>();
      int level = tool.getPersistentData().getInt(ImprovableModule.LEVEL_KEY);
      MutableComponent fullLevelName = ModUtil.makeTranslation(
         "tooltip", "level.name", ChatFormatting.GRAY, getLevelName(level), ModUtil.makeText(level, ChatFormatting.GRAY)
      );
      infoEntries.add(ModUtil.makeTranslation("tooltip", "level", fullLevelName));
      if (ToolLevellingUtil.canLevelUp(level)) {
         MutableComponent xp = ModUtil.makeText(tool.getPersistentData().getInt(ImprovableModule.EXPERIENCE_KEY), ChatFormatting.GOLD);
         MutableComponent xpNeeded = ModUtil.makeText(
            ToolLevellingUtil.getXpNeededForLevel(level + 1, ToolLevellingUtil.isBroadTool(tool)), ChatFormatting.GOLD
         );
         MutableComponent xpValue = ModUtil.makeTranslation("tooltip", "xp.value", ChatFormatting.GRAY, xp, xpNeeded);
         infoEntries.add(ModUtil.makeTranslation("tooltip", "xp", xpValue));
      }

      return infoEntries;
   }

   private static List<Component> prepareLevelInfo(ToolStack tool) {
      List<Component> infoEntries = new ArrayList<>();
      String modifierHistory = tool.getPersistentData().getString(ImprovableModule.SLOT_HISTORY_KEY);
      if (!modifierHistory.isBlank()) {
         Map<String, Long> gainedModifiers = Arrays.stream(modifierHistory.split(";"))
            .filter(s -> !s.equals("none"))
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
         infoEntries.add(TOOLTIP_MODIFIERS_GAINED);

         for (Entry<String, Long> entry : gainedModifiers.entrySet()) {
            MutableComponent value = ModUtil.makeText(entry.getValue(), ToolLevellingUtil.getSlotColor(entry.getKey()));
            infoEntries.add(ModUtil.makeTranslation("tooltip", "info.slots." + entry.getKey(), value));
         }
      }

      String statHistory = tool.getPersistentData().getString(ImprovableModule.STAT_HISTORY_KEY);
      if (!statHistory.isBlank()) {
         if (!infoEntries.isEmpty()) {
            infoEntries.add(Component.empty());
         }

         Map<String, Double> gainedStats = new LinkedHashMap<>();
         Arrays.stream(statHistory.split(";"))
            .filter(s -> !s.equals("none"))
            .sorted()
            .forEach(s -> gainedStats.merge(s, ToolLevellingUtil.getStatValue(tool, s), Double::sum));
         gainedStats.computeIfPresent("knockbackResistance", (k, v) -> v * 10.0);
         infoEntries.add(TOOLTIP_STATS_GAINED);

         for (Entry<String, Double> entry : gainedStats.entrySet()) {
            MutableComponent value = ModUtil.makeText(net.minecraft.world.item.component.ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(entry.getValue()), ToolLevellingUtil.getStatColor(entry.getKey()));
            infoEntries.add(ModUtil.makeTranslation("tooltip", "info.stats." + entry.getKey(), value));
         }
      }

      List<Component> nextLevelInfo = prepareNextLevelInfo(tool);
      if (!infoEntries.isEmpty() && !nextLevelInfo.isEmpty()) {
         infoEntries.add(Component.empty());
      }

      infoEntries.addAll(nextLevelInfo);
      return infoEntries;
   }

   private static List<Component> prepareNextLevelInfo(ToolStack tool) {
      List<Component> infoEntries = new ArrayList<>();
      int level = tool.getPersistentData().getInt(ImprovableModule.LEVEL_KEY);
      boolean canLevelUp = ToolLevellingUtil.canLevelUp(level);
      boolean knowNextSlot = ToolLevellingUtil.canPredictNextSlot(tool);
      boolean knowNextStat = ToolLevellingUtil.canPredictNextStat(tool);
      if (canLevelUp && (knowNextSlot || knowNextStat)) {
         infoEntries.add(TOOLTIP_NEXT_LEVEL);
         if (knowNextSlot) {
            String nextSlot = ToolLevellingUtil.getSlot(tool, level + 1);
            if (nextSlot.equals("none")) {
               infoEntries.add(ModUtil.makeTranslation("tooltip", "info.next_level.slot_none"));
            } else {
               MutableComponent slot = ModUtil.makeTranslation("tooltip", "slot." + nextSlot, ToolLevellingUtil.getSlotColor(nextSlot));
               infoEntries.add(ModUtil.makeTranslation("tooltip", "info.next_level.slot", slot));
            }
         }

         if (knowNextStat) {
            String nextStat = ToolLevellingUtil.getStat(tool, level + 1);
            if (nextStat.equals("none")) {
               infoEntries.add(ModUtil.makeTranslation("tooltip", "info.next_level.stat_none"));
            } else {
               double statValue = ToolLevellingUtil.getStatValue(tool, nextStat);
               if (nextStat.equals("knockbackResistance")) {
                  statValue *= 10.0;
               }

               MutableComponent value = ModUtil.makeText(net.minecraft.world.item.component.ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(statValue), ToolLevellingUtil.getStatColor(nextStat));
               MutableComponent name = ModUtil.makeTranslation("tooltip", "stat." + nextStat);
               infoEntries.add(ModUtil.makeTranslation("tooltip", "info.next_level.stat", value, name));
            }
         }
      }

      return infoEntries;
   }

   private static MutableComponent getLevelName(int level) {
      TextColor levelColor = getLevelColor(level);
      if (ModUtil.canTranslate("tooltip", "level." + level)) {
         return ModUtil.makeTranslation("tooltip", "level." + level, levelColor);
      } else {
         int i = 1;

         while (ModUtil.canTranslate("tooltip", "level." + i)) {
            i++;
         }

         int tier = level / i;
         String suffix = Config.squashLevelPluses.get() && level > 0 ? "+" + tier : "+".repeat(tier);
         return ModUtil.makeTranslation("tooltip", "level." + level % i, levelColor).append(ModUtil.makeText(suffix, levelColor));
      }
   }

   private static TextColor getLevelColor(int level) {
      float hue = 0.277777F * level;
      hue -= (int)hue;
      return TextColor.fromRgb(Color.HSBtoRGB(hue, 0.75F, 0.8F));
   }
}
