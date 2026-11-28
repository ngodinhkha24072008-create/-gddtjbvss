package mrthomas20121.tinkers_reforged.api.holder;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import modernmods.hilt.registration.object.ItemObject;
import modernmods.modernfoundry.shared.block.PlatformBlock;

public class BlockMetalObject extends ItemObject<Block> {
   private final Supplier<? extends PlatformBlock> platform;

   public BlockMetalObject(ItemObject<Block> block, ItemObject<PlatformBlock> platform) {
      super(block);
      this.platform = platform;
   }

   public PlatformBlock getPlatform() {
      return Objects.requireNonNull(this.platform.get(), "BlockMetalObject is missing platform block");
   }
}
