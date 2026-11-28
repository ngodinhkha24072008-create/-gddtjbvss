package mrthomas20121.tinkers_reforged.api.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class TagData {
   private final ResourceLocation name;

   public TagData(ResourceLocation name) {
      this.name = name;
   }

   protected ResourceLocation getResourceName() {
      return this.name;
   }

   protected TagKey<Item> makeItemTag(String type) {
      return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", type + "/" + this.name.getPath()));
   }

   protected TagKey<Block> makeBlockTag(String type) {
      return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", type + "/" + this.name.getPath()));
   }

   protected TagKey<Block> makeExtraBlockTag(String type) {
      return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", type + this.name.getPath()));
   }

   protected TagKey<Item> makeExtraItemTag(String type) {
      return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", type + this.name.getPath()));
   }

   protected TagKey<Item> makeNamespaceItemTag(String type) {
      return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(this.name.getNamespace(), type + "/" + this.name.getPath()));
   }

   protected TagKey<Block> makeNamespaceBlockTag(String type) {
      return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(this.name.getNamespace(), type + "/" + this.name.getPath()));
   }
}
