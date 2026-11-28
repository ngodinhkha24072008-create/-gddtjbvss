package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import net.minecraft.world.entity.ai.attributes.Attributes;
import com.creeping_creeper.tinkers_thinking.data.ModModifierIds;
import java.util.Objects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.shared.TinkerEffects;
import modernmods.modernfoundry.tools.modifiers.effect.RepulsiveEffect;

public class FallingAttackModifier extends Modifier implements MeleeDamageModifierHook, RedirectAfter {
   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.MONSTER_MELEE_DAMAGE);
   }

   public float getMeleeDamage(IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      float h = context.getAttacker().fallDistance;
      int p = 3 + tool.getModifierLevel(ModModifierIds.DensityAdvanced);
      float x = (float)(Objects.requireNonNull(context.getAttacker().getAttribute(Attributes.GRAVITY)).getValue() / 0.08);
      if (!context.isExtraAttack() && context.isFullyCharged() && x > 0.0F) {
         damage += (float)((Math.min(6.0F, 2.0F * h) + Math.min(8.0F, h) + (1.0 + 0.5 * p) * h) * x);
         context.getAttacker().resetFallDistance();
         if (context.getAttacker().isFallFlying()) {
            ((RepulsiveEffect)TinkerEffects.repulsive.get()).apply(context.getAttacker(), 50, h > 5.0F ? 3 : 1);
         }
      }

      return damage;
   }
}
