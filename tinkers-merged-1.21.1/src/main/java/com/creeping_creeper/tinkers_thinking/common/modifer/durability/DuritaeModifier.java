package com.creeping_creeper.tinkers_thinking.common.modifer.durability;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class DuritaeModifier extends Modifier implements ToolDamageModifierHook {
   public int getPriority() {
      return 185;
   }

   protected void registerHooks(@NotNull Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE);
   }

   public int onDamageTool(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
      float chance1 = (float)Math.pow(0.7, modifier.getLevel());
      float chance2 = (float)Math.pow(0.95, modifier.getLevel());
      int maxDamage = amount;

      for (int i = 0; i < maxDamage; i++) {
         if (RANDOM.nextFloat() > chance1) {
            amount--;
         }

         if (RANDOM.nextFloat() > chance2) {
            amount++;
         }
      }

      return amount;
   }
}
