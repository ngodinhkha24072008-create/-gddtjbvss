package com.creeping_creeper.tinkers_thinking.common.things.block;

import com.creeping_creeper.tinkers_thinking.common.register.ModBlockEntities;
import com.creeping_creeper.tinkers_thinking.common.things.block.entity.DryingRackBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class DryingRackBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   private static final VoxelShape EAST = Block.box(6.0, 12.0, 0.0, 10.0, 16.0, 16.0);
   private static final VoxelShape NORTH = Block.box(0.0, 12.0, 6.0, 16.0, 16.0, 10.0);

   @NotNull
   public RenderShape getRenderShape(@NotNull BlockState p_49232_) {
      return RenderShape.MODEL;
   }

   public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos blockPos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock()) {
         BlockEntity block = level.getBlockEntity(blockPos);
         if (block instanceof DryingRackBlockEntity) {
            ((DryingRackBlockEntity)block).drops();
         }
      }

      super.onRemove(state, level, blockPos, newState, isMoving);
   }

   @NotNull
   public InteractionResult use(
      @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult res
   ) {
      if (!level.isClientSide()) {
         BlockEntity entity = level.getBlockEntity(pos);
         if (entity instanceof DryingRackBlockEntity) {
            if (((DryingRackBlockEntity)entity).itemStackHandler.getStackInSlot(0).isEmpty()
               && ((DryingRackBlockEntity)entity).itemStackHandler.getStackInSlot(1).isEmpty()) {
               if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                  ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
                  CompoundTag tag = itemStack.getTag();
                  ItemStack itemStack2 = new ItemStack(itemStack.getItem(), 1);
                  itemStack2.setTag(tag);
                  ((DryingRackBlockEntity)entity).itemStackHandler.setStackInSlot(0, itemStack2);
                  itemStack.shrink(1);
                  return InteractionResult.SUCCESS;
               }

               if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
                  ItemStack itemStack = player.getItemInHand(InteractionHand.OFF_HAND);
                  CompoundTag tag = itemStack.getTag();
                  ItemStack itemStack2 = new ItemStack(itemStack.getItem(), 1);
                  itemStack2.setTag(tag);
                  ((DryingRackBlockEntity)entity).itemStackHandler.setStackInSlot(0, itemStack2);
                  itemStack.shrink(1);
                  return InteractionResult.SUCCESS;
               }

               return InteractionResult.CONSUME;
            }

            if (!((DryingRackBlockEntity)entity).itemStackHandler.getStackInSlot(0).isEmpty()) {
               ItemHandlerHelper.giveItemToPlayer(player, ((DryingRackBlockEntity)entity).itemStackHandler.getStackInSlot(0), player.getInventory().selected);
               ((DryingRackBlockEntity)entity).itemStackHandler.extractItem(0, 1, false);
               level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
               return InteractionResult.SUCCESS;
            }

            ItemHandlerHelper.giveItemToPlayer(player, ((DryingRackBlockEntity)entity).itemStackHandler.getStackInSlot(1), player.getInventory().selected);
            ((DryingRackBlockEntity)entity).itemStackHandler.extractItem(1, 1, false);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.CONSUME;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new DryingRackBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return createTickerHelper(type, (BlockEntityType)ModBlockEntities.Drying_Rack.get(), DryingRackBlockEntity::tick);
   }

   public DryingRackBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
      Direction direction = (Direction)p_60555_.getValue(FACING);

      return switch (direction) {
         case EAST, WEST -> EAST;
         default -> NORTH;
      };
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
   }

   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return (BlockState)pState.setValue(FACING, pRotation.rotate((Direction)pState.getValue(FACING)));
   }

   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation((Direction)pState.getValue(FACING)));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }
}
