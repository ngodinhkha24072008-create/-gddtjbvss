package com.creeping_creeper.tinkers_thinking.common.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/** Drying rack: one input item turns into one output item over time. */
public class DryingRackRecipes implements Recipe<SingleRecipeInput> {
   private final ItemStack output;
   private final NonNullList<Ingredient> recipeItems;

   public DryingRackRecipes(ItemStack output, NonNullList<Ingredient> recipeItems) {
      this.output = output;
      this.recipeItems = recipeItems;
   }

   @Override
   public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
      return !level.isClientSide() && this.recipeItems.get(0).test(input.item());
   }

   @Override
   public ItemStack assemble(@NotNull SingleRecipeInput input, @NotNull HolderLookup.Provider registries) {
      return this.output.copy();
   }

   @NotNull
   @Override
   public NonNullList<Ingredient> getIngredients() {
      return this.recipeItems;
   }

   @NotNull
   @Override
   public String getGroup() {
      return "misc";
   }

   @Override
   public boolean isSpecial() {
      return true;
   }

   @Override
   public boolean canCraftInDimensions(int width, int height) {
      return false;
   }

   @Override
   public ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
      return this.output.copy();
   }

   public ItemStack getOutput() {
      return this.output;
   }

   @NotNull
   @Override
   public RecipeSerializer<?> getSerializer() {
      return Serializer.INSTANCE;
   }

   @NotNull
   @Override
   public RecipeType<?> getType() {
      return ModRecipes.DRYING_RACK_TYPE.get();
   }

   public static class Serializer implements RecipeSerializer<DryingRackRecipes> {
      public static final Serializer INSTANCE = new Serializer();

      private static final MapCodec<DryingRackRecipes> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
         ItemStack.STRICT_CODEC.fieldOf("output").forGetter(r -> r.output),
         Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredient").forGetter(r -> r.recipeItems)
      ).apply(inst, (out, ings) -> new DryingRackRecipes(out, NonNullList.of(Ingredient.EMPTY, ings.toArray(Ingredient[]::new)))));

      private static final StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipes> STREAM_CODEC = StreamCodec.composite(
         ItemStack.STREAM_CODEC, r -> r.output,
         Ingredient.CONTENTS_STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list()), r -> r.recipeItems,
         (out, ings) -> new DryingRackRecipes(out, NonNullList.of(Ingredient.EMPTY, ings.toArray(Ingredient[]::new))));

      @Override
      public MapCodec<DryingRackRecipes> codec() {
         return CODEC;
      }

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipes> streamCodec() {
         return STREAM_CODEC;
      }
   }

   /** Kept so existing references (JEI plugin) keep compiling; the type itself is registered in ModRecipes. */
   public static final class Type {
      public static final String ID = "drying_rack";

      private Type() {
      }
   }
}
