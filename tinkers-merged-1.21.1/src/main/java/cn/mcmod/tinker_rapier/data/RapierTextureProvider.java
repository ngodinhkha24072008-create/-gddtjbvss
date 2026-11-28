package cn.mcmod.tinker_rapier.data;

import modernmods.modernfoundry.library.client.data.material.AbstractPartSpriteProvider;

public class RapierTextureProvider extends AbstractPartSpriteProvider {
   public RapierTextureProvider() {
      super("tinker_rapier");
   }

   protected void addAllSpites() {
      this.addHead("slender_blade");
      this.buildTool("rapier").addBreakableHead("blade").addBinding("guard_cup").addHandle("guard").addHandle("handle");
      this.buildTool("estoc").addBreakableHead("blade").addHandle("guard_estoc").addHandle("handle");
   }

   public String getName() {
      return "Tinker's Rapier Tool Texture";
   }
}
