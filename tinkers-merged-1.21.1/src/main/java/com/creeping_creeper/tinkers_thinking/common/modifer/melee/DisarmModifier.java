package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class DisarmModifier extends Modifier implements MeleeHitModifierHook, RedirectAfter, ModifierUtils {
   protected void registerHooks(@NotNull Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT);
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      if (!context.isExtraAttack() && context.isFullyCharged()) {
         LivingEntity target = context.getLivingTarget();
         if (target != null && !target.hasEffect(ModEffects.modifier_immune)) {
            this.addEffect(target, ModEffects.disarm, 160);
            this.addEffect(target, ModEffects.modifier_immune, 360 / modifier.getLevel());
         }
      }
   }
}
