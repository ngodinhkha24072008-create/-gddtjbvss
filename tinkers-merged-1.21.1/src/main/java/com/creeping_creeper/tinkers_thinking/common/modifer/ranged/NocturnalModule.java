package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

public record NocturnalModule(LevelingValue amount) implements ModifierModule, ConditionalStatModifierHook {
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.CONDITIONAL_STAT});
   public static final RecordLoadable<NocturnalModule> LOADER = RecordLoadable.create(
      LevelingValue.LOADABLE.directField(NocturnalModule::amount), NocturnalModule::new
   );

   @NotNull
   public RecordLoadable<NocturnalModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public float modifyStat(IToolStackView tool, ModifierEntry modifier, LivingEntity living, FloatToolStat stat, float baseValue, float multiplier) {
      return stat == ToolStats.VELOCITY
         ? baseValue * (1.0F + (living.level().isDay() ? -this.amount.eachLevel() : this.amount.eachLevel() * modifier.getLevel()))
         : baseValue;
   }
}
