package com.creeping_creeper.tinkers_thinking.common.things.block.entity;

import com.creeping_creeper.tinkers_thinking.common.register.ModBlockEntities;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;

public class WasteFluidCylinderBlockEntity extends BlockEntity {
   private final LazyOptional<IFluidHandler> mainOpt = LazyOptional.of(WasteFluidCylinderBlockEntity.VoidHandler::new);

   public WasteFluidCylinderBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
      super((BlockEntityType)ModBlockEntities.Waste_Fluid_Cylinder.get(), p_155229_, p_155230_);
   }

   public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
      return (LazyOptional<T>)(capability == Capabilities.FLUID_HANDLER ? this.mainOpt : super.getCapability(capability, facing));
   }

   public void setRemoved() {
      super.setRemoved();
      this.mainOpt.invalidate();
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
