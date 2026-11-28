package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

public record LightlyAttackModule(LevelingValue amount) implements ModifierModule, MeleeHitModifierHook, ConditionalStatModifierHook, ModifierUtils {
   private static final Component Boost = TinkersThinking.makeTranslation("modifier", "lightly_attack.draw_speed");
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.MELEE_HIT, ModifierHooks.CONDITIONAL_STAT});
   public static final RecordLoadable<LightlyAttackModule> LOADER = RecordLoadable.create(
      LevelingValue.LOADABLE.directField(LightlyAttackModule::amount), LightlyAttackModule::new
   );

   @NotNull
   public RecordLoadable<LightlyAttackModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   private boolean isEmpty(LivingEntity living, int stack) {
      return living.getSlot(stack).get().isEmpty();
   }

   private boolean isAllEmpty(LivingEntity living) {
      int x = 0;

      for (int i = 0; i < 9; i++) {
         if (!this.isEmpty(living, i)) {
            x++;
         }
      }

      return x < 2;
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      if (!context.isExtraAttack() && this.isAllEmpty(context.getAttacker())) {
         this.addEffect(context.getAttacker(), (MobEffect)ModEffects.quick_attack.get(), 60, modifier.getLevel() * 3 - 1);
      }
   }

   public float modifyStat(IToolStackView tool, ModifierEntry modifier, LivingEntity living, FloatToolStat stat, float baseValue, float multiplier) {
      return this.isAllEmpty(living) && stat == ToolStats.DRAW_SPEED ? baseValue * (1.0F + this.amount.eachLevel() * modifier.getLevel()) : baseValue;
   }
}
