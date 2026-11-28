package pyre.tinkerslevellingaddon.setup;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import pyre.tinkerslevellingaddon.util.ModUtil;

@EventBusSubscriber(
   modid = "tinkerslevellingaddon",
   bus = Bus.MOD
)
public enum Sounds {
   CHIME("tool_level_up_chime"),
   SNARE_DRUM("tool_level_up_snare_drum"),
   YAY("tool_level_up_yay");

   private final SoundEvent sound;

   private Sounds(String name) {
      this.sound = SoundEvent.createVariableRangeEvent(ModUtil.getResource(name));
   }

   @SubscribeEvent
   public static void registerSounds(RegisterEvent event) {
      if (event.getRegistryKey() == Registries.SOUND_EVENT) {
         for (Sounds sound : values()) {
            event.register(Registries.SOUND_EVENT, sound.sound.getLocation(), sound::getSound);
         }
      }
   }

   public SoundEvent getSound() {
      return this.sound;
   }
}
