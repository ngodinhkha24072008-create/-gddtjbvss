package cn.mcmod.tinker_rapier;

import cn.mcmod.tinker_rapier.item.TiCItemRegistry;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("tinker_rapier")
public class RapierMain {
   public static final String MODID = "tinker_rapier";
   private static final Logger LOGGER = LogManager.getLogger();

   public RapierMain(IEventBus modBus, ModContainer container) {
      container.registerConfig(Type.COMMON, RapierConfig.COMMON_CONFIG);
      TiCItemRegistry.ITEMS.register(modBus);
      RapierCreativeGroup.CREATIVE_MODE_TABS.register(modBus);
   }

   public static Properties defaultItemProperties() {
      return new Properties();
   }

   public static Logger getLogger() {
      return LOGGER;
   }
}
