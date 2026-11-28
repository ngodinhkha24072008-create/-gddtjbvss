package com.creeping_creeper.tinkers_thinking.common.register;

import com.creeping_creeper.tinkers_thinking.common.things.block.entity.DryingRackBlockEntity;
import com.creeping_creeper.tinkers_thinking.common.things.block.entity.WasteFluidCylinderBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockEntities extends ModModule {
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> Drying_Rack = BLOCK_ENTITIES.register(
      "drying_rack", DryingRackBlockEntity::new, ModCommonItems.drying_rack
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WasteFluidCylinderBlockEntity>> Waste_Fluid_Cylinder = BLOCK_ENTITIES.register(
      "waste_fluid_cylinder", WasteFluidCylinderBlockEntity::new, ModCommonItems.waste_fluid_cylinder
   );

   /** Registered on the mod bus by {@code bus.register(new ModBlockEntities())} in TinkersThinking. */
   @SubscribeEvent
   void registerCapabilities(RegisterCapabilitiesEvent event) {
      event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Drying_Rack.get(), DryingRackBlockEntity::getItemHandler);
      event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Waste_Fluid_Cylinder.get(), (be, side) -> be.getFluidHandler());
   }
}
