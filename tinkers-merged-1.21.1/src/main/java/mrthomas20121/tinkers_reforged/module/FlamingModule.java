package mrthomas20121.tinkers_reforged.module;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public record FlamingModule(LevelingInt consumed) implements ModifierModule, ToolDamageModifierHook {
   public static final RecordLoadable<FlamingModule> LOADER = RecordLoadable.create(
      LevelingInt.LOADABLE.requiredField("consumed", FlamingModule::consumed), FlamingModule::new
   );
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.TOOL_DAMAGE});

   public RecordLoadable<? extends ModifierModule> getLoader() {
      return LOADER;
   }

   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
      return 0;
   }

   public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder, @Nullable ItemStack stack) {
      if (holder != null && holder.isOnFire() && amount > 1) {
         int value = this.consumed.compute(modifier.getEffectiveLevel());
         if (amount >= value) {
            return amount - value;
         }
      }

      return amount;
   }
}
