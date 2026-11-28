package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.shared.TinkerEffects;

public class RecalamityModifier extends Modifier implements MeleeDamageModifierHook, ProjectileHitModifierHook, ModifierUtils {
   private static final List<net.minecraft.core.Holder<MobEffect>> EFFECTS = new ArrayList<>(
      List.of(MobEffects.POISON, MobEffects.WITHER, TinkerEffects.bleeding, ModEffects.disintegration)
   );

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.PROJECTILE_HIT);
   }

   private float calamity(LivingEntity target, LivingEntity attacker, boolean a, float damage, int l) {
      net.minecraft.core.Holder<MobEffect> effect = ModEffects.disintegration;
      int x = 0;
      if (a) {
         switch (RANDOM.nextInt(4)) {
            case 1:
               effect = MobEffects.POISON;
               break;
            case 2:
               effect = MobEffects.WITHER;
               break;
            case 3:
               effect = TinkerEffects.bleeding;
         }

         target.addEffect(new MobEffectInstance(effect, target.hasEffect(effect) ? Objects.requireNonNull(target.getEffect(effect)).getDuration() + 80 * l : 80 * l));
         target.setLastHurtMob(attacker);
      } else {
         for (net.minecraft.core.Holder<MobEffect> i : EFFECTS) {
            x += target.hasEffect(i) ? 1 : 0;
         }
      }

      return (float)(damage * (1.0 + 0.1 * x));
   }

   public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      LivingEntity target = context.getLivingTarget();
      if (!context.isExtraAttack() && context.isFullyCharged() && target != null && target.isAlive()) {
         this.calamity(target, context.getAttacker(), this.reverse(tool), damage, modifier.getLevel());
      }

      return damage;
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
      if (target != null) {
         float damage = 1.0F;
         this.setPower(projectile, this.calamity(target, attacker, this.reverseProjectile(projectile), damage, modifier.getLevel()));
      }

      return false;
   }
}
