package com.creeping_creeper.tinkers_thinking.common.things.block.entity;

import com.creeping_creeper.tinkers_thinking.common.register.ModBlockEntities;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;

public class WasteFluidCylinderBlockEntity extends BlockEntity {
   private final IFluidHandler handler = new VoidHandler();

   public WasteFluidCylinderBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.Waste_Fluid_Cylinder.get(), pos, state);
   }

   /** Exposed as a block capability in ModBlockEntities. Voids every fluid inserted. */
   public IFluidHandler getFluidHandler() {
      return this.handler;
   }

   private static class VoidHandler implements IFluidHandler {
      public int getTanks() {
         return 1;
      }

      @Nonnull
      public FluidStack getFluidInTank(int tank) {
         return FluidStack.EMPTY;
      }

      public int getTankCapacity(int tank) {
         return 10000;
      }

      public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
         return true;
      }

      public int fill(FluidStack resource, FluidAction action) {
         return resource.getAmount();
      }

      @Nonnull
      public FluidStack drain(FluidStack resource, FluidAction action) {
         return FluidStack.EMPTY;
      }

      @Nonnull
      public FluidStack drain(int maxDrain, FluidAction action) {
         return FluidStack.EMPTY;
      }
   }
}
