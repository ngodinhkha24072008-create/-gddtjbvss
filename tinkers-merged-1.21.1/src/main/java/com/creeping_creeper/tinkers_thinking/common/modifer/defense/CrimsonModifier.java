package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.common.TinkerTags.Items;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

public class CrimsonModifier extends Modifier implements MeleeHitModifierHook, RedirectAfter, ProjectileHitModifierHook, ModifyDamageModifierHook, ModifierUtils {
   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(
         this, new ModuleHook[]{ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.PROJECTILE_HIT, ModifierHooks.MODIFY_DAMAGE}
      );
   }

   private void apply(LivingEntity living, int level) {
      int x = -1;
      if (living.hasEffect(ModEffects.armor)) {
         x = living.getEffect(ModEffects.armor).getAmplifier();
      }

      this.addEffect(living, ModEffects.armor, 400, Math.min(x + 1, level * 2 + 2));
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      if (!context.isExtraAttack() && context.isFullyCharged()) {
         this.apply(context.getAttacker(), modifier.getLevel());
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
      if (attacker != null) {
         this.apply(attacker, modifier.getLevel());
      }

      return false;
   }

   public float modifyDamageTaken(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage
   ) {
      if (tool.hasTag(Items.ARMOR)) {
         this.apply(context.getEntity(), modifier.getLevel());
      }

      return amount;
   }
}
