package mrthomas20121.tinkers_reforged.api;

import java.util.Locale;
import mrthomas20121.tinkers_reforged.api.data.Metal;
import mrthomas20121.tinkers_reforged.init.TinkersReforgedFluids;
import modernmods.hilt.recipe.helper.FluidOutput;
import modernmods.hilt.registration.object.FluidObject;
import modernmods.modernfoundry.library.data.recipe.IByproduct;
import modernmods.modernfoundry.library.recipe.melting.IMeltingContainer.OreRateType;

public enum TinkersReforgedByproduct implements IByproduct {
   GRAPHITE(true, (FluidObject<?>)TinkersReforgedFluids.METALS.get(Metal.GRAPHITE));

   private final String name;
   private final boolean alwaysPresent;
   private final FluidObject<?> fluid;
   private final int amount;
   private final int damageUnit;
   private final OreRateType oreRate;

   private TinkersReforgedByproduct(boolean alwaysPresent, FluidObject<?> fluid, int amount, int damageUnit, OreRateType oreRate) {
      this.name = this.name().toLowerCase(Locale.ROOT);
      this.alwaysPresent = alwaysPresent;
      this.fluid = fluid;
      this.amount = amount;
      this.damageUnit = damageUnit;
      this.oreRate = oreRate;
   }

   private TinkersReforgedByproduct(boolean alwaysPresent, FluidObject<?> fluid) {
      this(alwaysPresent, fluid, 90, 10, OreRateType.METAL);
   }

   public FluidOutput getFluid(float scale) {
      return this.fluid.result((int)(this.amount * scale));
   }

   private TinkersReforgedByproduct(String name, boolean alwaysPresent, FluidObject<?> fluid, int amount, int damageUnit, OreRateType oreRate) {
      this.name = name;
      this.alwaysPresent = alwaysPresent;
      this.fluid = fluid;
      this.amount = amount;
      this.damageUnit = damageUnit;
      this.oreRate = oreRate;
   }

   public String getName() {
      return this.name;
   }

   public boolean isAlwaysPresent() {
      return this.alwaysPresent;
   }

   public FluidObject<?> getFluid() {
      return this.fluid;
   }

   public int getAmount() {
      return this.amount;
   }

   public int getDamageUnit() {
      return this.damageUnit;
   }

   public OreRateType getOreRate() {
      return this.oreRate;
   }
}
