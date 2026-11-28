package mrthomas20121.tinkers_reforged.module;

import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.hilt.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.ArmorLootingModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.LootingContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public record RoastModule(LevelingInt consumed, IJsonPredicate<LivingEntity> predicate) implements ModifierModule, ArmorLootingModifierHook {
   public static final RecordLoadable<RoastModule> LOADER = RecordLoadable.create(
      LevelingInt.LOADABLE.requiredField("consumed", RoastModule::consumed),
      LivingEntityPredicate.LOADER.requiredField("predicate", RoastModule::predicate),
      RoastModule::new
   );
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.ARMOR_LOOTING});

   public RecordLoadable<? extends ModifierModule> getLoader() {
      return LOADER;
   }

   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public int updateArmorLooting(
      IToolStackView tool, ModifierEntry modifier, LootingContext context, EquipmentContext equipment, EquipmentSlot slot, int looting
   ) {
      LivingEntity holder = context.getHolder();
      if (this.predicate.matches(holder)) {
         return looting > 0 && looting < 4
            ? looting + this.consumed.compute(modifier.getEffectiveLevel())
            : this.consumed.compute(modifier.getEffectiveLevel());
      } else {
         return looting;
      }
   }
}
