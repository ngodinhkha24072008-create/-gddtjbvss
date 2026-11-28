package pyre.tinkerslevellingaddon.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server -> client: a tool levelled up. */
public record LevelUpPacket(int level, Component toolName) implements CustomPacketPayload {
   public static final Type<LevelUpPacket> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath("tinkerslevellingaddon", "level_up"));
   public static final StreamCodec<RegistryFriendlyByteBuf, LevelUpPacket> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, LevelUpPacket::level,
      ComponentSerialization.STREAM_CODEC, LevelUpPacket::toolName,
      LevelUpPacket::new);

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(LevelUpPacket packet, IPayloadContext context) {
      context.enqueueWork(() -> ClientPacketHandler.handleLevelUpMessage(packet.level(), packet.toolName()));
   }
}
