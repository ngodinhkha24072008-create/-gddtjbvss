package com.creeping_creeper.tinkers_thinking.common.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
   private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "tinkers_thinking");
   private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "tinkers_thinking");

   public static final DeferredHolder<RecipeType<?>, RecipeType<DryingRackRecipes>> DRYING_RACK_TYPE = TYPES.register(
      "drying_rack", () -> RecipeType.simple(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tinkers_thinking", "drying_rack"))
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DryingRackRecipes>> Drying_Rack = RECIPE_SERIALIZERS.register(
      "drying_rack", () -> DryingRackRecipes.Serializer.INSTANCE
   );

   public static void registers(IEventBus eventBus) {
      TYPES.register(eventBus);
      RECIPE_SERIALIZERS.register(eventBus);
   }
}
