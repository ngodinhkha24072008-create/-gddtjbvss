package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class BattleAdvancedModifier extends Modifier implements MeleeDamageModifierHook {
   protected void registerHooks(Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
   }

   public float getMeleeDamage(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      Player player = context.getPlayerAttacker();
      if (player != null && player.isBlocking()) {
         float bonus = 0.005F * Math.min(1200, modifier.getLevel() * (tool.getItem().getUseDuration(player.getUseItem()) - player.getUseItemRemainingTicks()));
         damage *= 1.0F + bonus;
      }

      return damage;
   }
}
