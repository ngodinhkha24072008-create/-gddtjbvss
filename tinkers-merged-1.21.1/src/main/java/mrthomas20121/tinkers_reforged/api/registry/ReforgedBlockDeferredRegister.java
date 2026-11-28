package mrthomas20121.tinkers_reforged.api.registry;

import java.util.EnumMap;
import java.util.Locale;
import mrthomas20121.tinkers_reforged.api.data.Metal;
import mrthomas20121.tinkers_reforged.api.holder.BlockMetalObject;
import mrthomas20121.tinkers_reforged.api.holder.BlockOreObject;
import mrthomas20121.tinkers_reforged.api.holder.BlockOverworldOreObject;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import modernmods.hilt.registration.object.ItemObject;
import modernmods.modernfoundry.common.registration.BlockDeferredRegisterExtension;
import modernmods.modernfoundry.shared.block.PlatformBlock;
import modernmods.modernfoundry.shared.block.SlimesteelBlock;

public class ReforgedBlockDeferredRegister extends BlockDeferredRegisterExtension {
   public ReforgedBlockDeferredRegister(String modID) {
      super(modID);
   }

   public BlockMetalObject registerMetal(String name) {
      ItemObject<Block> block = this.register(
         name + "_block",
         () -> new Block(Properties.ofFullCopy(Blocks.IRON_BLOCK).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresCorrectToolForDrops().strength(4.0F, 4.0F)),
         b -> new BlockItem(b, new net.minecraft.world.item.Item.Properties())
      );
      ItemObject<PlatformBlock> platform = this.register(
         name + "_platform",
         () -> new PlatformBlock(Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()),
         b -> new BlockItem(b, new net.minecraft.world.item.Item.Properties())
      );
      return new BlockMetalObject(block, platform);
   }

   public BlockMetalObject registerSlimeMetal(String name) {
      ItemObject<Block> block = this.register(
         name + "_block",
         () -> new SlimesteelBlock(
            Properties.ofFullCopy(Blocks.IRON_BLOCK).instrument(NoteBlockInstrument.IRON_XYLOPHONE).noOcclusion().requiresCorrectToolForDrops().strength(4.0F, 4.0F)
         ),
         b -> new BlockItem(b, new net.minecraft.world.item.Item.Properties())
      );
      ItemObject<PlatformBlock> platform = this.register(
         name + "_platform",
         () -> new PlatformBlock(Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops().strength(3.0F, 6.0F).noOcclusion()),
         b -> new BlockItem(b, new net.minecraft.world.item.Item.Properties())
      );
      return new BlockMetalObject(block, platform);
   }

   public BlockOverworldOreObject registerOverworldOre(String name) {
      ItemObject<DropExperienceBlock> ore = this.register(
         name + "_ore",
         () -> new DropExperienceBlock(
            UniformInt.of(3, 7),
            Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
         ),
         b -> new BlockItem(b, new net.minecraft.world.item.Item.Properties())
      );
      ItemObject<DropExperienceBlock> deepslate_ore = this.register(
         "deepslate_" + name + "_ore",
         () -> new DropExperienceBlock(
            UniformInt.of(3, 7),
            Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
         ),
         b -> new BlockItem(b, new net.minecraft.world.item.Item.Properties())
      );
      ItemObject<Block> rawOreBlock = this.register(
         name + "_raw_block",
         () -> new Block(Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)),
         b -> new BlockItem(b, new net.minecraft.world.item.Item.Properties())
      );
      ItemObject<Item> raw_ore_item = new ItemObject(this.itemRegister.register("raw_" + name, () -> new Item(new net.minecraft.world.item.Item.Properties())));
      return new BlockOverworldOreObject(ore, deepslate_ore, rawOreBlock, raw_ore_item);
   }

   public BlockOreObject registerOre(String name) {
      ItemObject<DropExperienceBlock> ore = this.register(
         name + "_ore",
         () -> new DropExperienceBlock(
            UniformInt.of(3, 7),
            Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
         ),
         b -> new BlockItem(b, new net.minecraft.world.item.Item.Properties())
      );
      ItemObject<Block> rawOreBlock = this.register(
         name + "_raw_block",
         () -> new Block(Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)),
         b -> new BlockItem(b, new net.minecraft.world.item.Item.Properties())
      );
      ItemObject<Item> raw_ore_item = new ItemObject(this.itemRegister.register("raw_" + name, () -> new Item(new net.minecraft.world.item.Item.Properties())));
      return new BlockOreObject(ore, rawOreBlock, raw_ore_item);
   }

   public EnumMap<Metal, BlockMetalObject> registerEnumMetal(Class<Metal> e) {
      EnumMap<Metal, BlockMetalObject> map = new EnumMap<>(e);
      Metal[] values = Metal.values;

      for (Metal value : values) {
         if (value.getSerializedName().contains("slime")) {
            map.put(value, this.registerSlimeMetal(value.name().toLowerCase(Locale.ROOT)));
         } else {
            map.put(value, this.registerMetal(value.name().toLowerCase(Locale.ROOT)));
         }
      }

      return map;
   }
}
