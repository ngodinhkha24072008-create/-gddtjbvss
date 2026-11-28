package com.creeping_creeper.tinkers_thinking.common.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DryingRackRecipes implements Recipe<SimpleContainer> {
   private final ResourceLocation id;
   private final ItemStack output;
   private final NonNullList<Ingredient> recipeItems;
   private static String category = "misc";

   public DryingRackRecipes(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems, String category) {
      this.id = id;
      this.output = output;
      this.recipeItems = recipeItems;
      DryingRackRecipes.category = category;
   }

   public boolean matches(@NotNull SimpleContainer pContainer, Level pLevel) {
      return pLevel.isClientSide() ? false : ((Ingredient)this.recipeItems.get(0)).test(pContainer.getItem(0));
   }

   public ItemStack assemble(SimpleContainer p_44001_, RegistryAccess p_267165_) {
      return this.output;
   }

   @NotNull
   public NonNullList<Ingredient> getIngredients() {
      return this.recipeItems;
   }

   @NotNull
   public String getGroup() {
      return category;
   }

   public boolean isSpecial() {
      return true;
   }

   public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
      return false;
   }

   public ItemStack getResultItem(@Nullable RegistryAccess p_267052_) {
      return this.output.copy();
   }

   @NotNull
   public ResourceLocation getId() {
      return this.id;
   }

   @NotNull
   public RecipeSerializer<?> getSerializer() {
      return DryingRackRecipes.Serializer.INSTANCE;
   }

   @NotNull
   public RecipeType<?> getType() {
      return DryingRackRecipes.Type.INSTANCE;
   }

   public static class Serializer implements RecipeSerializer<DryingRackRecipes> {
      public static final DryingRackRecipes.Serializer INSTANCE = new DryingRackRecipes.Serializer();
      public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("tinkers_thinking", "drying_rack");

      @NotNull
      public DryingRackRecipes fromJson(@NotNull ResourceLocation pRecipeId, @NotNull JsonObject pSerializedRecipe) {
         ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));
         JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredient");
         NonNullList<Ingredient> inputs = NonNullList.withSize(1, Ingredient.EMPTY);
         String category = "misc";

         for (int i = 0; i < inputs.size(); i++) {
            inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
         }

         return new DryingRackRecipes(pRecipeId, output, inputs, category);
      }

      @Nullable
      public DryingRackRecipes fromNetwork(@NotNull ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
         NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(), Ingredient.EMPTY);
         inputs.replaceAll(ignored -> Ingredient.fromNetwork(pBuffer));
         ItemStack output = pBuffer.readItem();
         String category = "misc";
         return new DryingRackRecipes(pRecipeId, output, inputs, category);
      }

      public void toNetwork(FriendlyByteBuf pBuffer, DryingRackRecipes pRecipe) {
         pBuffer.writeInt(pRecipe.getIngredients().size());

         for (Ingredient ing : pRecipe.getIngredients()) {
            ing.toNetwork(pBuffer);
         }

         pBuffer.writeItemStack(pRecipe.getResultItem(null), false);
      }
   }

   public static class Type implements RecipeType<DryingRackRecipes> {
      public static final DryingRackRecipes.Type INSTANCE = new DryingRackRecipes.Type();
      public static final String ID = "drying_rack";

      private Type() {
      }
   }
}
