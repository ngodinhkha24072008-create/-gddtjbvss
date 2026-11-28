package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class OverdisintegrateModifier extends Modifier implements MeleeHitModifierHook {
   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      LivingEntity living = context.getAttacker();
      if (!context.isExtraAttack() && context.isFullyCharged() && living.isAlive()) {
         LivingEntity target = context.getLivingTarget();
         int x = Math.min(4 * modifier.getLevel(), OverslimeModule.INSTANCE.getAmount(tool));
         float z = living.getHealth();
         float w = living.getMaxHealth();
         int y;
         if (z > w * 0.5) {
            y = 0;
         } else {
            y = z > w * 0.25 ? 1 : 3;
         }

         if (target != null && x > 0) {
            OverslimeModule.INSTANCE.removeAmount(tool, modifier, x);
            target.addEffect(new MobEffectInstance((MobEffect)ModEffects.disintegration.get(), 20 * x, y));
            target.setLastHurtMob(context.getAttacker());
         }
      }
   }
}
