package com.creeping_creeper.tinkers_thinking.common.modifer.durability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ToolStatsModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.capability.ToolEnergyCapability;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ModifierStatsBuilder;

public class OverchargeModifier extends Modifier implements InventoryTickModifierHook, ToolStatsModifierHook {
   protected void registerHooks(@NotNull Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK, ModifierHooks.TOOL_STATS).addModule(ToolEnergyCapability.ENERGY_HANDLER);
   }

   public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
      ToolEnergyCapability.MAX_STAT.add(builder, (Float)builder.getStat(OverslimeModule.OVERSLIME_STAT) * 100.0F);
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
      if (!world.isClientSide && holder.tickCount % 20 == 0 && holder.getUseItem() != stack) {
         int x = ToolEnergyCapability.getEnergy(tool);
         if (OverslimeModule.getCapacity(tool) > OverslimeModule.INSTANCE.getAmount(tool) && x >= 200) {
            int y = Math.min(x / 200, modifier.getLevel());
            OverslimeModule.INSTANCE.addAmount(tool, modifier, y);
            ToolEnergyCapability.setEnergy(tool, ToolEnergyCapability.getEnergy(tool) - 200 * y);
         }
      }
   }
}
