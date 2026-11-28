package mrthomas20121.tinkers_reforged.api.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class MetalTagData extends TagData {
   private final TagKey<Item> ingotTag = this.makeItemTag("ingots");
   private final TagKey<Item> nuggetTag = this.makeItemTag("nuggets");
   private final TagKey<Item> dustTag = this.makeItemTag("dusts");
   private final TagKey<Item> plateTag = this.makeItemTag("plates");
   private final TagKey<Item> gearTag = this.makeItemTag("gears");
   private TagKey<Item> oreBlockItem;
   private TagKey<Item> rawItemTag;
   private final TagKey<Block> blockTag;
   private final TagKey<Item> blockItemTag;
   private TagKey<Block> rawBlockTag;
   private TagKey<Item> rawBlockItemTag;
   private TagKey<Block> oreBlock;

   public MetalTagData(ResourceLocation name, boolean isOre) {
      super(name);
      if (isOre) {
         this.rawItemTag = this.makeItemTag("raw_materials");
         this.rawBlockItemTag = this.makeExtraItemTag("storage_blocks/raw_");
         this.rawBlockTag = this.makeExtraBlockTag("storage_blocks/raw_");
         this.oreBlockItem = this.makeItemTag("ores");
         this.oreBlock = this.makeBlockTag("ores");
      }

      this.blockTag = this.makeBlockTag("storage_blocks");
      this.blockItemTag = this.makeItemTag("storage_blocks");
   }

   @Nullable
   public TagKey<Item> getRawBlockItemTag() {
      return this.rawBlockItemTag;
   }

   @Nullable
   public TagKey<Item> getRawItemTag() {
      return this.rawItemTag;
   }

   @Nullable
   public TagKey<Block> getRawBlockTag() {
      return this.rawBlockTag;
   }

   @Nullable
   public TagKey<Block> getOreBlock() {
      return this.oreBlock;
   }

   @Nullable
   public TagKey<Item> getOreBlockItem() {
      return this.oreBlockItem;
   }

   public TagKey<Item> getBlockItemTag() {
      return this.blockItemTag;
   }

   public TagKey<Item> getIngotTag() {
      return this.ingotTag;
   }

   public TagKey<Item> getNuggetTag() {
      return this.nuggetTag;
   }

   public TagKey<Item> getDustTag() {
      return this.dustTag;
   }

   public TagKey<Item> getPlateTag() {
      return this.plateTag;
   }

   public TagKey<Item> getGearTag() {
      return this.gearTag;
   }

   public TagKey<Block> getBlockTag() {
      return this.blockTag;
   }
}
