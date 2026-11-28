package pyre.tinkerslevellingaddon;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import pyre.tinkerslevellingaddon.command.ModCommands;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.network.Messages;
import pyre.tinkerslevellingaddon.setup.Registration;

@Mod("tinkerslevellingaddon")
public class TinkersLevellingAddon {
   public static final Logger LOGGER = LogUtils.getLogger();
   public static final String MOD_ID = "tinkerslevellingaddon";

   public TinkersLevellingAddon(net.neoforged.bus.api.IEventBus modBus, net.neoforged.fml.ModContainer container) {
      Config.init();
      Registration.init();
      Messages.register(modBus);
      ModCommands.init();
   }
}
