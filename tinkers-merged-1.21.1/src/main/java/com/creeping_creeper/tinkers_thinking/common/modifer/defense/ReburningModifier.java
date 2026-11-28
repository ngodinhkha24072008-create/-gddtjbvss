package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.data.ModDataKeys;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.Holder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public enum ReburningModifier implements ModifierModule, OnAttackedModifierHook, ModifierUtils {
   INSTANCE;

   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.ON_ATTACKED});
   public static final RecordLoadable<ReburningModifier> LOADER = new SingletonLoader(INSTANCE);

   @NotNull
   public RecordLoadable<ReburningModifier> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public void onAttacked(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage
   ) {
      LivingEntity living = context.getEntity();
      int fire = living.getRemainingFireTicks() / 20;
      if (!living.hasEffect(MobEffects.FIRE_RESISTANCE)) {
         Optional<Holder> dataCap = TinkerDataCapability.getCapability(living).resolve();
         dataCap.ifPresent(data -> living.heal(fire * (Integer)data.get(ModDataKeys.Reburning, 1)));
         living.clearFire();
         this.particles(living.level(), living, ParticleTypes.SMOKE, 4);
         this.addEffect(living, MobEffects.FIRE_RESISTANCE, 80);
      }
   }
}
