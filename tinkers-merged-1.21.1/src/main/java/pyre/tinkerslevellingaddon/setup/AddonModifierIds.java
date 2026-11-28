package pyre.tinkerslevellingaddon.setup;

import modernmods.modernfoundry.library.modifiers.ModifierId;

public class AddonModifierIds {
   public static ModifierId IMPROVABLE = create("improvable");

   public static ModifierId create(String name) {
      return new ModifierId("tinkerslevellingaddon", name);
   }

   private AddonModifierIds() {
   }
}
