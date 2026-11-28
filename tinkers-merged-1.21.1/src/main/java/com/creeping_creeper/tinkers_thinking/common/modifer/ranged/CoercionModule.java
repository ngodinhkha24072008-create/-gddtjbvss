package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileShootModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

public enum CoercionModule implements ModifierModule, ProjectileShootModifierHook, ModifierUtils {
   INSTANCE;

   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.PROJECTILE_SHOT});
   public static final RecordLoadable<CoercionModule> LOADER = new SingletonLoader(INSTANCE);

   @NotNull
   public RecordLoadable<CoercionModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
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
      if (shooter != null && shooter.hasEffect(MobEffects.INVISIBILITY)) {
         int x = shooter.getEffect(MobEffects.INVISIBILITY).getDuration();
         shooter.removeEffect(MobEffects.INVISIBILITY);
         this.addEffect(shooter, MobEffects.INVISIBILITY, x / 2);
      }
   }
}
