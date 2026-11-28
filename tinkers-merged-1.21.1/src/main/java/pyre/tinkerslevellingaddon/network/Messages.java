package pyre.tinkerslevellingaddon.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class Messages {
   public static void register(IEventBus modBus) {
      modBus.addListener(Messages::onRegisterPayloads);
   }

   private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1.0").optional();
      registrar.playToClient(LevelUpPacket.TYPE, LevelUpPacket.STREAM_CODEC, LevelUpPacket::handle);
   }

   public static void sendToServer(CustomPacketPayload message) {
      PacketDistributor.sendToServer(message);
   }

   public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, message);
   }
}
