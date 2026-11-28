package mrthomas20121.tinkers_reforged.api.holder;

import java.util.Objects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import modernmods.hilt.registration.object.ItemObject;

public class BlockOreObject extends ItemObject<DropExperienceBlock> {
   private final ItemObject<Block> rawOreBlock;
   private final ItemObject<Item> rawOreItem;

   public BlockOreObject(ItemObject<DropExperienceBlock> ore, ItemObject<Block> RAW_ORE_BLOCK, ItemObject<Item> RAW_ORE_ITEM) {
      super(ore);
      this.rawOreBlock = RAW_ORE_BLOCK;
      this.rawOreItem = RAW_ORE_ITEM;
   }

   public Block getRawOreBlock() {
      return Objects.requireNonNull((Block)this.rawOreBlock.get(), "BlockOreObject is missing a raw ore block");
   }

   public Item getRawItem() {
      return Objects.requireNonNull((Item)this.rawOreItem.get(), "BlockOreObject is missing a raw ore item");
   }
}
