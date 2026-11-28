package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import javax.annotation.Nullable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

public class CataclysmModifier extends Modifier implements MeleeHitModifierHook, ProjectileHitModifierHook {
   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.PROJECTILE_HIT);
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      if (!context.isExtraAttack() && context.isFullyCharged()) {
         LivingEntity target = context.getLivingTarget();
         if (target != null && RANDOM.nextFloat() > modifier.getLevel() * 0.15) {
            target.addEffect(new MobEffectInstance(ModEffects.cataclysm, 600, modifier.getLevel()));
         }
      }
   }

   public boolean onProjectileHitEntity(
      @NotNull ModifierNBT modifiers,
      ModDataNBT persistentData,
      @NotNull ModifierEntry modifier,
      @NotNull Projectile projectile,
      EntityHitResult hit,
      @Nullable LivingEntity attacker,
      @Nullable LivingEntity target
   ) {
      if (target != null && RANDOM.nextFloat() > modifier.getLevel() * 0.15) {
         target.addEffect(new MobEffectInstance(ModEffects.cataclysm, 600, modifier.getLevel()));
      }

      return false;
   }
}
