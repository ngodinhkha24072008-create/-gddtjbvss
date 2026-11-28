package com.creeping_creeper.tinkers_thinking.mixins;

import com.creeping_creeper.tinkers_thinking.data.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import modernmods.modernfoundry.smeltery.block.entity.CastingBlockEntity;

@Mixin({CastingBlockEntity.class})
public class CastingBlockEntityMixin {
   @org.spongepowered.asm.mixin.Shadow
   private int timer;
   @org.spongepowered.asm.mixin.Shadow
   private int coolingTime;

   @Unique
   private boolean isFaceBlock(Level level, BlockPos pos) {
      return level.getBlockState(pos.east()).is(ModTags.Blocks.cooling_fast)
         || level.getBlockState(pos.west()).is(ModTags.Blocks.cooling_fast)
         || level.getBlockState(pos.south()).is(ModTags.Blocks.cooling_fast)
         || level.getBlockState(pos.north()).is(ModTags.Blocks.cooling_fast);
   }

   @Inject(
      method = {"serverTick"},
      at = {@At(
         value = "INVOKE",
         target = "Lmodernmods/modernfoundry/smeltery/block/entity/tank/CastingFluidHandler;getFluid()Lnet/neoforged/neoforge/fluids/FluidStack;",
         shift = Shift.BY,
         by = 2
      )},
      remap = false,
      require = 0
   )
   private void serverTick(Level level, BlockPos pos, CallbackInfo ci) {
      if (this.isFaceBlock(level, pos)) {
         this.timer++;
      }
   }

   @Inject(
      method = {"clientTick"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/neoforged/neoforge/fluids/FluidStack;isEmpty()Z",
         shift = Shift.BY,
         by = 1
      )},
      remap = false,
      require = 0
   )
   private void clientTick(Level level, BlockPos pos, CallbackInfo ci) {
      if (this.isFaceBlock(level, pos)) {
         this.timer++;
      }
   }
}
