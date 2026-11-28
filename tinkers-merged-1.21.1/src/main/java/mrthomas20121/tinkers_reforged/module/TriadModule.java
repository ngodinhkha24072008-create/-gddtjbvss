package mrthomas20121.tinkers_reforged.module;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public record TriadModule(LevelingInt consumed) implements ModifierModule, MeleeDamageModifierHook {
   public static final RecordLoadable<TriadModule> LOADER = RecordLoadable.create(
      LevelingInt.LOADABLE.requiredField("consumed", TriadModule::consumed), TriadModule::new
   );
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.MELEE_DAMAGE});

   public RecordLoadable<? extends ModifierModule> getLoader() {
      return LOADER;
   }

   public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      if (context.getLivingTarget() != null) {
         float bonusDamage = damage - baseDamage;
         LivingEntity entity = context.getLivingTarget();
         float level = modifier.getEffectiveLevel();
         int target = this.consumed.compute(level);
         float dmg = target + entity.getMaxHealth();
         return 0.2F * dmg + bonusDamage;
      } else {
         return damage;
      }
   }

   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }
}
