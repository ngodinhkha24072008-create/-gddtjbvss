package mrthomas20121.tinkers_reforged.init;

import java.util.EnumMap;
import mrthomas20121.tinkers_reforged.TinkersReforged;
import mrthomas20121.tinkers_reforged.api.data.Metal;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType.Properties;
import modernmods.hilt.block.fluid.BurningLiquidBlock;
import modernmods.hilt.registration.object.FlowingFluidObject;
import modernmods.modernfoundry.common.registration.FluidDeferredRegisterExtension;

public class TinkersReforgedFluids {
   public static FluidDeferredRegisterExtension FLUIDS = new FluidDeferredRegisterExtension("tinkers_reforged");
   public static FlowingFluidObject<BaseFlowingFluid> CHARRED_PRISMARINE = FLUIDS.registerStone("charred_prismarine")
      .type(hot("charred_prismarine").temperature(500))
      .block(BurningLiquidBlock.createBurning(MapColor.COLOR_LIGHT_BLUE, 12, 10, 2.0F))
      .bucket()
      .commonTag()
      .flowing();
   public static FlowingFluidObject<BaseFlowingFluid> MOLTEN_URANOPHANE = FLUIDS.registerMetal("molten_uranophane")
      .type(hot("molten_uranophane").temperature(2300))
      .block(BurningLiquidBlock.createBurning(MapColor.COLOR_BLUE, 12, 10, 4.0F))
      .bucket()
      .commonTag()
      .flowing();
   public static FlowingFluidObject<BaseFlowingFluid> MOLTEN_SERANDITE = FLUIDS.registerMetal("molten_serandite")
      .type(hot("molten_serandite").temperature(3300))
      .block(BurningLiquidBlock.createBurning(MapColor.COLOR_RED, 12, 10, 4.0F))
      .bucket()
      .commonTag()
      .flowing();
   public static EnumMap<Metal, FlowingFluidObject<BaseFlowingFluid>> METALS = registerEnumMetal();

   private static EnumMap<Metal, FlowingFluidObject<BaseFlowingFluid>> registerEnumMetal() {
      EnumMap<Metal, FlowingFluidObject<BaseFlowingFluid>> map = new EnumMap<>(Metal.class);
      Metal[] values = Metal.values;

      for (Metal metal : values) {
         String name = "molten_" + metal.getSerializedName();
         map.put(
            metal,
            FLUIDS.registerMetal(name)
               .type(hot(name).temperature(metal.getTemperature()).lightLevel(12))
               .block(BurningLiquidBlock.createBurning(metal.getMapColor(), 12, 10, 5.0F))
               .bucket()
               .commonTag()
               .flowing()
         );
      }

      return map;
   }

   private static Properties cool() {
      return Properties.create()
         .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
         .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
         .motionScale(0.0023333333333333335)
         .canExtinguish(true);
   }

   private static Properties cool(String name) {
      return cool()
         .descriptionId(TinkersReforged.makeDescriptionId("fluid", name))
         .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
         .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
   }

   private static Properties slime(String name) {
      return cool(name).density(1600).viscosity(1600);
   }

   private static Properties powder(String name) {
      return Properties.create()
         .descriptionId(TinkersReforged.makeDescriptionId("fluid", name))
         .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_POWDER_SNOW)
         .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
   }

   private static Properties hot(String name) {
      return Properties.create()
         .density(2000)
         .viscosity(10000)
         .temperature(1000)
         .descriptionId(TinkersReforged.makeDescriptionId("fluid", name))
         .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
         .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
         .motionScale(0.0023333333333333335)
         .canSwim(false)
         .canDrown(false)
         .pathType(PathType.LAVA)
         .adjacentPathType(null);
   }
}
