package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import com.creeping_creeper.tinkers_thinking.data.ModDataKeys;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
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
import modernmods.modernfoundry.shared.TinkerEffects;

public record RetransitModule(LevelingValue amount) implements ModifierModule, ModifyDamageModifierHook, ModifierUtils {
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.MODIFY_DAMAGE});
   public static final RecordLoadable<RetransitModule> LOADER = RecordLoadable.create(
      LevelingValue.LOADABLE.directField(RetransitModule::amount), RetransitModule::new
   );

   @NotNull
   public RecordLoadable<RetransitModule> getLoader() {
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
      Optional<Holder> dataCap = TinkerDataCapability.getCapability(living).resolve();
      dataCap.ifPresent(data -> {
         int x = (int)this.amount().eachLevel() * (Integer)data.get(ModDataKeys.Retransit, 0) * 20;
         if (this.reverse(tool) && !living.hasEffect(TinkerEffects.returning)) {
            this.addEffect(living, TinkerEffects.returning, x);
         }

         if (!this.reverse(tool) && !living.hasEffect(ModEffects.reminiscence)) {
            this.addEffect(living, ModEffects.reminiscence, x);
         }
      });
      return amount;
   }
}
