package mrthomas20121.tinkers_reforged.api.registry;

import java.util.EnumMap;
import java.util.Locale;
import mrthomas20121.tinkers_reforged.api.holder.ItemGemObject;
import mrthomas20121.tinkers_reforged.api.holder.ItemMetalObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import modernmods.hilt.registration.object.ItemObject;
import modernmods.modernfoundry.common.registration.ItemDeferredRegisterExtension;

public class ReforgedItemDeferredRegister extends ItemDeferredRegisterExtension {
   public ReforgedItemDeferredRegister(String modID) {
      super(modID);
   }

   public ItemMetalObject registerMetal(String metalName) {
      ItemObject<Item> ingot = this.register(metalName + "_ingot");
      ItemObject<Item> nugget = this.register(metalName + "_nugget");
      ItemObject<Item> dust = this.register(metalName + "_dust");
      ItemObject<Item> plate = this.register(metalName + "_plate");
      ItemObject<Item> gear = this.register(metalName + "_gear");
      return new ItemMetalObject(ResourceLocation.fromNamespaceAndPath(this.modID, metalName), ingot, nugget, dust, plate, gear);
   }

   public <E extends Enum<E>> EnumMap<E, ItemMetalObject> registerEnumMetal(Class<E> e) {
      EnumMap<E, ItemMetalObject> map = new EnumMap<>(e);
      E[] values = (E[])e.getEnumConstants();

      for (E value : values) {
         map.put(value, this.registerMetal(value.name().toLowerCase(Locale.ROOT)));
      }

      return map;
   }

   public ItemGemObject registerGem(String metalName) {
      ItemObject<Item> gem = this.register(metalName + "_gem");
      ItemObject<Item> nugget = this.register(metalName + "_nugget");
      ItemObject<Item> dust = this.register(metalName + "_dust");
      ItemObject<Item> plate = this.register(metalName + "_plate");
      ItemObject<Item> gear = this.register(metalName + "_gear");
      return new ItemGemObject(ResourceLocation.fromNamespaceAndPath(this.modID, metalName), gem, nugget, dust, plate, gear);
   }
}
