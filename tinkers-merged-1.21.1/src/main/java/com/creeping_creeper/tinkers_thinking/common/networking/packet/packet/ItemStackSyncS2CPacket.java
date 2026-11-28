package com.creeping_creeper.tinkers_thinking.common.networking.packet.packet;

import com.creeping_creeper.tinkers_thinking.common.things.block.entity.DryingRackBlockEntity;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server -> client: sync the contents of a drying rack. */
public record ItemStackSyncS2CPacket(List<ItemStack> stacks, BlockPos pos) implements CustomPacketPayload {
   public static final Type<ItemStackSyncS2CPacket> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath("tinkers_thinking", "item_stack_sync"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackSyncS2CPacket> STREAM_CODEC = StreamCodec.composite(
      ItemStack.OPTIONAL_LIST_STREAM_CODEC, ItemStackSyncS2CPacket::stacks,
      BlockPos.STREAM_CODEC, ItemStackSyncS2CPacket::pos,
      ItemStackSyncS2CPacket::new);

   public ItemStackSyncS2CPacket(ItemStackHandler handler, BlockPos pos) {
      this(copy(handler), pos);
   }

   private static List<ItemStack> copy(ItemStackHandler handler) {
      ItemStack[] out = new ItemStack[handler.getSlots()];
      for (int i = 0; i < out.length; i++) {
         out[i] = handler.getStackInSlot(i).copy();
      }
      return List.of(out);
   }

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ItemStackSyncS2CPacket packet, IPayloadContext context) {
      context.enqueueWork(() -> {
         if (Minecraft.getInstance().level != null
               && Minecraft.getInstance().level.getBlockEntity(packet.pos()) instanceof DryingRackBlockEntity blockEntity) {
            ItemStackHandler handler = new ItemStackHandler(packet.stacks().size());
            for (int i = 0; i < packet.stacks().size(); i++) {
               handler.setStackInSlot(i, packet.stacks().get(i));
            }
            blockEntity.setHandler(handler);
         }
      });
   }
}
