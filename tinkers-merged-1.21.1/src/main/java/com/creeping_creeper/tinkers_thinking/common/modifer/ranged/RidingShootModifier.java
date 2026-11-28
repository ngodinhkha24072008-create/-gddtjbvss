package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileShootModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

public class RidingShootModifier extends Modifier implements ProjectileShootModifierHook, ProjectileHitModifierHook, TooltipModifierHook, ModifierUtils {
   private static final Component POWER = TinkersThinking.makeTranslation("modifier", "riding_shoot.projectile_power");

   protected void registerHooks(Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.PROJECTILE_SHOT, ModifierHooks.PROJECTILE_HIT, ModifierHooks.TOOLTIP);
   }

   public void onProjectileShoot(
      IToolStackView tool,
      ModifierEntry modifier,
      @Nullable LivingEntity shooter,
      ItemStack ammo,
      Projectile projectile,
      @Nullable AbstractArrow arrow,
      ModDataNBT persistentData,
      boolean primary
   ) {
      if (shooter != null && shooter.isPassenger()) {
         this.addPower(projectile, modifier.getLevel());
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
      if (target != null && (target.isPassenger() || target.isVehicle())) {
         target.unRide();
      }

      return false;
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag
   ) {
      if (player != null) {
         TooltipModifierHook.addPercentBoost(modifier.getModifier(), POWER, player.isPassenger() ? modifier.getLevel() : 0.0, tooltip);
      }
   }
}
