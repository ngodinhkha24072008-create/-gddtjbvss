package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileShootModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

public record BideTimeModule(LevelingValue amount) implements ModifierModule, ProjectileShootModifierHook {
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.PROJECTILE_SHOT});
   public static final RecordLoadable<BideTimeModule> LOADER = RecordLoadable.create(
      LevelingValue.LOADABLE.directField(BideTimeModule::amount), BideTimeModule::new
   );

   @NotNull
   public RecordLoadable<BideTimeModule> getLoader() {
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
      if (shooter instanceof Player player) {
         player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), (int)this.amount.eachLevel());
         player.getCooldowns().addCooldown(player.getOffhandItem().getItem(), (int)this.amount.eachLevel());
      }
   }
}
