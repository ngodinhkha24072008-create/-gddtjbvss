package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import com.creeping_creeper.tinkers_thinking.data.ModDataKeys;
import java.util.List;
import java.util.Optional;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.Holder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public enum AntibruteModule implements ModifierModule, ModifyDamageModifierHook, ModifierUtils {
   INSTANCE;

   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.MODIFY_HURT});
   public static final RecordLoadable<AntibruteModule> LOADER = new SingletonLoader(INSTANCE);

   @NotNull
   public RecordLoadable<AntibruteModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public float modifyDamageTaken(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage
   ) {
      LivingEntity living = context.getEntity();
      float x = living.getHealth() / 2.0F;
      if (!source.is(DamageTypeTags.BYPASSES_RESISTANCE) && !context.getEntity().hasEffect(ModEffects.antibrute_cooldown) && amount > x) {
         Optional<Holder> dataCap = TinkerDataCapability.getCapability(living).resolve();
         dataCap.ifPresent(
            data -> this.addEffect(context.getEntity(), ModEffects.antibrute_cooldown, 240 / (Integer)data.get(ModDataKeys.Antibrute, 1))
         );
         this.block(living);
         return x;
      } else {
         return amount;
      }
   }
}
