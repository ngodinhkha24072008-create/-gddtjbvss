package com.creeping_creeper.tinkers_thinking.common.register;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
   public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, "tinkers_thinking");
   public static final DeferredHolder<Potion, Potion> overweight_potion = POTIONS.register(
      "overweight_potion", () -> new Potion(new MobEffectInstance(ModEffects.overweight, 1200, 0))
   );
   public static final DeferredHolder<Potion, Potion> overweight_potion_strong = POTIONS.register(
      "overweight_potion_strong", () -> new Potion(new MobEffectInstance(ModEffects.overweight, 400, 1))
   );
   public static final DeferredHolder<Potion, Potion> overweight_potion_long = POTIONS.register(
      "overweight_potion_long", () -> new Potion(new MobEffectInstance(ModEffects.overweight, 2400, 0))
   );
   public static final DeferredHolder<Potion, Potion> weightless_potion = POTIONS.register(
      "weightless_potion", () -> new Potion(new MobEffectInstance(ModEffects.weightless, 1200, 0))
   );
   public static final DeferredHolder<Potion, Potion> weightless_potion_strong = POTIONS.register(
      "weightless_potion_strong", () -> new Potion(new MobEffectInstance(ModEffects.weightless, 400, 1))
   );
   public static final DeferredHolder<Potion, Potion> weightless_potion_long = POTIONS.register(
      "weightless_potion_long", () -> new Potion(new MobEffectInstance(ModEffects.weightless, 2400, 0))
   );

   /** Kept for the old call site in common setup; brewing is now registered through an event. */
   public static void init() {
   }

   private static void registerBrewing(RegisterBrewingRecipesEvent event) {
      PotionBrewing.Builder b = event.getBuilder();
      b.addMix(Potions.AWKWARD, ModCommonItems.clay_crystal.get(), overweight_potion);
      b.addMix(overweight_potion, Items.GLOWSTONE_DUST, overweight_potion_strong);
      b.addMix(overweight_potion, Items.REDSTONE, overweight_potion_long);
      b.addMix(overweight_potion, Items.FERMENTED_SPIDER_EYE, weightless_potion);
      b.addMix(overweight_potion_strong, Items.FERMENTED_SPIDER_EYE, weightless_potion_strong);
      b.addMix(overweight_potion_long, Items.FERMENTED_SPIDER_EYE, weightless_potion_long);
      b.addMix(weightless_potion, Items.GLOWSTONE_DUST, weightless_potion_strong);
      b.addMix(weightless_potion, Items.REDSTONE, weightless_potion_long);
   }

   public static void registers(IEventBus eventBus) {
      POTIONS.register(eventBus);
      NeoForge.EVENT_BUS.addListener(ModPotions::registerBrewing);
   }
}
