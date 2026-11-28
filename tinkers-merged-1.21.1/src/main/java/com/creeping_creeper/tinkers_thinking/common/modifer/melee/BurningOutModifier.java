package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

public class BurningOutModifier extends Modifier implements MeleeHitModifierHook, ProjectileHitModifierHook, RedirectAfter, ModifierUtils {
   public int getPriority() {
      return 85;
   }

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.PROJECTILE_HIT);
   }

   public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      LivingEntity target = context.getLivingTarget();
      if (!context.isExtraAttack() && context.isFullyCharged() && target != null) {
         this.burn(context.getAttacker(), target, modifier.getLevel());
      }
   }

   public boolean onProjectileHitEntity(
      @NotNull ModifierNBT modifiers,
      ModDataNBT persistentData,
      @NotNull ModifierEntry modifier,
      @NotNull Projectile projectile,
      EntityHitResult hit,
      @Nullable LivingEntity attacker,
      @Nullable LivingEntity target,
      boolean notBlocked
   ) {
      if (target != null && attacker != null) {
         this.burn(attacker, target, modifier.getLevel());
      }

      return false;
   }

   private void burn(LivingEntity attacker, LivingEntity target, int level) {
      if (target.isAlive() && !target.fireImmune() && !target.hasEffect(MobEffects.FIRE_RESISTANCE)) {
         int fire = target.getRemainingFireTicks() / 20;
         if (fire > 0) {
            DamageSource source = TinkerDamageTypes.source(target.level().registryAccess(), DamageTypes.ON_FIRE, attacker);
            ToolAttackUtil.attackEntitySecondary(source, (float)(fire * 0.3 * level), target, target, true);
            target.invulnerableTime = 0;
            target.clearFire();
            this.particles(attacker.level(), target, ParticleTypes.SMOKE, 4);
            this.addEffect(target, MobEffects.FIRE_RESISTANCE, 80);
         }
      }
   }
}
