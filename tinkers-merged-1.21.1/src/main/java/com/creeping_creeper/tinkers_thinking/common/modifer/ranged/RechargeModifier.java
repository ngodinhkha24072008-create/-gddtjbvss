package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

public class RechargeModifier extends Modifier implements ConditionalStatModifierHook, ProjectileLaunchModifierHook, ModifierUtils {
   protected void registerHooks(Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.CONDITIONAL_STAT, ModifierHooks.PROJECTILE_LAUNCH);
   }

   public float modifyStat(
      @NotNull IToolStackView tool, @NotNull ModifierEntry modifier, LivingEntity living, @NotNull FloatToolStat stat, float baseValue, float multiplier
   ) {
      if (this.reverse(tool)) {
         if (stat == ToolStats.DRAW_SPEED) {
            return (float)(baseValue * (1.0 - 0.15 * modifier.getLevel()));
         }

         if (stat == ToolStats.VELOCITY) {
            return (float)(baseValue * (1.0 + 0.2 * modifier.getLevel()));
         }
      } else if (stat == ToolStats.ACCURACY) {
         return (float)(baseValue * (1.0 - 0.15 * modifier.getLevel()));
      }

      return baseValue;
   }

   public void onProjectileLaunch(
      IToolStackView tool,
      ModifierEntry modifier,
      LivingEntity shooter,
      Projectile projectile,
      @Nullable AbstractArrow arrow,
      ModDataNBT persistentData,
      boolean primary
   ) {
      if (!this.reverse(tool)) {
         this.setPower(projectile, 0.2F);
      }
   }
}
