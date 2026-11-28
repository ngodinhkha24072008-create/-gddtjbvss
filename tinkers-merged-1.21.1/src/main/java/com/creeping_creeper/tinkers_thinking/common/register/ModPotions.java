package com.creeping_creeper.tinkers_thinking.common.register;

import com.creeping_creeper.tinkers_thinking.common.recipes.ModBrewingRecipe;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.brewing.BrewingRecipeRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModPotions {
   public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, "tinkers_thinking");
   public static final DeferredHolder<Potion, Potion> overweight_potion = POTIONS.register(
      "overweight_potion", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)ModEffects.overweight.get(), 1200, 0)})
   );
   public static final DeferredHolder<Potion, Potion> overweight_potion_strong = POTIONS.register(
      "overweight_potion_strong", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)ModEffects.overweight.get(), 400, 1)})
   );
   public static final DeferredHolder<Potion, Potion> overweight_potion_long = POTIONS.register(
      "overweight_potion_long", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)ModEffects.overweight.get(), 2400, 0)})
   );
   public static final DeferredHolder<Potion, Potion> weightless_potion = POTIONS.register(
      "weightless_potion", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)ModEffects.weightless.get(), 1200, 0)})
   );
   public static final DeferredHolder<Potion, Potion> weightless_potion_strong = POTIONS.register(
      "weightless_potion_strong", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)ModEffects.weightless.get(), 400, 1)})
   );
   public static final DeferredHolder<Potion, Potion> weightless_potion_long = POTIONS.register(
      "weightless_potion_long", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)ModEffects.weightless.get(), 2400, 0)})
   );

   public static void init() {
      potionBrewing(
         PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD), (Potion)overweight_potion.get(), (Item)ModCommonItems.clay_crystal.get()
      );
      potionBrewing(
         PotionUtils.setPotion(new ItemStack(Items.POTION), (Potion)overweight_potion.get()), (Potion)overweight_potion_strong.get(), Items.GLOWSTONE_DUST
      );
      potionBrewing(PotionUtils.setPotion(new ItemStack(Items.POTION), (Potion)overweight_potion.get()), (Potion)overweight_potion_long.get(), Items.REDSTONE);
      potionBrewing(PotionUtils.setPotion(new ItemStack(Items.POTION), (Potion)overweight_potion.get()), (Potion)weightless_potion.get(), Items.FERMENTED_SPIDER_EYE);
      potionBrewing(
         PotionUtils.setPotion(new ItemStack(Items.POTION), (Potion)overweight_potion_strong.get()), (Potion)weightless_potion_strong.get(), Items.FERMENTED_SPIDER_EYE
      );
      potionBrewing(
         PotionUtils.setPotion(new ItemStack(Items.POTION), (Potion)overweight_potion_long.get()), (Potion)weightless_potion_long.get(), Items.FERMENTED_SPIDER_EYE
      );
      potionBrewing(
         PotionUtils.setPotion(new ItemStack(Items.POTION), (Potion)weightless_potion.get()), (Potion)weightless_potion_strong.get(), Items.GLOWSTONE_DUST
      );
      potionBrewing(PotionUtils.setPotion(new ItemStack(Items.POTION), (Potion)weightless_potion.get()), (Potion)weightless_potion_long.get(), Items.REDSTONE);
   }

   private static void potionBrewing(ItemStack inputPot, Potion pot, Item item) {
      BrewingRecipeRegistry.addRecipe(
         new ModBrewingRecipe(inputPot, Ingredient.of(new ItemLike[]{item}), PotionUtils.setPotion(new ItemStack(Items.POTION), pot))
      );
   }

   public static void registers(IEventBus eventBus) {
      POTIONS.register(eventBus);
   }
}
