package com.creeping_creeper.tinkers_thinking;

import com.creeping_creeper.tinkers_thinking.common.client.ClientEvents;
import com.creeping_creeper.tinkers_thinking.common.client.ModSlots;
import com.creeping_creeper.tinkers_thinking.common.library.OnDeath;
import com.creeping_creeper.tinkers_thinking.common.library.OnExpPickUp;
import com.creeping_creeper.tinkers_thinking.common.library.Onhurt;
import com.creeping_creeper.tinkers_thinking.common.networking.ModMessages;
import com.creeping_creeper.tinkers_thinking.common.recipes.ModRecipes;
import com.creeping_creeper.tinkers_thinking.common.register.ModBlockEntities;
import com.creeping_creeper.tinkers_thinking.common.register.ModCommonItems;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import com.creeping_creeper.tinkers_thinking.common.register.ModEntities;
import com.creeping_creeper.tinkers_thinking.common.register.ModFluids;
import com.creeping_creeper.tinkers_thinking.common.register.ModModifiers;
import com.creeping_creeper.tinkers_thinking.common.register.ModModule;
import com.creeping_creeper.tinkers_thinking.common.register.ModPotions;
import com.creeping_creeper.tinkers_thinking.common.register.ModSounds;
import com.creeping_creeper.tinkers_thinking.common.register.ModToolItems;
import com.creeping_creeper.tinkers_thinking.data.ModDataKeys;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.TinkerDataKey;
import modernmods.modernfoundry.library.utils.Util;

@Mod("tinkers_thinking")
@EventBusSubscriber(
   bus = Bus.MOD
)
public class TinkersThinking {
   public static final String MODID = "tinkers_thinking";
   public static final Logger LOGGER = LogUtils.getLogger();

   public TinkersThinking(IEventBus modBus) {
      IEventBus bus = modBus;
      ModMessages.register(bus);
      ModRecipes.registers(bus);
      ModPotions.registers(bus);
      ModSounds.SOUND_EVENTS.register(bus);
      bus.register(new ModModifiers());
      bus.addListener(this::commonSetup);
      NeoForge.EVENT_BUS.register(this);
      ModModifiers.init(bus);
      if (net.neoforged.fml.loading.FMLEnvironment.dist == Dist.CLIENT) { ClientEvents.onConstruct(); }
      bus.register(new ModCommonItems());
      bus.register(new ModToolItems());
      bus.register(new ModFluids());
      bus.register(new ModEffects());
      bus.register(new ModBlockEntities());
      bus.register(new ModEntities());
      ModModule.initRegisters();
      ModDataKeys.init();
   }

   public void commonSetup(FMLCommonSetupEvent event) {
      NeoForge.EVENT_BUS.register(new OnExpPickUp());
      NeoForge.EVENT_BUS.register(new OnDeath());
      NeoForge.EVENT_BUS.register(new Onhurt());
      if (net.neoforged.fml.loading.FMLEnvironment.dist == Dist.CLIENT) { ModSlots.init(); }
      ModPotions.init();
   }

   public static String makeTranslationKey(String base, String name) {
      return Util.makeTranslationKey(base, getResource(name));
   }

   public static MutableComponent makeTranslation(String base, String name) {
      return Component.translatable(makeTranslationKey(base, name));
   }

   public static MutableComponent makeTranslation(String base, String name, Object... arguments) {
      return Component.translatable(makeTranslationKey(base, name), arguments);
   }

   public static String makeDescriptionId(String type, String name) {
      return type + ".tinkers_thinking." + name;
   }

   public static ResourceLocation getResource(String name) {
      return ResourceLocation.fromNamespaceAndPath("tinkers_thinking", name);
   }

   public static <T> TinkerDataKey<T> createKey(String name) {
      return TinkerDataKey.of(getResource(name));
   }
}
