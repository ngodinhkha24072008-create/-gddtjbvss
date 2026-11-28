package com.creeping_creeper.tinkers_thinking.common.client;

import com.creeping_creeper.tinkers_thinking.common.register.ModBlockEntities;
import com.creeping_creeper.tinkers_thinking.common.register.ModToolItems;
import com.creeping_creeper.tinkers_thinking.common.things.block.renderer.DryingRackBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import modernmods.modernfoundry.library.client.model.TinkerItemProperties;

@EventBusSubscriber(
   modid = "tinkers_thinking",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class ClientEvents {
   private static Font unicodeRenderer;

   public static void onConstruct() {
      ModBooks.initBook();
   }

   @SubscribeEvent
   public static void registerRenderers(RegisterRenderers event) {
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.Drying_Rack.get(), DryingRackBlockEntityRenderer::new);
   }

   @SubscribeEvent
   static void clientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         TinkerItemProperties.registerToolProperties(ModToolItems.paxel.asItem());
         TinkerItemProperties.registerToolProperties(ModToolItems.knife.asItem());
         TinkerItemProperties.registerToolProperties(ModToolItems.mace.asItem());
         TinkerItemProperties.registerToolProperties(ModToolItems.cutlass.asItem());
         TinkerItemProperties.registerToolProperties(ModToolItems.atlatl.asItem());
         TinkerItemProperties.registerToolProperties(ModToolItems.magma_staff.asItem());
         TinkerItemProperties.registerToolProperties(ModToolItems.quartz_staff.asItem());
         TinkerItemProperties.registerToolProperties(ModToolItems.clay_staff.asItem());
         TinkerItemProperties.registerCrossbowProperties(ModToolItems.repeating_crossbow.asItem());
         EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      });
      ModBooks.FANTASTIC_GADGETRY.fontRenderer = unicodeFontRender();
   }

   public static Font unicodeFontRender() {
      if (unicodeRenderer == null) {
         unicodeRenderer = new Font(rl -> {
            FontManager resourceManager = Minecraft.getInstance().fontManager;
            return (FontSet)resourceManager.fontSets.get(Minecraft.UNIFORM_FONT);
         }, false);
      }

      return unicodeRenderer;
   }
}
