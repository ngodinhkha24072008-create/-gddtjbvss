package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public enum SculkStruggleModule implements ModifierModule, MeleeHitModifierHook, ModifierUtils {
   INSTANCE;

   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.MELEE_HIT});
   public static final RecordLoadable<SculkStruggleModule> LOADER = new SingletonLoader(INSTANCE);

   @NotNull
   public RecordLoadable<SculkStruggleModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      LivingEntity attacker = context.getAttacker();
      LivingEntity living = context.getLivingTarget();
      if (!context.isExtraAttack() && attacker.hasEffect(ModEffects.sculk_power) && living != null && living.isDeadOrDying()) {
         int x = 0;
         int y = 0;
         if (attacker.hasEffect(ModEffects.last_effort)) {
            x = attacker.getEffect(ModEffects.last_effort).getDuration();
            y = attacker.getEffect(ModEffects.last_effort).getAmplifier();
         }

         this.addEffect(attacker, ModEffects.last_effort, 100 * modifier.getLevel() + x, y);
      }
   }
}
