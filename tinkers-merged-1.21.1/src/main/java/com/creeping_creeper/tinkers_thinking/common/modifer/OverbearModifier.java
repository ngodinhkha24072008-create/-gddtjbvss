package com.creeping_creeper.tinkers_thinking.common.modifer;

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
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.library.utils.Util;

public class OverbearModifier extends Modifier implements ConditionalStatModifierHook, BreakSpeedModifierHook, MeleeDamageModifierHook, TooltipModifierHook {
   private static final Component Debuff = TinkersThinking.makeTranslation("modifier", "overbear.debuff");

   private boolean hasOverSlime(IToolStackView tool) {
      return 0 < OverslimeModule.INSTANCE.getAmount(tool);
   }

   protected void registerHooks(@NotNull Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, new ModuleHook[]{ModifierHooks.CONDITIONAL_STAT, ModifierHooks.BREAK_SPEED, ModifierHooks.MELEE_DAMAGE, ModifierHooks.TOOLTIP});
   }

   public float modifyStat(IToolStackView tool, ModifierEntry modifier, LivingEntity living, FloatToolStat stat, float baseValue, float multiplier) {
      return !this.hasOverSlime(tool) && stat == ToolStats.PROJECTILE_DAMAGE ? (float)(baseValue * (1.0 - 0.15 * modifier.getLevel())) : baseValue;
   }

   public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier, BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
      if (isEffective) {
         if (!this.hasOverSlime(tool)) {
            event.setNewSpeed((float)(event.getNewSpeed() * (1.0 - 0.15 * modifier.getLevel())));
         }
      }
   }

   public float getMeleeDamage(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      return !this.hasOverSlime(tool) ? (float)(damage * (1.0 - 0.15 * modifier.getLevel())) : damage;
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag
   ) {
      if (player != null) {
         float bonus = 0.0F;
         if (!this.hasOverSlime(tool)) {
            bonus = (float)(-0.15 * modifier.getLevel());
         }

         tooltip.add(this.applyStyle(Component.literal(Util.PERCENT_BOOST_FORMAT.format(bonus) + " ").append(Debuff)));
      }
   }
}
