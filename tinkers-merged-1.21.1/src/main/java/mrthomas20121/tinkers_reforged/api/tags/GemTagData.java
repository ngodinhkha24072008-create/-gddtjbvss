package mrthomas20121.tinkers_reforged.api.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class GemTagData extends TagData {
   private final TagKey<Item> gemTag = this.makeItemTag("gems");
   private final TagKey<Item> nuggetTag = this.makeItemTag("nuggets");
   private final TagKey<Item> dustTag = this.makeItemTag("dusts");
   private final TagKey<Item> plateTag = this.makeItemTag("plates");
   private final TagKey<Item> gearTag = this.makeItemTag("gears");
   private final TagKey<Block> blockTag = this.makeBlockTag("storage_blocks");

   public GemTagData(ResourceLocation name) {
      super(name);
   }

   public TagKey<Item> getGemTag() {
      return this.gemTag;
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
