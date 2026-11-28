package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

public record NonsenseModule(LevelingValue amount) implements ModifierModule, ProjectileHitModifierHook {
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.PROJECTILE_HIT});
   public static final RecordLoadable<NonsenseModule> LOADER = RecordLoadable.create(
      LevelingValue.LOADABLE.directField(NonsenseModule::amount), NonsenseModule::new
   );

   @NotNull
   public RecordLoadable<NonsenseModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public void onProjectileHitBlock(
      ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, BlockHitResult hit, @Nullable LivingEntity attacker
   ) {
      float level = modifier.getEffectiveLevel();
      if (Modifier.RANDOM.nextFloat() > level * this.amount.eachLevel()) {
         projectile.level()
            .playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), SoundEvents.GRASS_BREAK, SoundSource.MASTER, 1.0F, 1.0F);
         projectile.discard();
      }
   }
}
