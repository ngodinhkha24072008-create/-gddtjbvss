package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

public record SculkGravityModule(LevelingValue amount) implements ModifierModule, ProjectileHitModifierHook, ModifierUtils {
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.PROJECTILE_HIT});
   public static final RecordLoadable<SculkGravityModule> LOADER = RecordLoadable.create(
      LevelingValue.LOADABLE.directField(SculkGravityModule::amount), SculkGravityModule::new
   );

   @NotNull
   public RecordLoadable<SculkGravityModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
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
      if (target != null
         && !target.hasEffect(ModEffects.modifier_immune)
         && attacker != null
         && attacker.hasEffect(ModEffects.sculk_power)) {
         this.addEffect(target, ModEffects.overweight, 120, 5);
         this.addEffect(target, ModEffects.jumpless, 120, 0, false);
         this.addEffect(target, ModEffects.modifier_immune, (int)(this.amount.eachLevel() / modifier.getLevel()));
      }

      return false;
   }
}
