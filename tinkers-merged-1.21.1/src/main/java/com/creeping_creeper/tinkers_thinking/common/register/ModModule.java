package com.creeping_creeper.tinkers_thinking.common.register;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import modernmods.hilt.item.BlockTooltipItem;
import modernmods.hilt.item.TooltipItem;
import modernmods.hilt.registration.deferred.BlockEntityTypeDeferredRegister;
import modernmods.hilt.registration.deferred.EntityTypeDeferredRegister;
import modernmods.hilt.registration.deferred.EnumDeferredRegister;
import modernmods.hilt.registration.deferred.SynchronizedDeferredRegister;
import modernmods.modernfoundry.common.registration.BlockDeferredRegisterExtension;
import modernmods.modernfoundry.common.registration.FluidDeferredRegisterExtension;
import modernmods.modernfoundry.common.registration.ItemDeferredRegisterExtension;

public abstract class ModModule {
   protected static final BlockDeferredRegisterExtension BLOCKS = new BlockDeferredRegisterExtension("tinkers_thinking");
   protected static final ItemDeferredRegisterExtension ITEMS = new ItemDeferredRegisterExtension("tinkers_thinking");
   protected static final FluidDeferredRegisterExtension FLUIDS = new FluidDeferredRegisterExtension("tinkers_thinking");
   protected static final SynchronizedDeferredRegister<CreativeModeTab> CREATIVE_TABS = SynchronizedDeferredRegister.create(
      Registries.CREATIVE_MODE_TAB, "tinkers_thinking"
   );
   protected static final EnumDeferredRegister<MobEffect> MOB_EFFECTS = new EnumDeferredRegister(Registries.MOB_EFFECT, "tinkers_thinking");
   protected static final EntityTypeDeferredRegister ENTITIES = new EntityTypeDeferredRegister("tinkers_thinking");
   protected static final BlockEntityTypeDeferredRegister BLOCK_ENTITIES = new BlockEntityTypeDeferredRegister("tinkers_thinking");
   protected static final Properties GENERAL_PROPS = new Properties();
   protected static final Properties FIRE_PROPS = new Properties().fireResistant();
   protected static final Supplier<Item> TOOLTIP_ITEM = () -> new TooltipItem(GENERAL_PROPS);
   protected static final Function<Block, ? extends BlockItem> GENERAL_BLOCK_ITEM = b -> new BlockItem(b, GENERAL_PROPS);
   protected static final Function<Block, ? extends BlockItem> FIRE_BLOCK_ITEM = b -> new BlockItem(b, FIRE_PROPS);
   protected static final Properties Stack1Item = new Properties().stacksTo(1);
   protected static final Function<Block, ? extends BlockItem> GENERAL_TOOLTIP_BLOCK_ITEM = b -> new BlockTooltipItem(b, GENERAL_PROPS);
   protected static final Function<Block, ? extends BlockItem> FIRE_TOOLTIP_BLOCK_ITEM = b -> new BlockTooltipItem(b, FIRE_PROPS);

   public static void initRegisters() {
      IEventBus bus = net.neoforged.fml.ModLoadingContext.get().getActiveContainer().getEventBus();
      BLOCKS.register(bus);
      ITEMS.register(bus);
      FLUIDS.register(bus);
      CREATIVE_TABS.register(bus);
      MOB_EFFECTS.register(bus);
      BLOCK_ENTITIES.register(bus);
      ENTITIES.register(bus);
   }
}
