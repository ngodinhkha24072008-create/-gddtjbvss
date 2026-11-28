package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public record SculkLevitateModule(LevelingValue amount) implements ModifierModule, MeleeHitModifierHook, RedirectAfter, ModifierUtils {
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(
      new ModuleHook[]{ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT}
   );
   public static final RecordLoadable<SculkLevitateModule> LOADER = RecordLoadable.create(
      LevelingValue.LOADABLE.directField(SculkLevitateModule::amount), SculkLevitateModule::new
   );

   @NotNull
   public RecordLoadable<SculkLevitateModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      if (!context.isExtraAttack() && context.isFullyCharged()) {
         LivingEntity attacker = context.getAttacker();
         LivingEntity target = context.getLivingTarget();
         if (attacker.hasEffect(ModEffects.sculk_power) && target != null && !target.hasEffect(ModEffects.modifier_immune)) {
            this.addEffect(target, ModEffects.weightless, 120, 2);
            this.addEffect(target, ModEffects.modifier_immune, (int)(this.amount.eachLevel() / modifier.getLevel()));
         }
      }
   }
}
