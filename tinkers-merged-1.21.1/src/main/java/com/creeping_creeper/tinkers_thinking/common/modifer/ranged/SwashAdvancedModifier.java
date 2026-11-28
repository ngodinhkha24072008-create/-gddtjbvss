package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectContext;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectManager;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffects;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.tools.entity.FluidEffectProjectile;

public class SwashAdvancedModifier extends Modifier implements ProjectileHitModifierHook {
   protected void registerHooks(Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.PROJECTILE_HIT);
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
      if (attacker != null && target != null && projectile instanceof FluidEffectProjectile spit) {
         Level level = target.level();
         int range = modifier.getLevel();
         List<Entity> list = level.getEntitiesOfClass(Entity.class, target.getBoundingBox().inflate(range, 0.25, range));
         list.remove(target);
         FluidStack fluid = spit.getFluid();
         FluidEffects recipe = FluidEffectManager.INSTANCE.find(fluid.getFluid());
         fluid.getFluid().getFluidType();
         if (recipe.hasEntityEffects()) {
            for (Entity entity : list) {
               if (!(entity instanceof ArmorStand stand && stand.isMarker())) {
                  recipe.applyToEntity(fluid, spit.getPower(), FluidEffectContext.builder(level).user(attacker).target(entity), FluidAction.EXECUTE);
               }
            }
         }
      }

      return false;
   }
}
