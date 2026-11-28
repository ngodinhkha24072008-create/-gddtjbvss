package com.creeping_creeper.tinkers_thinking.common.register;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSounds {
   public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(net.minecraft.core.registries.Registries.SOUND_EVENT, "tinkers_thinking");
   public static final DeferredHolder<SoundEvent, SoundEvent> DAMAGE_BLOCKING = registerSoundEvent("damage_blocking");

   private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
      ResourceLocation id = ResourceLocation.fromNamespaceAndPath("tinkers_thinking", name);
      return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
   }
}
