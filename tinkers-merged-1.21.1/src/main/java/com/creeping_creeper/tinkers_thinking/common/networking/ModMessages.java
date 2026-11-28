package com.creeping_creeper.tinkers_thinking.common.networking;

import com.creeping_creeper.tinkers_thinking.common.networking.packet.packet.ItemStackSyncS2CPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class ModMessages {
   public static void register(IEventBus modBus) {
      modBus.addListener(ModMessages::onRegisterPayloads);
   }

   private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
      event.registrar("1.0").optional()
         .playToClient(ItemStackSyncS2CPacket.TYPE, ItemStackSyncS2CPacket.STREAM_CODEC, ItemStackSyncS2CPacket::handle);
   }

   public static void sendToServer(CustomPacketPayload message) {
      PacketDistributor.sendToServer(message);
   }

   public static void sendToClients(CustomPacketPayload message) {
      PacketDistributor.sendToAllPlayers(message);
   }

   public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, message);
   }
}
