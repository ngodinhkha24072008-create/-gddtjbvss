package com.creeping_creeper.tinkers_thinking.common.modifer.durability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class OvereatModifier extends Modifier implements InventoryTickModifierHook {
   protected void registerHooks(@NotNull Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
   }

   public void onInventoryTick(
      @NotNull IToolStackView tool,
      @NotNull ModifierEntry modifier,
      Level world,
      @NotNull LivingEntity holder,
      int itemSlot,
      boolean isSelected,
      boolean isCorrectSlot,
      ItemStack stack
   ) {
      if (!world.isClientSide
         && holder.tickCount % 20 == 0
         && holder.getUseItem() != stack
         && 0 < OverslimeModule.INSTANCE.getAmount(tool)
         && 0 < tool.getDamage()
         && RANDOM.nextFloat() < modifier.getLevel() * 0.15) {
         ToolDamageUtil.repair(tool, 1);
         if (RANDOM.nextFloat() < 0.33) {
            OverslimeModule.INSTANCE.removeAmount(tool, modifier, 1);
         }
      }
   }
}
