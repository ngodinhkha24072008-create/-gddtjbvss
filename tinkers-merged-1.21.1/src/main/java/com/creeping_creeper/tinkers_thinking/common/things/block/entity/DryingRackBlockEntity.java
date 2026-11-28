package com.creeping_creeper.tinkers_thinking.common.things.block.entity;

import com.creeping_creeper.tinkers_thinking.common.networking.ModMessages;
import com.creeping_creeper.tinkers_thinking.common.networking.packet.packet.ItemStackSyncS2CPacket;
import com.creeping_creeper.tinkers_thinking.common.recipes.DryingRackRecipes;
import com.creeping_creeper.tinkers_thinking.common.register.ModBlockEntities;
import com.creeping_creeper.tinkers_thinking.common.things.block.DryingRackBlock;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import com.creeping_creeper.tinkers_thinking.common.recipes.ModRecipes;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.block.entity.HiltBlockEntity;

public class DryingRackBlockEntity extends HiltBlockEntity {
   public final ItemStackHandler itemStackHandler = new ItemStackHandler(2) {
      public int getSlotLimit(int slot) {
         return 1;
      }

      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
         return switch (slot) {
            case 0 -> this.getStackInSlot(1).isEmpty();
            case 1 -> false;
            default -> super.isItemValid(slot, stack);
         };
      }
   };
   private final Map<Direction, WrappedHandler> directionWrappedHandlerMap = Map.of(
      Direction.DOWN,
      new WrappedHandler(this.itemStackHandler, i -> i == 1, (i, s) -> false),
      Direction.UP,
      new WrappedHandler(this.itemStackHandler, i -> i == 1, (index, stack) -> this.itemStackHandler.isItemValid(0, stack)),
      Direction.NORTH,
      new WrappedHandler(this.itemStackHandler, i -> i == 1, (index, stack) -> this.itemStackHandler.isItemValid(0, stack)),
      Direction.SOUTH,
      new WrappedHandler(this.itemStackHandler, i -> i == 1, (index, stack) -> this.itemStackHandler.isItemValid(0, stack)),
      Direction.EAST,
      new WrappedHandler(this.itemStackHandler, i -> i == 1, (index, stack) -> this.itemStackHandler.isItemValid(0, stack)),
      Direction.WEST,
      new WrappedHandler(this.itemStackHandler, i -> i == 1, (index, stack) -> this.itemStackHandler.isItemValid(0, stack))
   );
   protected final ContainerData data;
   private int progress = 0;
   private int maxProgress = 2998;

   private static boolean canInsertItemToOutputSlot(SimpleContainer inventory) {
      return inventory.getItem(1).isEmpty();
   }

   public DryingRackBlockEntity(BlockPos blockPos, BlockState blockState) {
      super(ModBlockEntities.Drying_Rack.get(), blockPos, blockState);
      this.data = new ContainerData() {
         public int get(int index) {
            return switch (index) {
               case 0 -> DryingRackBlockEntity.this.progress;
               case 1 -> DryingRackBlockEntity.this.maxProgress;
               default -> 0;
            };
         }

         public void set(int index, int value) {
            switch (index) {
               case 0:
                  DryingRackBlockEntity.this.progress = value;
                  break;
               case 1:
                  DryingRackBlockEntity.this.maxProgress = value;
            }
         }

         public int getCount() {
            return 2;
         }
      };
   }

   /** Item handler exposed to pipes/hoppers; registered in ModBlockEntities via RegisterCapabilitiesEvent. */
   @Nullable
   public IItemHandler getItemHandler(@Nullable Direction side) {
      if (side == null) {
         return this.itemStackHandler;
      }
      if (this.itemStackHandler.getStackInSlot(0).isEmpty() && this.itemStackHandler.getStackInSlot(1).isEmpty()) {
         Direction localDir = this.getBlockState().getValue(DryingRackBlock.FACING);
         if (side != Direction.UP && side != Direction.DOWN) {
            return switch (localDir) {
               case EAST -> this.directionWrappedHandlerMap.get(side.getClockWise());
               case SOUTH -> this.directionWrappedHandlerMap.get(side);
               case WEST -> this.directionWrappedHandlerMap.get(side.getCounterClockWise());
               default -> this.directionWrappedHandlerMap.get(side.getOpposite());
            };
         }
         return this.directionWrappedHandlerMap.get(side);
      }
      return this.directionWrappedHandlerMap.get(Direction.DOWN);
   }

   @Override
   public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
      super.saveAdditional(nbt, registries);
      nbt.put("inventory", this.itemStackHandler.serializeNBT(registries));
      nbt.putInt("drying_rack.progress", this.progress);
   }

   @Override
   protected void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.Provider registries) {
      super.loadAdditional(nbt, registries);
      this.itemStackHandler.deserializeNBT(registries, nbt.getCompound("inventory"));
      this.progress = nbt.getInt("drying_rack.progress");
   }

   public void drops() {
      SimpleContainer inventory = new SimpleContainer(this.itemStackHandler.getSlots());

      for (int i = 0; i < this.itemStackHandler.getSlots(); i++) {
         inventory.setItem(i, this.itemStackHandler.getStackInSlot(i));
      }

      if (this.level != null) {
         Containers.dropContents(this.level, this.worldPosition, inventory);
      }
   }

   public static void tick(Level level, BlockPos blockPos, BlockState state, DryingRackBlockEntity entity) {
      if (!level.isClientSide) {
         ModMessages.sendToClients(new ItemStackSyncS2CPacket(entity.itemStackHandler, blockPos));
      }

      if (hasRecipe(entity)) {
         entity.progress++;
         setChanged(level, blockPos, state);
         if (entity.progress >= entity.maxProgress) {
            craftItem(entity);
         }
      } else {
         entity.resetProgress();
         setChanged(level, blockPos, state);
      }
   }

   private void resetProgress() {
      this.progress = 0;
   }

   private static void craftItem(DryingRackBlockEntity entity) {
      Level level = entity.level;
      SimpleContainer inventory = new SimpleContainer(entity.itemStackHandler.getSlots());

      for (int i = 0; i < entity.itemStackHandler.getSlots(); i++) {
         inventory.setItem(i, entity.itemStackHandler.getStackInSlot(i));
      }

      Optional<RecipeHolder<DryingRackRecipes>> recipe = Optional.empty();
      if (level != null) {
         recipe = level.getRecipeManager().getRecipeFor(ModRecipes.DRYING_RACK_TYPE.get(), new SingleRecipeInput(inventory.getItem(0)), level);
      }

      if (hasRecipe(entity)) {
         entity.itemStackHandler.extractItem(0, 1, false);
         entity.itemStackHandler.setStackInSlot(1, new ItemStack(recipe.get().value().getResultItem(level.registryAccess()).getItem(), 1));
         entity.resetProgress();
      }
   }

   private static boolean hasRecipe(DryingRackBlockEntity entity) {
      Level level = entity.level;
      SimpleContainer inventory = new SimpleContainer(entity.itemStackHandler.getSlots());

      for (int i = 0; i < entity.itemStackHandler.getSlots(); i++) {
         inventory.setItem(i, entity.itemStackHandler.getStackInSlot(i));
      }

      Optional<RecipeHolder<DryingRackRecipes>> recipe = Optional.empty();
      if (level != null) {
         recipe = level.getRecipeManager().getRecipeFor(ModRecipes.DRYING_RACK_TYPE.get(), new SingleRecipeInput(inventory.getItem(0)), level);
      }

      return recipe.isPresent() && canInsertItemToOutputSlot(inventory);
   }

   public ItemStack getRenderStack() {
      ItemStack itemStack;
      if (this.itemStackHandler.getStackInSlot(1).isEmpty()) {
         itemStack = this.itemStackHandler.getStackInSlot(0);
      } else {
         itemStack = this.itemStackHandler.getStackInSlot(1);
      }

      return itemStack;
   }

   public void setHandler(ItemStackHandler itemStackHandler) {
      for (int i = 0; i < itemStackHandler.getSlots(); i++) {
         this.itemStackHandler.setStackInSlot(i, itemStackHandler.getStackInSlot(i));
      }
   }
}
