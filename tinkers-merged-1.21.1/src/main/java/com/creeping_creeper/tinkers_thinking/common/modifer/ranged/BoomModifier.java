package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ScheduledProjectileTaskModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.utils.Schedule.Scheduler;

public class BoomModifier extends Modifier implements ScheduledProjectileTaskModifierHook, ProjectileHitModifierHook {
   protected void registerHooks(Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.SCHEDULE_PROJECTILE_TASK, ModifierHooks.PROJECTILE_HIT);
   }

   public void scheduleProjectileTask(
      IToolStackView tool,
      ModifierEntry modifier,
      ItemStack ammo,
      Projectile projectile,
      @Nullable AbstractArrow arrow,
      ModDataNBT persistentData,
      Scheduler scheduler
   ) {
      scheduler.add(0, 200);
   }

   public void onScheduledProjectileTask(
      IToolStackView tool, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, int task
   ) {
      if (task == 0) {
         this.boom(projectile, modifier.getLevel());
      }
   }

   public boolean onProjectileHitEntity(
      ModifierNBT modifiers,
      ModDataNBT persistentData,
      ModifierEntry modifier,
      Projectile projectile,
      EntityHitResult hit,
      @javax.annotation.Nullable LivingEntity attacker,
      @javax.annotation.Nullable LivingEntity target,
      boolean notBlocked
   ) {
      if (target != null && attacker != null) {
         target.setLastHurtMob(attacker);
      }

      this.boom(projectile, modifier.getLevel());
      return false;
   }

   private void boom(Projectile projectile, int level) {
      projectile.level().explode(projectile, projectile.getX(), projectile.getY(), projectile.getZ(), level, ExplosionInteraction.MOB);
      projectile.discard();
   }
}
