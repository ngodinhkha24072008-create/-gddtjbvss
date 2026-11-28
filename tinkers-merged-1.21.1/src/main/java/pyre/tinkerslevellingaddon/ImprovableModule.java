package pyre.tinkerslevellingaddon;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.TickEvent.Phase;
import net.neoforged.neoforge.event.TickEvent.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.ShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.jetbrains.annotations.Nullable;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.util.ModUtil;
import pyre.tinkerslevellingaddon.util.ToolLevellingUtil;
import pyre.tinkerslevellingaddon.util.ToolUtil;
import pyre.tinkerslevellingaddon.util.WorldUtil;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.common.TinkerTags.Items;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectContext;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectManager;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffects;
import modernmods.modernfoundry.library.modifiers.hook.armor.ArmorWalkModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.armor.ElytraFlightModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ProcessLootModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ToolStatsModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.VolatileDataModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.EntityInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.UsingToolModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.BlockInteractionModifierHook.FirstMerger;
import modernmods.modernfoundry.library.modifiers.hook.mining.BlockBreakModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.LauncherHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.BlockTransformModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.PlantHarvestModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.ShearsModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.sling.SlingLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.ArmorWalkRadiusModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.CoverGroundWalkerModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.ReplaceBlockWalkerModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.ToolActionWalkerTransformModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.recipe.entitymelting.EntityMeltingRecipe;
import modernmods.modernfoundry.library.recipe.entitymelting.EntityMeltingRecipeCache;
import modernmods.modernfoundry.library.tools.SlotType;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.context.ToolHarvestContext;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.aoe.AreaOfEffectIterator;
import modernmods.modernfoundry.library.tools.definition.module.aoe.CircleAOEIterator;
import modernmods.modernfoundry.library.tools.definition.module.aoe.AreaOfEffectIterator.AOEMatchType;
import modernmods.modernfoundry.library.tools.definition.module.interaction.InteractionToolModule;
import modernmods.modernfoundry.library.tools.definition.module.mining.IsEffectiveToolHook;
import modernmods.modernfoundry.library.tools.definition.module.weapon.CircleWeaponAttack;
import modernmods.modernfoundry.library.tools.definition.module.weapon.MeleeHitToolHook;
import modernmods.modernfoundry.library.tools.definition.module.weapon.SweepWeaponAttack;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.item.IModifiable;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ModifierStatsBuilder;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.library.utils.MutableUseOnContext;
import modernmods.modernfoundry.library.utils.Util;
import modernmods.modernfoundry.smeltery.block.entity.module.EntityMeltingModule;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.data.ModifierIds;
import modernmods.modernfoundry.tools.entity.CustomFireball;
import modernmods.modernfoundry.tools.entity.FluidEffectProjectile;
import modernmods.modernfoundry.tools.modules.MeltingModule;
import modernmods.modernfoundry.tools.modules.armor.FireWalkerModule;
import modernmods.modernfoundry.tools.modules.armor.GlowWalkerModule;
import modernmods.modernfoundry.tools.modules.combat.SpillingModule;
import modernmods.modernfoundry.tools.modules.interaction.BucketModule;
import modernmods.modernfoundry.tools.modules.interaction.SplashingModule;

