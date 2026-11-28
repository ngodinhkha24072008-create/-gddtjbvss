package mrthomas20121.tinkers_reforged.api.data;

import java.util.Arrays;
import java.util.Locale;
import mrthomas20121.tinkers_reforged.api.tags.MetalTagData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.modernfoundry.library.materials.definition.MaterialId;

public enum Metal implements StringRepresentable {
   SLIMEBRONZE(750, MapColor.TERRACOTTA_GREEN),
   GRAPHITE(500, MapColor.COLOR_BLACK),
   YTTRIUM(true, 855, MapColor.COLOR_RED),
   ASTATINE(935, MapColor.TERRACOTTA_ORANGE),
   BARIUM(true, 1450, MapColor.TERRACOTTA_PURPLE),
   TELLURIUM(2000, MapColor.TERRACOTTA_RED),
   ACTINIUM(2000, MapColor.TERRACOTTA_GRAY),
   THALLIUM(true, 2500, MapColor.TERRACOTTA_CYAN),
   GERMANIUM(3000, MapColor.TERRACOTTA_PINK),
   BERKELIUM(2500, MapColor.COLOR_LIGHT_GREEN);

   public static final Metal[] values = values();
   public static final MaterialId[] TRIM_MATERIALS = Arrays.stream(values).map(Metal::asMaterial).toList().toArray(new MaterialId[0]);
   private final MetalTagData metalTags;
   private final String serializedName = this.name().toLowerCase(Locale.ROOT);
   private final boolean isOre;
   private final int temperature;
   private final MapColor mapColor;
   private final MaterialId id;

   private Metal(boolean isOre, int temp, MapColor mapColor) {
      this.metalTags = new MetalTagData(ResourceLocation.fromNamespaceAndPath("tinkers_reforged", this.serializedName), isOre);
      this.isOre = isOre;
      this.temperature = temp;
      this.mapColor = mapColor;
      this.id = new MaterialId("tinkers_reforged", this.serializedName);
   }

   private Metal(int temperature, MapColor mapColor) {
      this(false, temperature, mapColor);
   }

   public MaterialId asMaterial() {
      return this.id;
   }

   public boolean isOre() {
      return this.isOre;
   }

   public int getTemperature() {
      return this.temperature;
   }

   public MapColor getMapColor() {
      return this.mapColor;
   }

   public MetalTagData getTags() {
      return this.metalTags;
   }

   public TagKey<Item> getIngotTag() {
      return this.metalTags.getIngotTag();
   }

   public TagKey<Item> getNuggetTag() {
      return this.metalTags.getNuggetTag();
   }

   public TagKey<Item> getDustTag() {
      return this.metalTags.getDustTag();
   }

   public TagKey<Item> getPlateTag() {
      return this.metalTags.getPlateTag();
   }

   public TagKey<Item> getGearTag() {
      return this.metalTags.getGearTag();
   }

   public TagKey<Block> getBlockTag() {
      return this.metalTags.getBlockTag();
   }

   public TagKey<Item> getBlockItemTag() {
      return this.metalTags.getBlockItemTag();
   }

   @Nullable
   public TagKey<Item> getRawItemTag() {
      return this.metalTags.getRawItemTag();
   }

   @Nullable
   public TagKey<Item> getRawBlockItemTag() {
      return this.metalTags.getRawBlockItemTag();
   }

   @Nullable
   public TagKey<Block> getRawBlockTag() {
      return this.metalTags.getRawBlockTag();
   }

   @Nullable
   public TagKey<Block> getOreBlock() {
      return this.metalTags.getOreBlock();
   }

   @Nullable
   public TagKey<Item> getOreBlockItem() {
      return this.metalTags.getOreBlockItem();
   }

   @NotNull
   public String getSerializedName() {
      return this.serializedName;
   }
}
