package com.creeping_creeper.tinkers_thinking.common.modifer.durability;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

public class DurableModifier extends Modifier implements ToolDamageModifierHook, TooltipModifierHook {
   private static final Component prefix = TinkersThinking.makeTranslation("modifier", "durable.chance");

   public int getPriority() {
      return 200;
   }

   protected void registerHooks(@NotNull Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE, ModifierHooks.TOOLTIP);
   }

   public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
      float chance = (float)Math.min(
         Math.pow(modifier.getLevel(), 0.5) * 5.0 * Math.pow(tool.getDamage() / (Float)tool.getStats().get(ToolStats.DURABILITY), 2.0), 0.9
      );
      int maxDamage = amount;

      for (int i = 0; i < maxDamage; i++) {
         if (RANDOM.nextFloat() < chance) {
            amount--;
         }
      }

      return amount;
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag
   ) {
      if (player != null) {
         float chance = (float)Math.min(
            Math.pow(modifier.getLevel(), 0.5) * 5.0 * Math.pow(tool.getDamage() / (Float)tool.getStats().get(ToolStats.DURABILITY), 2.0), 0.9
         );
         TooltipModifierHook.addPercentBoost(this, prefix, chance, tooltip);
      }
   }
}
