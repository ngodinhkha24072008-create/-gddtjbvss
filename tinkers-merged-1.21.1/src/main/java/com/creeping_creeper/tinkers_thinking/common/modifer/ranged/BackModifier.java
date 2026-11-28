package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
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

public class BackModifier extends Modifier implements ScheduledProjectileTaskModifierHook, ProjectileHitModifierHook {
   ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath("tinkers_thinking", "back");

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
      scheduler.add(0, 40);
   }

   public void onScheduledProjectileTask(
      IToolStackView tool, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, int task
   ) {
      if (task == 0 && !persistentData.getBoolean(this.KEY)) {
         this.back(projectile);
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
      this.back(projectile);
      persistentData.putBoolean(this.KEY, true);
      return false;
   }

   public boolean onProjectileHitsBlock(
      ModifierNBT modifiers,
      ModDataNBT persistentData,
      ModifierEntry modifier,
      Projectile projectile,
      BlockHitResult hit,
      @javax.annotation.Nullable LivingEntity owner
   ) {
      this.back(projectile);
      persistentData.putBoolean(this.KEY, true);
      return false;
   }

   private void back(Projectile projectile) {
      Vec3 vec3 = projectile.getDeltaMovement();
      Vec3 newVec = new Vec3(-vec3.x, -vec3.y, -vec3.z);
      projectile.setDeltaMovement(newVec);
   }
}
