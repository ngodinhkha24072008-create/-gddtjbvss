package pyre.tinkerslevellingaddon.util;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.setup.AddonModifierIds;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectContext;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectManager;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffects;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ProcessLootModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.armor.ArmorWalkRadiusModule;
import modernmods.modernfoundry.library.recipe.TinkerRecipeTypes;
import modernmods.modernfoundry.library.recipe.melting.IMeltingContainer;
import modernmods.modernfoundry.library.recipe.melting.IMeltingRecipe;
import modernmods.modernfoundry.library.recipe.melting.IMeltingContainer.IOreRate;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.tools.data.ModifierIds;
import modernmods.modernfoundry.tools.modules.MeltingModule;

public class ToolUtil {
   public static float getWalkerTrueRadius(IToolStackView tool, ModifierEntry entry, ArmorWalkRadiusModule<?> module) {
      return Math.min(16.0F, module.getRadius(tool, entry));
   }

   public static boolean isAttackAllowed(ToolAttackContext context, IToolStackView tool) {
      ModifierEntry activeModifier = GeneralInteractionModifierHook.getActiveModifier(tool);
      return !context.isExtraAttack() || activeModifier == ModifierEntry.EMPTY || ModifierIds.bonking.equals(activeModifier.getId());
   }

   public static boolean isValidMeleeTarget(ServerPlayer attacker, ToolAttackContext context) {
      return context.getLivingTarget() != null
         && !(context.getTarget() instanceof ArmorStand)
         && ((Boolean)Config.enablePvp.get() || !(context.getLivingTarget() instanceof Player))
         && !context.getLivingTarget().equals(attacker);
   }

   public static boolean isValidLauncherTarget(ServerPlayer attacker, Entity target) {
      return !(target instanceof ArmorStand) && ((Boolean)Config.enablePvp.get() || !(target instanceof Player)) && !target.equals(attacker);
   }

   public static boolean isValidDamageSource(DamageSource source, Player player) {
      return !source.is(DamageTypeTags.BYPASSES_ARMOR)
         && source.getEntity() instanceof LivingEntity attacker
         && !attacker.equals(player)
         && ((Boolean)Config.enablePvp.get() || !(attacker instanceof Player));
   }

   public static float getDamageBlockedAmount(ServerPlayer player, DamageSource source, float amount) {
      ItemStack activeStack = player.getUseItem();
      if (hasImprovable(activeStack)) {
         ToolStack tool = ToolStack.from(activeStack);
         if (!tool.isBroken()) {
            float blockAngle = ConditionalStatModifierHook.getModifiedStat(tool, player, ToolStats.BLOCK_ANGLE) / 2.0F;
            if (canBlock(player, source.getSourcePosition(), blockAngle)) {
               return (Float)tool.getStats().get(ToolStats.BLOCK_AMOUNT);
            }
         }
      } else if (activeStack.getItem() instanceof ShieldItem && canBlock(player, source.getSourcePosition(), 90.0F)) {
         return amount;
      }

      return 0.0F;
   }

   public static boolean canBlock(ServerPlayer player, @Nullable Vec3 sourcePosition, float blockAngle) {
      if (sourcePosition == null) {
         return false;
      } else {
         Vec3 viewVector = player.getViewVector(1.0F);
         Vec3 entityPosition = player.position();
         Vec3 direction = new Vec3(entityPosition.x - sourcePosition.x, 0.0, entityPosition.z - sourcePosition.z);
         double length = viewVector.length() * direction.length();
         if (length < 1.0E-4) {
            return false;
         } else {
            double angle = Math.abs(180.0 - Math.acos(direction.dot(viewVector) / length) * 180.0F / (float)Math.PI);
            return blockAngle >= angle;
         }
      }
   }

   public static boolean canSlurp(IToolStackView tool, ServerPlayer player, ModifierEntry slurping) {
      FluidStack fluid = ToolTankHelper.TANK_HELPER.getFluid(tool);
      if (fluid.isEmpty()) {
         return false;
      } else {
         FluidEffects recipe = FluidEffectManager.INSTANCE.find(fluid.getFluid());
         return recipe.hasEntityEffects()
            && recipe.applyToEntity(
                  fluid,
                  LevelingValue.eachLevel(1.0F).compute(slurping),
                  FluidEffectContext.builder(player.level()).user(player, player).target(player),
                  FluidAction.SIMULATE
               )
               > 0;
      }
   }

   public static FluidStack getMeltItemResult(ModifierEntry modifier, final ItemStack stack, Level world) {
      ProcessLootModifierHook hook = (ProcessLootModifierHook)modifier.getModifier().getHook(ModifierHooks.PROCESS_LOOT);
      if (hook instanceof MeltingModule melting) {
         int level = modifier.intEffectiveLevel();
         IMeltingContainer inv = new IMeltingContainer() {
            public IOreRate getOreRate() {
               return melting.getOreRate();
            }

            public ItemStack getStack() {
               return stack;
            }
         };
         IMeltingRecipe recipe = (IMeltingRecipe)world.getRecipeManager().getRecipeFor((RecipeType)TinkerRecipeTypes.MELTING.get(), inv, world).orElse(null);
         if (recipe == null) {
            return FluidStack.EMPTY;
         } else {
            FluidStack result = FluidStack.EMPTY;
            if (recipe.getTemperature(inv) <= melting.temperature().compute(level)) {
               result = recipe.getOutput(inv);
            }

            return result;
         }
      } else {
         return FluidStack.EMPTY;
      }
   }

   public static boolean hasImprovable(ItemStack activeStack) {
      return ModifierUtil.getModifierLevel(activeStack, AddonModifierIds.IMPROVABLE) > 0;
   }

   public static HitResult calculateHitResult(LivingEntity living) {
      return ProjectileUtil.getHitResultOnViewVector(living, entity -> !entity.isSpectator() && entity.isPickable(), living.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE));
   }

   private ToolUtil() {
   }
}
