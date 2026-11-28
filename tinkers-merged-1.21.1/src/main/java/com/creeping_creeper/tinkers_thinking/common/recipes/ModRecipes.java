package com.creeping_creeper.tinkers_thinking.common.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import modernmods.hilt.registration.deferred.SynchronizedDeferredRegister;

public class ModRecipes {
   private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "tinkers_thinking");
   protected static final SynchronizedDeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = SynchronizedDeferredRegister.create(
      ForgeRegistries.RECIPE_SERIALIZERS, "tinkers_thinking"
   );
   public static final DeferredHolder<RecipeSerializer<DryingRackRecipes>> Drying_Rack = RECIPE_SERIALIZERS.register(
      "drying_rack", () -> DryingRackRecipes.Serializer.INSTANCE
   );

   public static void registers(IEventBus eventBus) {
      RECIPE_SERIALIZERS.register(eventBus);
   }
}
