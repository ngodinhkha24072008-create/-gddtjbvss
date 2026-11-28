package com.creeping_creeper.tinkers_thinking.common.modifer.harvest;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.mining.BlockBreakModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolHarvestContext;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class InspiredModifier extends Modifier implements BlockBreakModifierHook, ModifierUtils {
   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK);
   }

   public void afterBlockBreak(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolHarvestContext context) {
      if (context.canHarvest() && context.isEffective() && !context.isAOE() && RANDOM.nextFloat() < 0.25) {
         LivingEntity living = context.getLiving();
         this.addEffect(living, MobEffects.DIG_SPEED, 20, 1);
         ToolDamageUtil.directDamage(tool, modifier.getLevel() * 5, living, living.getUseItem());
      }
   }
}
