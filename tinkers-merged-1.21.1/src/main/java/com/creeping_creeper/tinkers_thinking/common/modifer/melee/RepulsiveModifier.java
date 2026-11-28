package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
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
import modernmods.modernfoundry.shared.TinkerEffects;
import modernmods.modernfoundry.tools.modifiers.effect.RepulsiveEffect;

public class RepulsiveModifier extends Modifier implements MeleeHitModifierHook, RedirectAfter, ProjectileHitModifierHook, ModifyDamageModifierHook {
   protected void registerHooks(@NotNull Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(
         this, new ModuleHook[]{ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.PROJECTILE_HIT, ModifierHooks.MODIFY_DAMAGE}
      );
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      if (context.getLivingTarget() != null && !context.isExtraAttack()) {
         ((RepulsiveEffect)TinkerEffects.repulsive.get()).apply(context.getLivingTarget(), 15, modifier.getLevel() * 2 - 1);
      }
   }

   public boolean onProjectileHitEntity(
      @NotNull ModifierNBT modifiers,
      ModDataNBT persistentData,
      @NotNull ModifierEntry modifier,
      Projectile projectile,
      EntityHitResult hit,
      @Nullable LivingEntity attacker,
      @Nullable LivingEntity target,
      boolean notBlocked
   ) {
      if (target != null) {
         ((RepulsiveEffect)TinkerEffects.repulsive.get()).apply(target, 15, modifier.getLevel() * 2 - 1);
      }

      return false;
   }

   public float modifyDamageTaken(
      @NotNull IToolStackView tool,
      ModifierEntry modifier,
      EquipmentContext context,
      @NotNull EquipmentSlot slotType,
      DamageSource source,
      float amount,
      boolean isDirectDamage
   ) {
      if (tool.hasTag(Items.ARMOR)) {
         ((RepulsiveEffect)TinkerEffects.repulsive.get()).apply(context.getEntity(), 15, modifier.getLevel() * 2 - 1);
      }

      return amount;
   }
}
