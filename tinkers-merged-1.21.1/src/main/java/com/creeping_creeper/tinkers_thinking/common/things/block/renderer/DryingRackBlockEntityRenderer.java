package com.creeping_creeper.tinkers_thinking.common.things.block.renderer;

import com.creeping_creeper.tinkers_thinking.common.things.block.DryingRackBlock;
import com.creeping_creeper.tinkers_thinking.common.things.block.entity.DryingRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class DryingRackBlockEntityRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {
   public DryingRackBlockEntityRenderer(Context context) {
   }

   public void render(
      DryingRackBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay
   ) {
      ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
      ItemStack itemStack = pBlockEntity.getRenderStack();
      pPoseStack.pushPose();
      pPoseStack.scale(1.0F, 1.0F, 1.0F);
      switch ((Direction)pBlockEntity.getBlockState().getValue(DryingRackBlock.FACING)) {
         case NORTH:
            pPoseStack.translate(0.5F, 0.5F, 0.345F);
            break;
         case SOUTH:
            pPoseStack.translate(0.5F, 0.5F, 0.655F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            break;
         case WEST:
            pPoseStack.translate(0.345F, 0.5F, 0.5F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            break;
         case EAST:
            pPoseStack.translate(0.655F, 0.5F, 0.5F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
      }

      renderer.renderStatic(
         itemStack,
         ItemDisplayContext.FIXED,
         this.getLightLevel(Objects.requireNonNull(pBlockEntity.getLevel()), pBlockEntity.getBlockPos()),
         OverlayTexture.NO_OVERLAY,
         pPoseStack,
         pBufferSource,
         pBlockEntity.getLevel(),
         1
      );
      pPoseStack.popPose();
   }

   private int getLightLevel(Level level, BlockPos pos) {
      int bLight = level.getBrightness(LightLayer.BLOCK, pos);
      int sLight = level.getBrightness(LightLayer.SKY, pos);
      return LightTexture.pack(bLight, sLight);
   }
}
