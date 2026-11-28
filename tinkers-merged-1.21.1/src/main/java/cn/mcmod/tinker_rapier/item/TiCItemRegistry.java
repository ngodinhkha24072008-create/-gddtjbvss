package cn.mcmod.tinker_rapier.item;

import cn.mcmod.tinker_rapier.RapierMain;
import net.minecraft.world.item.Item.Properties;
import modernmods.hilt.registration.object.ItemObject;
import modernmods.modernfoundry.common.registration.CastItemObject;
import modernmods.modernfoundry.common.registration.ItemDeferredRegisterExtension;
import modernmods.modernfoundry.library.tools.part.ToolPartItem;
import modernmods.modernfoundry.tools.stats.HeadMaterialStats;

public class TiCItemRegistry {
   public static final ItemDeferredRegisterExtension ITEMS = new ItemDeferredRegisterExtension("tinker_rapier");
   public static final ItemObject<ToolPartItem> SLENDER_BLADE = ITEMS.register(
      "slender_blade", () -> new ToolPartItem(RapierMain.defaultItemProperties(), HeadMaterialStats.ID)
   );
   public static final CastItemObject SLENDER_BLADE_CAST = ITEMS.registerCast("slender_blade", new Properties());
   public static final ItemObject<RapierTiC> RAPIER = ITEMS.register("rapier_tic", RapierTiC::new);
   public static final ItemObject<EstocTiC> ESTOC = ITEMS.register("estoc_tic", EstocTiC::new);
}
