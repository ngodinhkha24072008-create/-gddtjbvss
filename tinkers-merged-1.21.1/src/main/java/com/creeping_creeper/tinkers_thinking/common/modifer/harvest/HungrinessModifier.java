package com.creeping_creeper.tinkers_thinking.common.modifer.harvest;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.common.TinkerTags.Items;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

public class HungrinessModifier extends Modifier implements ConditionalStatModifierHook, BreakSpeedModifierHook, TooltipModifierHook {
   private static final Component MINING_SPEED = TinkersThinking.makeTranslation("modifier", "hungriness.mining_speed");
   private static final Component VELOCITY = TinkersThinking.makeTranslation("modifier", "hungriness.velocity");

   public int getPriority() {
      return 55;
   }

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.CONDITIONAL_STAT, ModifierHooks.BREAK_SPEED, ModifierHooks.TOOLTIP);
   }

   public float modifyStat(IToolStackView tool, ModifierEntry modifier, LivingEntity living, FloatToolStat stat, float baseValue, float multiplier) {
      return stat == ToolStats.VELOCITY && living instanceof Player
         ? (float)(baseValue * (1.0 + (20 - ((Player)living).getFoodData().getFoodLevel()) * 0.02 * modifier.getLevel()))
         : baseValue;
   }

   public void onBreakSpeed(
      IToolStackView tool, ModifierEntry modifier, @NotNull BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier
   ) {
      if (isEffective && event.getEntity() != null) {
         event.setNewSpeed((float)(event.getNewSpeed() * (1.0 + (20 - event.getEntity().getFoodData().getFoodLevel()) * 0.02 * modifier.getLevel())));
      }
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag
   ) {
      boolean harvest = tool.hasTag(Items.HARVEST);
      if ((harvest || tool.hasTag(Items.RANGED)) && player != null) {
         Component prefix = harvest ? MINING_SPEED : VELOCITY;
         float boost = (float)((20 - player.getFoodData().getFoodLevel()) * 0.02 * modifier.getLevel());
         TooltipModifierHook.addPercentBoost(this, prefix, boost, tooltip);
      }
   }
}
