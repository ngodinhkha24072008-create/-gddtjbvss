package mrthomas20121.tinkers_reforged;

import mrthomas20121.tinkers_reforged.client.TinkersReforgedBook;
import mrthomas20121.tinkers_reforged.init.TinkersReforgedBlocks;
import mrthomas20121.tinkers_reforged.init.TinkersReforgedFluids;
import mrthomas20121.tinkers_reforged.init.TinkersReforgedItems;
import mrthomas20121.tinkers_reforged.module.FlamingModule;
import mrthomas20121.tinkers_reforged.module.OverfractureModule;
import mrthomas20121.tinkers_reforged.module.RoastModule;
import mrthomas20121.tinkers_reforged.module.TriadModule;
import mrthomas20121.tinkers_reforged.predicate.TinkersReforgedPredicates;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import modernmods.hilt.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;

@Mod("tinkers_reforged")
public class TinkersReforged {
   public static final String MOD_ID = "tinkers_reforged";
   public static final Logger LOGGER = LogManager.getLogger();

   public TinkersReforged(IEventBus modBus) {
      IEventBus bus = modBus;
      TinkersReforgedBlocks.BLOCKS.register(bus);
      TinkersReforgedItems.register(bus);
      TinkersReforgedFluids.FLUIDS.register(bus);
      bus.addListener(this::registerSerializers);
      if (net.neoforged.fml.loading.FMLEnvironment.dist == Dist.CLIENT) { TinkersReforgedBook.initBook(); }
   }

   public static ResourceLocation getResource(String resource) {
      return ResourceLocation.fromNamespaceAndPath("tinkers_reforged", resource);
   }

   public static String makeDescriptionId(String type, String name) {
      return type + ".tinkers_reforged." + name;
   }

   public void registerSerializers(RegisterEvent event) {
      if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
         ModifierModule.LOADER.register(getResource("overfracture"), OverfractureModule.LOADER);
         ModifierModule.LOADER.register(getResource("triad"), TriadModule.LOADER);
         ModifierModule.LOADER.register(getResource("roast"), RoastModule.LOADER);
         ModifierModule.LOADER.register(getResource("flaming"), FlamingModule.LOADER);
         LivingEntityPredicate.LOADER.register(getResource("is_nether_biome"), TinkersReforgedPredicates.IS_NETHER_BIOME.getLoader());
         LivingEntityPredicate.LOADER.register(getResource("below_zero"), TinkersReforgedPredicates.BELOW_ZERO.getLoader());
         LivingEntityPredicate.LOADER.register(getResource("not_max_health"), TinkersReforgedPredicates.NOT_MAX_HEALTH.getLoader());
         LivingEntityPredicate.LOADER.register(getResource("not_in_overworld"), TinkersReforgedPredicates.IS_NOT_IN_THE_OVERWORLD.getLoader());
      }
   }
}
