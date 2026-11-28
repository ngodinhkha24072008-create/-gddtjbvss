package com.creeping_creeper.tinkers_thinking.data;

import modernmods.modernfoundry.library.materials.definition.MaterialId;

public class ModMaterialIds {
   public static final MaterialId electrical_steel = id("electrical_steel");

   private static MaterialId id(String name) {
      return new MaterialId("tinkers_thinking", name);
   }
}
