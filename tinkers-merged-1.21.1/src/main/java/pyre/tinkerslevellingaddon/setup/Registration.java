package pyre.tinkerslevellingaddon.setup;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;
import pyre.tinkerslevellingaddon.ImprovableModule;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;

public class Registration {
   public static void init() {
      IEventBus bus = net.neoforged.fml.ModLoadingContext.get().getActiveContainer().getEventBus();
      bus.addListener(Registration::registerSerializers);
   }

   private static void registerSerializers(RegisterEvent event) {
      if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
         ModifierModule.LOADER.register(ResourceLocation.fromNamespaceAndPath("tinkerslevellingaddon", "improvable"), ImprovableModule.LOADER);
      }
   }

   private Registration() {
   }
}
