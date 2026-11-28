package com.creeping_creeper.tinkers_thinking.common.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import modernmods.hilt.client.model.NBTKeyModel;
import modernmods.modernfoundry.library.tools.SlotType;

public class ModSlots {
   public static SlotType ANCIENT = SlotType.getOrCreate("ancient");

   @OnlyIn(Dist.CLIENT)
   public static void init() {
      NBTKeyModel.registerExtraTexture(
         ResourceLocation.parse("modernfoundry:creative_slot"), ANCIENT.getName(), ResourceLocation.parse("tinkers_thinking:item/ancient_ceramic")
      );
   }
}
