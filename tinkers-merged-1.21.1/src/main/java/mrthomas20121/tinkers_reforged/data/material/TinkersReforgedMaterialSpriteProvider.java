package mrthomas20121.tinkers_reforged.data.material;

import mrthomas20121.tinkers_reforged.api.data.Metal;
import modernmods.modernfoundry.library.client.data.material.AbstractMaterialSpriteProvider;
import modernmods.modernfoundry.library.client.data.spritetransformer.GreyToColorMapping;

public class TinkersReforgedMaterialSpriteProvider extends AbstractMaterialSpriteProvider {
   public String getName() {
      return "Tinkers Reforged Material Sprite Provider";
   }

   protected void addAllMaterials() {
      this.buildMaterial(Metal.SLIMEBRONZE.asMaterial())
         .meleeHarvest()
         .armor()
         .ranged()
         .fallbacks(new String[]{"metal"})
         .colorMapper(
            GreyToColorMapping.builderFromBlack()
               .addARGB(63, -13353682)
               .addARGB(102, -12168642)
               .addARGB(140, -10523053)
               .addARGB(178, -9009564)
               .addARGB(216, -7035781)
               .addARGB(255, -5193327)
               .build()
         );
      this.buildMaterial(Metal.GRAPHITE.asMaterial())
         .meleeHarvest()
         .armor()
         .ranged()
         .fallbacks(new String[]{"metal"})
         .colorMapper(
            GreyToColorMapping.builderFromBlack()
               .addARGB(63, -14935008)
               .addARGB(102, -14276819)
               .addARGB(140, -13619144)
               .addARGB(178, -12500404)
               .addARGB(216, -11513250)
               .addARGB(255, -10197132)
               .build()
         );
      this.buildMaterial(Metal.YTTRIUM.asMaterial())
         .meleeHarvest()
         .armor()
         .ranged()
         .fallbacks(new String[]{"metal"})
         .colorMapper(
            GreyToColorMapping.builderFromBlack()
               .addARGB(63, -12250344)
               .addARGB(102, -10938596)
               .addARGB(140, -7461847)
               .addARGB(178, -5493710)
               .addARGB(216, -2469289)
               .addARGB(255, -38032)
               .build()
         );
      this.buildMaterial(Metal.ASTATINE.asMaterial())
         .meleeHarvest()
         .armor()
         .ranged()
         .fallbacks(new String[]{"metal"})
         .colorMapper(
            GreyToColorMapping.builderFromBlack()
               .addARGB(63, -1343402)
               .addARGB(102, -1216197)
               .addARGB(140, -1469323)
               .addARGB(178, -1460578)
               .addARGB(216, -532791)
               .addARGB(255, -3106)
               .build()
         );
      this.buildMaterial(Metal.BARIUM.asMaterial())
         .meleeHarvest()
         .armor()
         .ranged()
         .fallbacks(new String[]{"metal"})
         .colorMapper(
            GreyToColorMapping.builderFromBlack()
               .addARGB(63, -14017189)
               .addARGB(102, -13162388)
               .addARGB(140, -11451761)
               .addARGB(178, -9543510)
               .addARGB(216, -7372082)
               .addARGB(255, -5332752)
               .build()
         );
      this.buildMaterial(Metal.TELLURIUM.asMaterial())
         .meleeHarvest()
         .armor()
         .ranged()
         .fallbacks(new String[]{"metal"})
         .colorMapper(
            GreyToColorMapping.builderFromBlack()
               .addARGB(63, -11393240)
               .addARGB(102, -9817809)
               .addARGB(140, -7454403)
               .addARGB(178, -5877426)
               .addARGB(216, -3379088)
               .addARGB(255, -1211003)
               .build()
         );
      this.buildMaterial(Metal.ACTINIUM.asMaterial())
         .meleeHarvest()
         .armor()
         .ranged()
         .fallbacks(new String[]{"metal"})
         .colorMapper(
            GreyToColorMapping.builderFromBlack()
               .addARGB(63, -12566993)
               .addARGB(102, -11580356)
               .addARGB(140, -10067633)
               .addARGB(178, -8028308)
               .addARGB(216, -4410206)
               .addARGB(255, -2108225)
               .build()
         );
      this.buildMaterial(Metal.THALLIUM.asMaterial())
         .meleeHarvest()
         .armor()
         .ranged()
         .fallbacks(new String[]{"metal"})
         .colorMapper(
            GreyToColorMapping.builderFromBlack()
               .addARGB(63, -14141636)
               .addARGB(102, -13088691)
               .addARGB(140, -11245972)
               .addARGB(178, -9666683)
               .addARGB(216, -6113604)
               .addARGB(255, -4204577)
               .build()
         );
      this.buildMaterial(Metal.BERKELIUM.asMaterial())
         .meleeHarvest()
         .armor()
         .ranged()
         .fallbacks(new String[]{"metal"})
         .colorMapper(
            GreyToColorMapping.builderFromBlack()
               .addARGB(63, -12561378)
               .addARGB(102, -11311579)
               .addARGB(140, -8943819)
               .addARGB(178, -6444713)
               .addARGB(216, -3814280)
               .addARGB(255, -2103670)
               .build()
         );
      this.buildMaterial(Metal.GERMANIUM.asMaterial())
         .meleeHarvest()
         .armor()
         .ranged()
         .fallbacks(new String[]{"metal"})
         .colorMapper(
            GreyToColorMapping.builderFromBlack()
               .addARGB(63, -11790276)
               .addARGB(102, -9888173)
               .addARGB(140, -6674054)
               .addARGB(178, -5419378)
               .addARGB(216, -4100442)
               .addARGB(255, -2784841)
               .build()
         );
   }
}
