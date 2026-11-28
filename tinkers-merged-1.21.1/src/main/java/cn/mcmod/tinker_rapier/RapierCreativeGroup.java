package cn.mcmod.tinker_rapier;

import cn.mcmod.tinker_rapier.item.RapierTiC;
import cn.mcmod.tinker_rapier.item.TiCItemRegistry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import modernmods.hilt.registration.object.ItemObject;
import modernmods.modernfoundry.common.registration.CastItemObject;
import modernmods.modernfoundry.library.tools.helper.ToolBuildHandler;
import modernmods.modernfoundry.library.tools.item.IModifiable;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;

public class RapierCreativeGroup {
   public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "tinker_rapier");
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS = CREATIVE_MODE_TABS.register(
      "umapyoi",
      () -> CreativeModeTab.builder()
         .icon(() -> ((RapierTiC)TiCItemRegistry.RAPIER.get()).getRenderTool())
         .title(Component.translatable("itemGroup.tinker_rapier"))
         .displayItems(RapierCreativeGroup::addTabItems)
         .build()
   );

   private static void addTabItems(ItemDisplayParameters itemDisplayParameters, Output tab) {
      Consumer<ItemStack> output = tab::accept;
      acceptPart(output, TiCItemRegistry.SLENDER_BLADE);
      acceptCast(tab, ItemObject::get, TiCItemRegistry.SLENDER_BLADE_CAST);
      acceptCast(tab, CastItemObject::getSand, TiCItemRegistry.SLENDER_BLADE_CAST);
      acceptCast(tab, CastItemObject::getRedSand, TiCItemRegistry.SLENDER_BLADE_CAST);
      acceptTool(output, TiCItemRegistry.ESTOC);
      acceptTool(output, TiCItemRegistry.RAPIER);
   }

   private static void acceptPart(Consumer<ItemStack> output, Supplier<? extends IMaterialItem> item) {
      item.get().addVariants(output, "");
   }

   private static void acceptCast(Output output, Function<CastItemObject, ItemLike> getter, CastItemObject cast) {
      output.accept(getter.apply(cast));
   }

   private static void acceptTool(Consumer<ItemStack> output, Supplier<? extends IModifiable> tool) {
      ToolBuildHandler.addVariants(output, tool.get(), "");
   }
}
