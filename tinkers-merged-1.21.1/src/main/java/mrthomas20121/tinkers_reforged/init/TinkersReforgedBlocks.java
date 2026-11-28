package mrthomas20121.tinkers_reforged.init;

import java.util.EnumMap;
import mrthomas20121.tinkers_reforged.api.data.Metal;
import mrthomas20121.tinkers_reforged.api.holder.BlockMetalObject;
import mrthomas20121.tinkers_reforged.api.holder.BlockOreObject;
import mrthomas20121.tinkers_reforged.api.holder.BlockOverworldOreObject;
import mrthomas20121.tinkers_reforged.api.registry.ReforgedBlockDeferredRegister;

public class TinkersReforgedBlocks {
   public static ReforgedBlockDeferredRegister BLOCKS = new ReforgedBlockDeferredRegister("tinkers_reforged");
   public static EnumMap<Metal, BlockMetalObject> METAl_BLOCKS = BLOCKS.registerEnumMetal(Metal.class);
   public static BlockOverworldOreObject YTTRIUM_ORE = BLOCKS.registerOverworldOre("yttrium");
   public static BlockOreObject BARIUM_ORE = BLOCKS.registerOre("barium");
   public static BlockOreObject THALLIUM_ORE = BLOCKS.registerOre("thallium");
}
