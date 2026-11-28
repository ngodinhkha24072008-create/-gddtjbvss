package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.entity.ProjectileWithPower;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

public record SharpCircumstanceModule(LevelingValue amount)
   implements ModifierModule,
   MeleeDamageModifierHook,
   RedirectAfter,
   ProjectileHitModifierHook,
   ModifyDamageModifierHook {
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(
      new ModuleHook[]{ModifierHooks.MELEE_DAMAGE, ModifierHooks.MONSTER_MELEE_DAMAGE, ModifierHooks.PROJECTILE_HIT, ModifierHooks.MODIFY_DAMAGE}
   );
   public static final RecordLoadable<SharpCircumstanceModule> LOADER = RecordLoadable.create(
      LevelingValue.LOADABLE.directField(SharpCircumstanceModule::amount), SharpCircumstanceModule::new
   );

   @NotNull
   public RecordLoadable<SharpCircumstanceModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public float getMeleeDamage(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      LivingEntity attacker = context.getAttacker();
      LivingEntity target = context.getLivingTarget();
      if (!context.isExtraAttack() && target != null && damage > 0.0F) {
         this.dealDamage(attacker, target, damage * modifier.getLevel() * this.amount().eachLevel());
      }

      return damage;
   }

   public boolean onProjectileHitEntity(
      ModifierNBT modifiers,
      ModDataNBT persistentData,
      ModifierEntry modifier,
      Projectile projectile,
      EntityHitResult hit,
      @Nullable LivingEntity attacker,
      @Nullable LivingEntity target,
      boolean notBlocked
   ) {
      if (attacker != null && attacker.isAlive() && target != null) {
         float damage = 0.0F;
         if (projectile instanceof AbstractArrow arrow) {
            damage = Mth.ceil(Mth.clamp(arrow.getDeltaMovement().length() * arrow.getBaseDamage(), 0.0, Float.MAX_VALUE));
            if (arrow.isCritArrow()) {
               damage = Math.min(TConstruct.RANDOM.nextFloat(damage / 2.0F + 2.0F) + (float)((long)damage), Float.MAX_VALUE);
            }
         } else if (projectile instanceof ProjectileWithPower withPower) {
            damage = Mth.ceil(Mth.clamp(projectile.getDeltaMovement().length() * withPower.getDamage() * 1.5, 0.0, Float.MAX_VALUE));
         }

         this.dealDamage(attacker, target, damage * modifier.getLevel() * this.amount().eachLevel());
         return target.isDeadOrDying();
      } else {
         return false;
      }
   }

   public float modifyDamageTaken(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float damage, boolean isDirectDamage
   ) {
      LivingEntity living = context.getEntity();
      LivingEntity attacker = (LivingEntity)source.getEntity();
      if (attacker != null && attacker.isAlive() && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
         this.dealDamage(living, attacker, damage * modifier.getLevel() * (this.amount().eachLevel() - 0.045F));
         if (living.isDeadOrDying()) {
            damage = 0.0F;
         }
      }

      return damage;
   }

   private float who(LivingEntity living) {
      return living.getHealth() / living.getMaxHealth();
   }

   private void dealDamage(LivingEntity a, LivingEntity b, float damage) {
      if (Float.compare(this.who(a), this.who(b)) >= 0) {
         DamageSource source = TinkerDamageTypes.source(a.level().registryAccess(), TinkerDamageTypes.BLEEDING, a);
         ToolAttackUtil.attackEntitySecondary(source, damage, b, b, true);
         b.invulnerableTime = 0;
      } else {
         DamageSource source = TinkerDamageTypes.source(b.level().registryAccess(), TinkerDamageTypes.BLEEDING, b);
         ToolAttackUtil.attackEntitySecondary(source, damage, a, a, true);
         a.invulnerableTime = 0;
      }
   }
}