@EventBusSubscriber(
   modid = "tinkerslevellingaddon",
   bus = Bus.FORGE
)
public enum ImprovableModule
   implements ModifierModule,
   PlantHarvestModifierHook,
   ShearsModifierHook,
   BlockBreakModifierHook,
   BlockTransformModifierHook,
   ProjectileLaunchModifierHook,
   OnAttackedModifierHook,
   MeleeHitModifierHook,
   LauncherHitModifierHook,
   ElytraFlightModifierHook,
   ArmorWalkModifierHook,
   UsingToolModifierHook,
   InventoryTickModifierHook,
   EntityInteractionModifierHook,
   SlingLaunchModifierHook,
   BlockInteractionModifierHook,
   ProcessLootModifierHook,
   ModifierRemovalHook,
   VolatileDataModifierHook,
   ToolStatsModifierHook {
   INSTANCE;

   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(
      new ModuleHook[]{
         ModifierHooks.PLANT_HARVEST,
         ModifierHooks.SHEAR_ENTITY,
         ModifierHooks.TOOL_USING,
         ModifierHooks.ENTITY_INTERACT,
         ModifierHooks.BLOCK_TRANSFORM,
         ModifierHooks.BLOCK_BREAK,
         ModifierHooks.BLOCK_INTERACT,
         ModifierHooks.ON_ATTACKED,
         ModifierHooks.MELEE_HIT,
         ModifierHooks.LAUNCHER_HIT,
         ModifierHooks.PROJECTILE_LAUNCH,
         ModifierHooks.ELYTRA_FLIGHT,
         ModifierHooks.BOOT_WALK,
         ModifierHooks.SLING_LAUNCH,
         ModifierHooks.INVENTORY_TICK,
         ModifierHooks.PROCESS_LOOT,
         ModifierHooks.VOLATILE_DATA,
         ModifierHooks.TOOL_STATS,
         ModifierHooks.REMOVE
      }
   );
   public static final RecordLoadable<ImprovableModule> LOADER = new SingletonLoader(INSTANCE);
   public static final TextColor IMPROVABLE_MODIFIER_COLOR = TextColor.fromRgb(9337340);
   public static final ResourceLocation EXPERIENCE_KEY = ModUtil.getResource("experience");
   public static final ResourceLocation LEVEL_KEY = ModUtil.getResource("level");
   public static final ResourceLocation SLOT_HISTORY_KEY = ModUtil.getResource("slot_history");
   public static final ResourceLocation STAT_HISTORY_KEY = ModUtil.getResource("stat_history");
   public static final ResourceLocation PENDING_LEVEL_UP_KEY = ModUtil.getResource("pending_level_up");

   public RecordLoadable<? extends ModifierModule> getLoader() {
      return LOADER;
   }

   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public Integer getPriority() {
      return 300;
   }

   public Component onRemoved(IToolStackView tool, Modifier modifier) {
      tool.getPersistentData().remove(EXPERIENCE_KEY);
      tool.getPersistentData().remove(LEVEL_KEY);
      tool.getPersistentData().remove(SLOT_HISTORY_KEY);
      tool.getPersistentData().remove(STAT_HISTORY_KEY);
      tool.getPersistentData().remove(PENDING_LEVEL_UP_KEY);
      return null;
   }

   public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
      if (ToolLevellingUtil.isSlotsLevellingEnabled(context)) {
         for (SlotType slot : ToolLevellingUtil.parseSlotsHistory(context.getPersistentData().getString(SLOT_HISTORY_KEY))) {
            volatileData.addSlots(slot, 1);
         }
      }
   }

   public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
      if (ToolLevellingUtil.isStatsLevellingEnabled(context)) {
         for (FloatToolStat stat : ToolLevellingUtil.parseStatsHistory(context.getPersistentData().getString(STAT_HISTORY_KEY))) {
            stat.add(builder, ToolLevellingUtil.getStatValue(context, stat));
         }
      }
   }

   @SubscribeEvent
   static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         for (ItemStack itemStack : player.getInventory().armor) {
            ToolLevellingUtil.processPendingLevelUp(player, itemStack);
         }

         for (ItemStack itemStack : player.getInventory().items) {
            ToolLevellingUtil.processPendingLevelUp(player, itemStack);
         }

         for (ItemStack itemStack : player.getInventory().offhand) {
            ToolLevellingUtil.processPendingLevelUp(player, itemStack);
         }
      }
   }

   public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
      if ((Boolean)Config.enableMiningXp.get() && context.isEffective()) {
         ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusMiningXp.get());
      }
   }

   public void afterHarvest(IToolStackView tool, ModifierEntry modifier, UseOnContext context, ServerLevel world, BlockState state, BlockPos pos) {
      if ((Boolean)Config.enableHarvestingXp.get()) {
         ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusHarvestingXp.get());
      }
   }

   public void afterShearEntity(IToolStackView tool, ModifierEntry modifier, Player player, Entity entity, boolean isTarget) {
      if ((Boolean)Config.enableShearingXp.get()) {
         ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusShearingXp.get());
      }
   }

   public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      if (context.getPlayerAttacker() instanceof ServerPlayer player) {
         if ((Boolean)Config.enableAttackingXp.get() && ToolUtil.isAttackAllowed(context, tool) && ToolUtil.isValidMeleeTarget(player, context)) {
            int xp = (Config.damageDealt.get() ? Math.round(damageDealt) : 1) + (Integer)Config.bonusAttackingXp.get();
            ToolLevellingUtil.addExperience(tool, xp);
            MeleeHitToolHook hook = (MeleeHitToolHook)tool.getDefinitionData().getHook(ToolHooks.MELEE_HIT);
            if (hook instanceof SweepWeaponAttack sweepHook) {
               this.handleSweepAttack(context, tool, player, damageDealt, sweepHook);
            }

            if (hook instanceof CircleWeaponAttack circleHook) {
               this.handleCircleAttack(context, tool, player, damageDealt, circleHook);
            }
         }

         if ((Boolean)Config.enableSpillingXp.get() && damageDealt > 0.0F && context.isFullyCharged()) {
            this.handleSpilling(
               tool,
               tool.getModifier(ModifierIds.spilling),
               context.getAttacker(),
               context.getPlayerAttacker(),
               context.getTarget(),
               context.getLivingTarget(),
               context.getProjectile()
            );
         }

         if ((Boolean)Config.enableMeltingXp.get() && damageDealt > 0.0F && tool.getModifier(TinkerModifiers.melting.getId()) != ModifierEntry.EMPTY) {
            handleTargetMelting(tool, context.getLivingTarget());
         }
      }
   }

   public void onLauncherHitEntity(
      IToolStackView tool,
      ModifierEntry modifier,
      Projectile projectile,
      LivingEntity attacker,
      Entity target,
      @Nullable LivingEntity livingTarget,
      float damageDealt
   ) {
      if (attacker instanceof ServerPlayer player) {
         if ((Boolean)Config.enableAttackingXp.get() && !tool.isBroken() && ToolUtil.isValidLauncherTarget(player, target)) {
            int xp = (Config.damageDealt.get() ? Math.round(damageDealt) : 1) + (Integer)Config.bonusAttackingXp.get();
            ToolLevellingUtil.addExperience(tool, xp);
         }

         if ((Boolean)Config.enableSpillingXp.get() && damageDealt > 0.0F) {
            this.handleSpilling(tool, tool.getModifier(ModifierIds.spilling), attacker, ModifierUtil.asPlayer(attacker), target, livingTarget, projectile);
         }

         if ((Boolean)Config.enableMeltingXp.get() && damageDealt > 0.0F && tool.getModifier(TinkerModifiers.melting.getId()) != ModifierEntry.EMPTY) {
            handleTargetMelting(tool, livingTarget);
         }
      }
   }

   public void onLauncherHitBlock(IToolStackView tool, ModifierEntry modifier, Projectile projectile, LivingEntity owner, BlockPos target) {
      if (!tool.isBroken() && owner instanceof ServerPlayer && (Boolean)Config.enableGrapplingXp.get() && projectile instanceof FishingHook) {
         ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusGrapplingXp.get());
      }
   }

   @SubscribeEvent
   static void onItemFished(ItemFishedEvent event) {
      if ((Boolean)Config.enableFishingXp.get() && event.getEntity() instanceof ServerPlayer player) {
         ItemStack stack = player.getMainHandItem();
         if (!stack.canPerformAction(ItemAbilities.FISHING_ROD_CAST)) {
            stack = player.getOffhandItem();
            if (!stack.canPerformAction(ItemAbilities.FISHING_ROD_CAST)) {
               return;
            }
         }

         if (ToolUtil.hasImprovable(stack)) {
            ToolLevellingUtil.addExperience(ToolStack.from(stack), 1 + (Integer)Config.bonusFishingXp.get());
         }
      }
   }

   public void onAttacked(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage
   ) {
      if ((Boolean)Config.enableTakingDamageXp.get()
         && slotType.getType() == Type.ARMOR
         && isDirectDamage
         && context.getEntity() instanceof ServerPlayer player
         && player.invulnerableTime <= 10
         && ToolUtil.isValidDamageSource(source, player)) {
         float damageBlocked = ToolUtil.getDamageBlockedAmount(player, source, amount);
         if (!(amount <= damageBlocked)) {
            int xp = (Config.damageTaken.get() ? Math.round(amount - damageBlocked) : 1)
               + (Integer)Config.bonusTakingDamageXp.get()
               + ToolLevellingUtil.getThornsBonus(tool);
            ToolLevellingUtil.addExperience(tool, xp);
         }
      }
   }

   public void onProjectileLaunch(
      IToolStackView tool,
      ModifierEntry modifier,
      LivingEntity shooter,
      Projectile projectile,
      @Nullable AbstractArrow arrow,
      ModDataNBT persistentData,
      boolean primary
   ) {
      if (shooter instanceof ServerPlayer && !(projectile instanceof FishingHook)) {
         if ((Boolean)Config.enableShootingXp.get() && arrow != null) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusShootingXp.get());
         }

         if ((Boolean)Config.enableSpittingXp.get() && projectile instanceof FluidEffectProjectile) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusSpittingXp.get());
         }

         if ((Boolean)Config.enableSlimeballXp.get() && projectile instanceof CustomFireball) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusSlimeballXp.get());
         }
      }
   }

   public void afterTransformBlock(IToolStackView tool, ModifierEntry modifier, UseOnContext context, BlockState state, BlockPos pos, ItemAbility action) {
      if (context.getPlayer() instanceof ServerPlayer) {
         if ((Boolean)Config.enableStrippingXp.get() && action.equals(ItemAbilities.AXE_STRIP)) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusStrippingXp.get());
         } else if ((Boolean)Config.enableScrappingXp.get() && action.equals(ItemAbilities.AXE_SCRAPE)) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusScrappingXp.get());
         } else if ((Boolean)Config.enableWaxingOffXp.get() && action.equals(ItemAbilities.AXE_WAX_OFF)) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusWaxingOffXp.get());
         } else if ((Boolean)Config.enableTillingXp.get() && action.equals(ItemAbilities.HOE_TILL)) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusTillingXp.get());
         } else if ((Boolean)Config.enablePathMakingXp.get() && action.equals(ItemAbilities.SHOVEL_FLATTEN)) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusPathMakingXp.get());
         }
      }
   }

   public boolean elytraFlightTick(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int flightTicks) {
      if ((Boolean)Config.enableFlyingXp.get() && entity instanceof ServerPlayer) {
         if (flightTicks > 0 && flightTicks % (Integer)Config.flyingTime.get() == 0) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusFlyingXp.get());
         }

         return false;
      } else {
         return false;
      }
   }

   @SubscribeEvent
   static void onBlock(ShieldBlockEvent event) {
      if ((Boolean)Config.enableBlockingDamageXp.get()) {
         LivingEntity entity = event.getEntity();
         ItemStack activeStack = entity.getUseItem();
         if (ToolUtil.hasImprovable(activeStack) && entity instanceof ServerPlayer player) {
            ToolStack tool = ToolStack.from(activeStack);
            float blockAngle = ConditionalStatModifierHook.getModifiedStat(tool, player, ToolStats.BLOCK_ANGLE) / 2.0F;
            if (!tool.isBroken() && ToolUtil.canBlock(player, event.getDamageSource().getSourcePosition(), blockAngle)) {
               int xp = (int)(
                  (Config.damageBlocked.get() ? Math.min(event.getBlockedDamage(), (Float)tool.getStats().get(ToolStats.BLOCK_AMOUNT)) : 1.0F)
                     + ((Integer)Config.bonusBlockingDamageXp.get()).intValue()
               );
               ToolLevellingUtil.addExperience(tool, xp);
            }
         }
      }
   }

   public void onWalk(IToolStackView tool, ModifierEntry modifier, LivingEntity living, BlockPos prevPos, BlockPos newPos) {
      if (living instanceof ServerPlayer player && player.onGround() && !tool.isBroken()) {
         int xp = 0;

         for (ModifierEntry entry : tool.getModifierList()) {
            ArmorWalkModifierHook hook = (ArmorWalkModifierHook)entry.getHook(ModifierHooks.BOOT_WALK);
            if (hook instanceof ToolActionWalkerTransformModule toolAction) {
               if ((Boolean)Config.enablePlowingXp.get() && entry.getId().equals(ModifierIds.tilling)) {
                  xp = 1 + (Integer)Config.bonusPlowingXp.get();
               }

               if ((Boolean)Config.enablePathMakerXp.get() && entry.getId().equals(ModifierIds.pathing)) {
                  xp = 1 + (Integer)Config.bonusPathMakerXp.get();
               }

               if (xp > 0) {
                  this.handleWalkerTransform(tool, player, entry, toolAction, xp);
               }

               return;
            }

            if (hook instanceof CoverGroundWalkerModule coverGround && (Boolean)Config.enableSnowdriftXp.get() && entry.getId().equals(ModifierIds.snowdrift)) {
               xp = 1 + (Integer)Config.bonusSnowdriftXp.get();
               this.handleWalkerCoverGround(tool, player, entry, coverGround, xp);
               return;
            }

            if (hook instanceof ReplaceBlockWalkerModule replaceBlock
               && (Boolean)Config.enableFrostWalkerXp.get()
               && entry.getId().equals(ModifierIds.frostWalker)) {
               xp = 1 + (Integer)Config.bonusFrostWalkerXp.get();
               this.handleWalkerReplaceBlock(tool, player, entry, replaceBlock, xp);
               return;
            }

            if (hook instanceof FireWalkerModule fireWalker && (Boolean)Config.enableFlamewakeXp.get() && entry.getId().equals(ModifierIds.flamewake)) {
               xp = 1 + (Integer)Config.bonusFlamewakeXp.get();
               this.handleWalkerFire(tool, player, entry, fireWalker, xp);
               return;
            }

            if (hook instanceof GlowWalkerModule glowWalker && (Boolean)Config.enableGlowingXp.get() && entry.getId().equals(ModifierIds.glowing)) {
               xp = 1 + (Integer)Config.bonusGlowingXp.get();
               this.handleWalkerGlowing(tool, player, entry, glowWalker, xp);
               return;
            }
         }
      }
   }

   public void afterSlingLaunch(
      IToolStackView tool,
      ModifierEntry modifier,
      LivingEntity holder,
      LivingEntity target,
      ModifierEntry slingSource,
      float force,
      float multiplier,
      Vec3 angle
   ) {
      if (holder instanceof ServerPlayer) {
         if ((Boolean)Config.enableWarpingXp.get() && ModifierIds.warping.equals(slingSource.getId())) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusWarpingXp.get());
         } else if ((Boolean)Config.enableFlingingXp.get() && ModifierIds.flinging.equals(slingSource.getId())) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusFlingingXp.get());
         } else if ((Boolean)Config.enableSpringingXp.get() && ModifierIds.springing.equals(slingSource.getId())) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusSpringingXp.get());
         } else if ((Boolean)Config.enableBonkingXp.get() && ModifierIds.bonking.equals(slingSource.getId())) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusBonkingXp.get());
         }
      }
   }

   public InteractionResult beforeEntityUse(
      IToolStackView tool, ModifierEntry modifier, Player player, Entity target, InteractionHand hand, InteractionSource source
   ) {
      if (!tool.isBroken() && player instanceof ServerPlayer) {
         if ((Boolean)Config.enableFirestarterXp.get()
            && tool.getModifier(ModifierIds.firestarter) != ModifierEntry.EMPTY
            && ((InteractionToolModule)tool.getHook(ToolHooks.INTERACTION)).canInteract(tool, ModifierIds.firestarter, source)
            && target instanceof Creeper creeper
            && !creeper.isIgnited()) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusFirestarterXp.get());
         }

         ModifierEntry splashing = tool.getModifier(ModifierIds.splashing);
         if ((Boolean)Config.enableSplashingXp.get()
            && splashing != ModifierEntry.EMPTY
            && ((InteractionToolModule)tool.getHook(ToolHooks.INTERACTION)).canInteract(tool, ModifierIds.splashing, source)) {
            this.handleEntitySplashing(tool, player, target, splashing);
         }

         return InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
      if (!tool.isBroken() && !context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer) {
         return context.getLevel().getBlockState(context.getClickedPos()).is(BlockTags.CANDLE_CAKES)
            ? this.afterBlockUse(tool, modifier, context, source)
            : InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResult afterBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
      if (!tool.isBroken() && !context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer) {
         if ((Boolean)Config.enableGlowingXp.get()
            && tool.getModifier(ModifierIds.glowing) != ModifierEntry.EMPTY
            && ((InteractionToolModule)tool.getHook(ToolHooks.INTERACTION)).canInteract(tool, ModifierIds.glowing, source)) {
            this.handleGlowing(tool, context);
         }

         if ((Boolean)Config.enableFirestarterXp.get()
            && tool.getModifier(ModifierIds.firestarter) != ModifierEntry.EMPTY
            && ((InteractionToolModule)tool.getHook(ToolHooks.INTERACTION)).canInteract(tool, ModifierIds.firestarter, source)) {
            handleFirestarter(tool, context);
         }

         ModifierEntry splashing = tool.getModifier(ModifierIds.splashing);
         if ((Boolean)Config.enableSplashingXp.get()
            && splashing != ModifierEntry.EMPTY
            && ((InteractionToolModule)tool.getHook(ToolHooks.INTERACTION)).canInteract(tool, ModifierIds.splashing, source)) {
            this.handleBlockSplashing(tool, context, splashing);
         }

         ModifierEntry bucketing = tool.getModifier(ModifierIds.bucketing);
         if ((Boolean)Config.enableBucketingXp.get()
            && bucketing != ModifierEntry.EMPTY
            && ((InteractionToolModule)tool.getHook(ToolHooks.INTERACTION)).canInteract(tool, ModifierIds.bucketing, source)) {
            this.handleBucketing(tool, context, bucketing);
         }

         return InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void onUsingTick(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration, int timeLeft, ModifierEntry activeModifier) {
      if (!tool.isBroken() && timeLeft >= 0 && entity instanceof ServerPlayer player) {
         if ((Boolean)Config.enableBrushingXp.get() && ModifierIds.brushing.equals(activeModifier.getId())) {
            this.handleBrushing(tool, entity, timeLeft, player, activeModifier);
         }

         ModifierEntry slurping = tool.getModifier(ModifierIds.slurping);
         if ((Boolean)Config.enableSlurpingXp.get() && slurping != ModifierEntry.EMPTY && slurping.equals(activeModifier)) {
            this.handleToolSlurping(tool, useDuration - timeLeft, player, slurping);
         }
      }
   }

   public void beforeReleaseUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration, int timeLeft, ModifierEntry activeModifier) {
      if (!tool.isBroken() && timeLeft >= 0 && entity instanceof ServerPlayer player) {
         ModifierEntry slurping = tool.getModifier(ModifierIds.slurping);
         if ((Boolean)Config.enableSlurpingXp.get() && slurping != ModifierEntry.EMPTY && slurping.equals(activeModifier)) {
            this.handleToolSlurping(tool, useDuration - timeLeft, player, slurping);
         }
      }
   }

   public void onInventoryTick(
      IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack
   ) {
      if (!tool.isBroken() && holder instanceof ServerPlayer player) {
         ModifierEntry slurping = tool.getModifier(ModifierIds.slurping);
         if ((Boolean)Config.enableSlurpingXp.get() && slurping != ModifierEntry.EMPTY && isCorrectSlot) {
            this.handleArmorSlurping(tool, player, slurping);
         }
      }
   }

   public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> generatedLoot, LootContext context) {
      ModifierEntry melting = tool.getModifier(TinkerModifiers.melting.getId());
      if ((Boolean)Config.enableMeltingXp.get() && melting != ModifierEntry.EMPTY) {
         this.handleLootMelting(tool, generatedLoot, context, melting);
      }
   }

   private void handleSweepAttack(ToolAttackContext context, IToolStackView toolStack, ServerPlayer player, float damageDealt, SweepWeaponAttack module) {
      if (context.isFullyCharged()
         && !player.isSprinting()
         && !context.isCritical()
         && !context.isProjectile()
         && player.onGround()
         && !(player.walkDist - player.walkDistO >= player.getSpeed())) {
         double range = module.ranges().compute(toolStack.getVolatileData().getInt(IModifiable.EXPANDED));
         double rangeSq = 2.0 + range;
         rangeSq *= rangeSq;
         float sweepDamage = SweepWeaponAttack.getSweepingDamage(toolStack, damageDealt);
         this.handleAOETargets(toolStack, player, context.getTarget(), sweepDamage, range, rangeSq);
      }
   }

   private void handleCircleAttack(ToolAttackContext context, IToolStackView toolStack, ServerPlayer player, float damageDealt, CircleWeaponAttack module) {
      double range = module.diameters().compute(toolStack.getVolatileData().getInt(IModifiable.EXPANDED));
      if (!(range <= 0.0)) {
         double rangeSq = range * range;
         this.handleAOETargets(toolStack, player, context.getTarget(), damageDealt, range, rangeSq);
      }
   }

   private void handleAOETargets(IToolStackView toolStack, ServerPlayer player, Entity target, float damage, double range, double rangeSq) {
      for (LivingEntity aoeTarget : player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(range, 0.25, range))) {
         if (aoeTarget != player
            && aoeTarget != target
            && !player.isAlliedTo(aoeTarget)
            && !(aoeTarget instanceof ArmorStand armorStand && armorStand.isMarker())
            && player.distanceToSqr(aoeTarget) < rangeSq) {
            int xp = (Config.damageDealt.get() ? Math.round(damage) : 1) + (Integer)Config.bonusAttackingXp.get();
            ToolLevellingUtil.addExperience(toolStack, xp);
         }
      }
   }

   private void handleWalkerTransform(IToolStackView tool, ServerPlayer player, ModifierEntry entry, ToolActionWalkerTransformModule module, int xp) {
      if (module.condition().matches(tool, entry)) {
         MutableBlockPos mutable = new MutableBlockPos();
         this.handleWalker(tool, player, entry, module, pos -> {
            if (player.level().getBlockState(pos).canBeReplaced()) {
               mutable.set(pos.getX(), pos.getY() - 1, pos.getZ());
               MutableUseOnContext context = module.getContext(tool, entry, player, pos, mutable);
               context.setOffsetPos(mutable);
               BlockState original = player.level().getBlockState(mutable);
               BlockState transformed = original.getToolModifiedState(context, module.action(), true);
               if (transformed != null) {
                  ToolLevellingUtil.addExperience(tool, xp);
               }
            }

            return false;
         });
      }
   }

   private void handleWalkerCoverGround(IToolStackView tool, ServerPlayer player, ModifierEntry entry, CoverGroundWalkerModule module, int xp) {
      if (module.condition().matches(tool, entry)) {
         Level world = player.level();
         this.handleWalker(tool, player, entry, module, pos -> {
            if (world.isEmptyBlock(pos) && module.state().canSurvive(world, pos)) {
               ToolLevellingUtil.addExperience(tool, xp);
            }

            return false;
         });
      }
   }

   private void handleWalkerReplaceBlock(IToolStackView tool, ServerPlayer player, ModifierEntry entry, ReplaceBlockWalkerModule module, int xp) {
      if (module.tool().matches(tool)) {
         Level world = player.level();
         MutableBlockPos mutable = new MutableBlockPos();
         this.handleWalker(
            tool,
            player,
            entry,
            module,
            pos -> {
               mutable.set(pos.getX(), pos.getY() - 1, pos.getZ());
               BlockState state = Blocks.FROSTED_ICE.defaultBlockState();
               if (world.getBlockState(mutable).is(Blocks.WATER)
                  && state.canSurvive(world, mutable)
                  && world.isUnobstructed(state, mutable, CollisionContext.empty())
                  && !EventHooks.onBlockPlace(player, BlockSnapshot.create(world.dimension(), world, mutable), Direction.UP)) {
                  ToolLevellingUtil.addExperience(tool, xp);
               }

               return false;
            }
         );
      }
   }

   private void handleWalkerFire(IToolStackView tool, ServerPlayer player, ModifierEntry entry, FireWalkerModule module, int xp) {
      this.handleWalker(tool, player, entry, module, pos -> {
         if (BaseFireBlock.canBePlacedAt(player.level(), pos, player.getDirection())) {
            ToolLevellingUtil.addExperience(tool, xp);
         }

         return false;
      });
   }

   private void handleWalkerGlowing(IToolStackView tool, ServerPlayer player, ModifierEntry entry, GlowWalkerModule module, int xp) {
      Level world = player.level();
      this.handleWalker(tool, player, entry, module, pos -> {
         if (world.isEmptyBlock(pos) && world.getBrightness(LightLayer.BLOCK, pos) < module.minLight() && WorldUtil.canPlaceGlow(world, pos, Direction.DOWN)) {
            ToolLevellingUtil.addExperience(tool, xp);
            return true;
         } else {
            return false;
         }
      });
   }

   private void handleWalker(IToolStackView tool, ServerPlayer player, ModifierEntry entry, ArmorWalkRadiusModule<?> module, Function<BlockPos, Boolean> walkOn) {
      float trueRadius = ToolUtil.getWalkerTrueRadius(tool, entry, module);
      int radius = Mth.floor(trueRadius);
      Vec3 posVec = player.position();
      BlockPos center = BlockPos.containing(posVec.x, posVec.y + 0.5, posVec.z);

      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, 0, -radius), center.offset(radius, 0, radius))) {
         if (pos.closerToCenterThan(player.position(), trueRadius)) {
            boolean doBreak = walkOn.apply(pos);
            if (doBreak) {
               return;
            }
         }
      }
   }

   private void handleGlowing(IToolStackView tool, UseOnContext context) {
      Direction face = context.getClickedFace();
      BlockPos pos = context.getClickedPos().relative(face);
      if (WorldUtil.canPlaceGlow(context.getLevel(), pos, face.getOpposite())) {
         ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusGlowingXp.get());
      }
   }

   private static void handleFirestarter(IToolStackView tool, UseOnContext context) {
      Level world = context.getLevel();
      UseOnContext targetContext = context;
      BlockPos pos = context.getClickedPos();
      Direction sideHit = context.getClickedFace();
      BlockState state = world.getBlockState(pos);
      boolean targetingFire = false;
      if (state.is(BlockTags.FIRE)) {
         pos = pos.relative(sideHit.getOpposite());
         targetContext = Util.offset(context, pos);
         targetingFire = true;
      }

      int range = tool.getVolatileData().getInt(IModifiable.EXPANDED);
      Iterable<BlockPos> targets = Collections.emptyList();
      if (range > 0) {
         targets = CircleAOEIterator.calculate(tool, targetContext, 1 + range, true, AOEMatchType.TRANSFORM);
      }

      Direction horizontalFacing = context.getHorizontalDirection();
      if (!targetingFire && WorldUtil.canIgnite(world, pos, state, sideHit, horizontalFacing)) {
         ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusFirestarterXp.get());
      } else {
         for (BlockPos target : targets) {
            if (WorldUtil.canIgnite(world, target, world.getBlockState(target), sideHit, horizontalFacing)) {
               ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusFirestarterXp.get());
            }
         }
      }
   }

   private void handleBrushing(IToolStackView tool, LivingEntity entity, int timeLeft, ServerPlayer player, ModifierEntry activeModifier) {
      HitResult hit = ToolUtil.calculateHitResult(entity);
      if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
         && hit instanceof BlockHitResult blockHit
         && (((GeneralInteractionModifierHook)activeModifier.getHook(ModifierHooks.GENERAL_INTERACT)).getUseDuration(tool, activeModifier) - timeLeft + 1) % 10
            == 5) {
         Level level = entity.level();
         BlockPos pos = blockHit.getBlockPos();
         BlockState state = level.getBlockState(pos);
         InteractionHand hand = entity.getUsedItemHand();
         if (WorldUtil.isBrushable(blockHit, level)) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusBrushingXp.get());
         }

         UseOnContext context = new UseOnContext(level, player, hand, player.getItemInHand(hand), blockHit);

         for (BlockPos extraPos : ((AreaOfEffectIterator)tool.getDefinition().getHook(ToolHooks.AOE_ITERATOR))
            .getBlocks(tool, context, state, AOEMatchType.TRANSFORM)) {
            if (WorldUtil.isBrushable(Util.offset(blockHit, extraPos), level)) {
               ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusBrushingXp.get());
            }
         }
      }
   }

   private void handleSpilling(
      IToolStackView tool,
      ModifierEntry spilling,
      LivingEntity attacker,
      Player playerAttacker,
      Entity target,
      LivingEntity livingTarget,
      Projectile projectile
   ) {
      if (spilling != ModifierEntry.EMPTY) {
         MeleeHitModifierHook hook = (MeleeHitModifierHook)spilling.getModifier().getHook(ModifierHooks.MELEE_HIT);
         if (hook instanceof SpillingModule spillingModule && spillingModule.condition().matches(tool, spilling)) {
            FluidStack fluid = ToolTankHelper.TANK_HELPER.getFluid(tool);
            if (!fluid.isEmpty()) {
               FluidEffects recipe = FluidEffectManager.INSTANCE.find(fluid.getFluid());
               if (recipe.hasEntityEffects()) {
                  int consumed = recipe.applyToEntity(
                     fluid,
                     spillingModule.level().compute(spilling.getEffectiveLevel()),
                     FluidEffectContext.builder(attacker.level()).user(attacker, playerAttacker).projectile(projectile).target(target, livingTarget),
                     FluidAction.SIMULATE
                  );
                  if (consumed > 0) {
                     ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusSpillingXp.get());
                  }
               }
            }
         }
      }
   }

   private void handleEntitySplashing(IToolStackView tool, Player player, Entity target, ModifierEntry splashing) {
      EntityInteractionModifierHook hook = (EntityInteractionModifierHook)splashing.getModifier().getHook(ModifierHooks.ENTITY_INTERACT);
      if (hook instanceof SplashingModule splashingModule) {
         FluidStack fluid = ToolTankHelper.TANK_HELPER.getFluid(tool);
         if (!fluid.isEmpty()) {
            FluidEffects recipe = FluidEffectManager.INSTANCE.find(fluid.getFluid());
            if (recipe.hasEntityEffects()) {
               Level world = player.level();
               float level = splashingModule.strength().compute(splashing);
               int consumed = recipe.applyToEntity(fluid, level, FluidEffectContext.builder(world).user(player).target(target), FluidAction.SIMULATE);
               if (consumed > 0) {
                  ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusSplashingXp.get());
                  fluid.shrink(consumed);
               }

               if (!fluid.isEmpty()) {
                  float range = 1 + tool.getVolatileData().getInt(IModifiable.EXPANDED);
                  float rangeSq = range * range;

                  for (Entity aoeTarget : world.getEntitiesOfClass(Entity.class, target.getBoundingBox().inflate(range, 0.25, range))) {
                     if (aoeTarget != player
                        && aoeTarget != target
                        && !(aoeTarget instanceof ArmorStand stand && stand.isMarker())
                        && target.distanceToSqr(aoeTarget) < rangeSq
                        && ((Boolean)Config.enablePvp.get() || !(aoeTarget instanceof Player))) {
                        consumed = recipe.applyToEntity(fluid, level, FluidEffectContext.builder(world).user(player).target(aoeTarget), FluidAction.SIMULATE);
                        if (consumed > 0) {
                           ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusSplashingXp.get());
                           fluid.shrink(consumed);
                           if (fluid.isEmpty()) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void handleBlockSplashing(IToolStackView tool, UseOnContext context, ModifierEntry splashing) {
      EntityInteractionModifierHook hook = (EntityInteractionModifierHook)splashing.getModifier().getHook(ModifierHooks.ENTITY_INTERACT);
      if (hook instanceof SplashingModule splashingModule) {
         FluidStack fluid = ToolTankHelper.TANK_HELPER.getFluid(tool);
         if (!fluid.isEmpty()) {
            FluidEffects recipe = FluidEffectManager.INSTANCE.find(fluid.getFluid());
            if (recipe.hasBlockEffects()) {
               Level world = context.getLevel();
               Player player = context.getPlayer();
               float level = splashingModule.strength().compute(splashing);
               BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
               BlockState state = world.getBlockState(hit.getBlockPos());
               int consumed = recipe.applyToBlock(fluid, level, FluidEffectContext.builder(world).user(player).block(hit), FluidAction.EXECUTE);
               if (consumed > 0) {
                  ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusSplashingXp.get());
                  fluid.shrink(consumed);
               }

               if (!fluid.isEmpty()) {
                  for (BlockPos offset : ((AreaOfEffectIterator)tool.getHook(ToolHooks.AOE_ITERATOR)).getBlocks(tool, context, state, AOEMatchType.TRANSFORM)) {
                     BlockHitResult offsetHit = Util.offset(hit, offset);
                     consumed = recipe.applyToBlock(fluid, level, FluidEffectContext.builder(world).user(player).block(offsetHit), FluidAction.EXECUTE);
                     if (consumed > 0) {
                        ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusSplashingXp.get());
                        fluid.shrink(consumed);
                        if (fluid.isEmpty()) {
                           break;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void handleToolSlurping(IToolStackView tool, int useTime, ServerPlayer player, ModifierEntry slurping) {
      int duration = ((GeneralInteractionModifierHook)slurping.getHook(ModifierHooks.GENERAL_INTERACT)).getUseDuration(tool, slurping);
      if (useTime == duration && ToolUtil.canSlurp(tool, player, slurping)) {
         ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusSlurpingXp.get());
      }
   }

   private void handleArmorSlurping(IToolStackView tool, ServerPlayer player, ModifierEntry slurping) {
      if (tool.hasTag(Items.WORN_ARMOR)) {
         int finishTime = tool.getPersistentData().getInt(ModifierIds.slurping);
         if (finishTime > 0 && finishTime - player.tickCount < 0 && ToolUtil.canSlurp(tool, player, slurping)) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusSlurpingXp.get());
         }
      }
   }

   private static void handleTargetMelting(IToolStackView tool, LivingEntity target) {
      if (target != null) {
         EntityMeltingRecipe recipe = EntityMeltingRecipeCache.findRecipe(target.level().getRecipeManager(), target.getType());
         FluidStack output;
         if (recipe != null) {
            output = recipe.getOutput(target);
         } else {
            output = EntityMeltingModule.getDefaultFluid();
         }

         FluidStack fluid = ToolTankHelper.TANK_HELPER.getFluid(tool);
         if (fluid.isEmpty() || fluid.isFluidEqual(output)) {
            ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusMeltingXp.get());
         }
      }
   }

   private void handleLootMelting(IToolStackView tool, List<ItemStack> generatedLoot, LootContext context, ModifierEntry melting) {
      FluidStack current = ToolTankHelper.TANK_HELPER.getFluid(tool);
      int capacity = ToolTankHelper.TANK_HELPER.getCapacity(tool);
      if (current.getAmount() < capacity) {
         boolean forceMelt = tool.getVolatileData().getBoolean(MeltingModule.FORCE_MELTING);
         if (forceMelt && context.hasParam(LootContextParams.BLOCK_STATE)) {
            BlockState state = (BlockState)context.getParam(LootContextParams.BLOCK_STATE);
            forceMelt = ((IsEffectiveToolHook)tool.getHook(ToolHooks.IS_EFFECTIVE)).isToolEffective(tool, state);
         }

         Level world = context.getLevel();

         for (ItemStack stack : generatedLoot) {
            FluidStack output = ToolUtil.getMeltItemResult(melting, stack, world);
            if (!output.isEmpty() && (current.isEmpty() || current.isFluidEqual(output))) {
               if (forceMelt) {
                  ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusMeltingXp.get());
               } else {
                  int maxCopies = Math.min((capacity - current.getAmount()) / output.getAmount(), stack.getCount());
                  if (maxCopies > 0) {
                     ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusMeltingXp.get());
                  }
               }
            } else if (forceMelt) {
               ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusMeltingXp.get());
            }
         }
      }
   }

   private void handleBucketing(IToolStackView tool, UseOnContext context, ModifierEntry bucketing) {
      BlockInteractionModifierHook hook = (BlockInteractionModifierHook)bucketing.getModifier().getHook(ModifierHooks.BLOCK_INTERACT);
      BucketModule bucketModule;
      if (hook instanceof BucketModule bucketModule1) {
         bucketModule = bucketModule1;
      } else {
         if (!(hook instanceof FirstMerger merger)) {
            return;
         }

         bucketModule = merger.modules().stream().filter(BucketModule.class::isInstance).map(BucketModule.class::cast).findFirst().orElse(null);
         if (bucketModule == null) {
            return;
         }
      }

      Player player = context.getPlayer();
      if (player != null && player.isShiftKeyDown()) {
         FluidStack fluidStack = ToolTankHelper.TANK_HELPER.getFluid(tool);
         if (fluidStack.getAmount() >= 1000) {
            Fluid fluid = fluidStack.getFluid();
            if (bucketModule.fluids().matches(fluid) && fluid instanceof FlowingFluid) {
               if (WorldUtil.canPlaceBucketFluid(context, player, fluidStack)) {
                  ToolLevellingUtil.addExperience(tool, 1 + (Integer)Config.bonusBucketingXp.get());
               }
            }
         }
      }
   }
}
