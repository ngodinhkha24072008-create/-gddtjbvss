package mrthomas20121.tinkers_reforged.module;

import java.util.List;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedContext;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public record OverfractureModule(LevelingInt consumed) implements ModifierModule, BreakSpeedModifierHook {
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.BREAK_SPEED});
   public static final RecordLoadable<OverfractureModule> LOADER = RecordLoadable.create(
      LevelingInt.LOADABLE.requiredField("consumed", OverfractureModule::consumed), OverfractureModule::new
   );

   public RecordLoadable<? extends ModifierModule> getLoader() {
      return LOADER;
   }

   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier, BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
   }

   public float modifyBreakSpeed(IToolStackView tool, ModifierEntry modifier, BreakSpeedContext context, float speed) {
      if (!context.isEffective()) {
         int overslime = OverslimeModule.INSTANCE.getAmount(tool);
         if (overslime > 0) {
            float level = modifier.getEffectiveLevel();
            int target = this.consumed.compute(level);
            int consumed = Math.min(overslime, target);
            speed += target;
            OverslimeModule.INSTANCE.removeAmount(tool, consumed);
         }
      }

      return speed;
   }
}
