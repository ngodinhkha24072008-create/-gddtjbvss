package mrthomas20121.tinkers_reforged.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import modernmods.modernfoundry.shared.CommonsClientEvents;

@EventBusSubscriber(
   modid = "tinkers_reforged",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class ClientEvents {
   @SubscribeEvent
   static void clientSetup(FMLClientSetupEvent event) {
      TinkersReforgedBook.BOOK.fontRenderer = CommonsClientEvents.unicodeFontRender();
   }
}
