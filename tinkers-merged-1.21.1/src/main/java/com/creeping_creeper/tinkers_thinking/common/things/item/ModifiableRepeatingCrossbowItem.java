package com.creeping_creeper.tinkers_thinking.common.things.item;

import com.creeping_creeper.tinkers_thinking.common.register.ModModifiers;
import com.creeping_creeper.tinkers_thinking.data.ModModifierIds;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags.Modifiers;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.UsingToolModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.BowAmmoModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.tools.capability.EntityModifierCapability;
import modernmods.modernfoundry.library.tools.capability.PersistentDataCapability;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.item.ranged.ModifiableLauncherItem;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.modifiers.ability.interaction.BlockingModifier;

public class ModifiableRepeatingCrossbowItem extends ModifiableLauncherItem {
   public static final ResourceLocation KEY_CROSSBOW_AMMO = TConstruct.getResource("crossbow_ammo");
   private static final String PROJECTILE_KEY = "item.minecraft.crossbow.projectile";
   private final Predicate<ItemStack> supportedHeldProjectiles;
   private final boolean storeDrawingItem;

   public ModifiableRepeatingCrossbowItem(
      Properties properties, ToolDefinition toolDefinition, Predicate<ItemStack> supportedHeldProjectiles, boolean storeDrawingItem
   ) {
      super(properties, toolDefinition);
      this.supportedHeldProjectiles = supportedHeldProjectiles;
      this.storeDrawingItem = storeDrawingItem;
   }

   public ModifiableRepeatingCrossbowItem(Properties properties, ToolDefinition toolDefinition) {
      this(properties, toolDefinition, ARROW_OR_FIREWORK, false);
   }

   public Predicate<ItemStack> getAllSupportedProjectiles() {
      return ARROW_ONLY;
   }

   public int getDefaultProjectileRange() {
      return 8;
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return BlockingModifier.blockWhileCharging(ToolStack.from(stack), UseAnim.NONE);
   }

   public boolean useOnRelease(ItemStack stack) {
      return true;
   }

   private static float getRandomShotPitch(float angle, RandomSource pRandom) {
      return angle == 0.0F ? 1.0F : 1.0F / (pRandom.nextFloat() * 0.5F + 1.8F) + 0.53F + angle / 10.0F;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack bow = player.getItemInHand(hand);
      ToolStack tool = ToolStack.from(bow);
      if (tool.isBroken()) {
         return InteractionResultHolder.fail(bow);
      } else {
         boolean sinistral = hand == InteractionHand.MAIN_HAND && tool.getModifierLevel(ModModifiers.Sinistral.get()) > 0;
         ModDataNBT persistentData = tool.getPersistentData();
         CompoundTag heldAmmo = persistentData.getCompound(KEY_CROSSBOW_AMMO);
         if (heldAmmo.isEmpty()) {
            if (sinistral && !player.getOffhandItem().isEmpty() && player.isCrouching()) {
               return InteractionResultHolder.pass(bow);
            } else {
               ItemStack ammo = BowAmmoModifierHook.getAmmo(tool, bow, player, this.getSupportedHeldProjectiles());
               if (!ammo.isEmpty() || tool.getModifiers().has(Modifiers.CHARGE_EMPTY_BOW_WITH_DRAWTIME)) {
                  GeneralInteractionModifierHook.startDrawtime(tool, player, 1.0F);
                  if (!ammo.isEmpty()) {
                     if (this.storeDrawingItem) {
                        persistentData.put(KEY_DRAWBACK_AMMO, modernmods.modernfoundry.library.utils.TagUtil.saveItem(ammo, new CompoundTag()));
                     } else {
                        persistentData.putBoolean(KEY_DRAWBACK_AMMO, true);
                     }
                  }

                  player.startUsingItem(hand);
                  if (!level.isClientSide) {
                     level.playSound(
                        (Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_QUICK_CHARGE_1, SoundSource.PLAYERS, 0.75F, 1.0F
                     );
                  }

                  return InteractionResultHolder.consume(bow);
               } else if (tool.getModifiers().has(Modifiers.CHARGE_EMPTY_BOW_WITHOUT_DRAWTIME)) {
                  player.startUsingItem(hand);
                  return InteractionResultHolder.consume(bow);
               } else {
                  return InteractionResultHolder.fail(bow);
               }
            }
         } else {
            if (sinistral) {
               ItemStack ammo = player.getOffhandItem();
               if (!ammo.isEmpty() && !ammo.is(Items.FIREWORK_ROCKET)) {
                  return InteractionResultHolder.pass(bow);
               }

               if (ModifierUtil.canPerformAction(tool, ItemAbilities.SHIELD_BLOCK)) {
                  player.startUsingItem(hand);
                  return InteractionResultHolder.consume(bow);
               }
            }

            fireCrossbow(tool, player, hand, heldAmmo);
            return InteractionResultHolder.consume(bow);
         }
      }
   }

   public static void fireCrossbow(IToolStackView tool, Player player, InteractionHand hand, CompoundTag heldAmmo) {
      fireCrossbow(tool, player, player.getAbilities().instabuild, hand, heldAmmo);
   }

   public static void fireCrossbow(IToolStackView tool, LivingEntity living, boolean creative, InteractionHand hand, CompoundTag heldAmmo) {
      Level level = living.level();
      if (!level.isClientSide) {
         int damage = 0;
         float velocity = ConditionalStatModifierHook.getModifiedStat(tool, living, ToolStats.VELOCITY);
         float inaccuracy = ModifierUtil.getInaccuracy(tool, living);
         ItemStack ammo = modernmods.modernfoundry.library.utils.TagUtil.readItem(heldAmmo);
         int ammoCount = Math.min(1 + 2 * tool.getModifierLevel(TinkerModifiers.multishot.getId()), ammo.getCount());
         float startAngle = getAngleStart(ammoCount);
         int primaryIndex = ammoCount / 2;

         for (int arrowIndex = 0; arrowIndex < ammoCount; arrowIndex++) {
            AbstractArrow arrow = null;
            Projectile projectile;
            float speed;
            if (ammo.is(Items.FIREWORK_ROCKET)) {
               projectile = new FireworkRocketEntity(level, ammo, living, living.getX(), living.getEyeY() - 0.15F, living.getZ(), true);
               speed = 1.5F;
               damage += 3;
            } else {
               ArrowItem arrowItem = ammo.getItem() instanceof ArrowItem a ? a : (ArrowItem)Items.ARROW;
               arrow = arrowItem.createArrow(level, ammo, living, null);
               projectile = arrow;
               arrow.setCritArrow(true);
               arrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
               speed = 3.0F;
               damage++;
               float angle = (float)(arrow.getBaseDamage() - 2.0 + ((Float)tool.getStats().get(ToolStats.PROJECTILE_DAMAGE)).floatValue());
               arrow.setBaseDamage(ConditionalStatModifierHook.getModifiedStat(tool, living, ToolStats.PROJECTILE_DAMAGE, angle));
               if (creative) {
                  arrow.pickup = Pickup.CREATIVE_ONLY;
               }
            }

            Vec3 upVector = living.getUpVector(1.0F);
            float angle = startAngle + 10 * arrowIndex;
            Vector3f targetVector = living.getViewVector(1.0F)
               .toVector3f()
               .rotate(new Quaternionf().setAngleAxis(angle * Math.PI / 180.0, upVector.x, upVector.y, upVector.z));
            projectile.shoot(targetVector.x(), targetVector.y(), targetVector.z(), velocity * speed, inaccuracy);
            ModifierNBT modifiers = tool.getModifiers();
            EntityModifierCapability.getCapability(projectile).addModifiers(modifiers);
            ModDataNBT projectileData = PersistentDataCapability.getOrWarn(projectile);

            for (ModifierEntry entry : modifiers.getModifiers()) {
               ((ProjectileLaunchModifierHook)entry.getHook(ModifierHooks.PROJECTILE_LAUNCH))
                  .onProjectileLaunch(tool, entry, living, ammo, projectile, arrow, projectileData, arrowIndex == primaryIndex);
            }

            level.addFreshEntity(projectile);
            level.playSound(
               null,
               living.getX(),
               living.getY(),
               living.getZ(),
               SoundEvents.CROSSBOW_SHOOT,
               SoundSource.PLAYERS,
               1.0F,
               getRandomShotPitch(angle, living.getRandom())
            );
         }

         tool.getPersistentData().remove(KEY_CROSSBOW_AMMO);
         if (ammo.getCount() > ammoCount) {
            ammo.shrink(ammoCount);
            CompoundTag ammoNBT = modernmods.modernfoundry.library.utils.TagUtil.saveItem(ammo, new CompoundTag());
            tool.getPersistentData().put(KEY_CROSSBOW_AMMO, ammoNBT);
         }

         ToolDamageUtil.damageAnimated(tool, damage, living, hand);
         if (living instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, living.getItemInHand(hand));
            serverPlayer.awardStat(Stats.ITEM_USED.get(tool.getItem()));
         }
      }
   }

   public void releaseUsing(ItemStack bow, Level level, LivingEntity living, int chargeRemaining) {
      ToolStack tool = ToolStack.from(bow);
      int duration = this.getUseDuration(bow, living);

      for (ModifierEntry entry : tool.getModifiers()) {
         ((UsingToolModifierHook)entry.getHook(ModifierHooks.TOOL_USING))
            .beforeReleaseUsing(tool, entry, living, duration, chargeRemaining, ModifierEntry.EMPTY);
      }

      ModDataNBT persistentData = tool.getPersistentData();
      if (!tool.isBroken()
         && this.getUseDuration(bow, living) - chargeRemaining >= persistentData.getInt(GeneralInteractionModifierHook.KEY_DRAWTIME)
         && !persistentData.contains(KEY_CROSSBOW_AMMO, 10)) {
         Player player = living instanceof Player p ? p : null;
         int projectilesDesired = (3 + tool.getModifierLevel(ModModifierIds.RepeatingAdvanced))
            * (1 + 2 * tool.getModifierLevel(TinkerModifiers.multishot.getId()));
         ItemStack ammo = BowAmmoModifierHook.consumeAmmo(tool, bow, living, player, this.getSupportedHeldProjectiles(), projectilesDesired);
         if (!ammo.isEmpty()) {
            level.playSound(
               null,
               living.getX(),
               living.getY(),
               living.getZ(),
               SoundEvents.CROSSBOW_LOADING_END,
               SoundSource.PLAYERS,
               1.0F,
               1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F
            );
            if (!level.isClientSide) {
               CompoundTag ammoNBT = modernmods.modernfoundry.library.utils.TagUtil.saveItem(ammo, new CompoundTag());
               persistentData.put(KEY_CROSSBOW_AMMO, ammoNBT);
               if (tool.isBroken()) {
                  fireCrossbow(tool, living, player != null && player.getAbilities().instabuild, living.getUsedItemHand(), ammoNBT);
               }
            }
         }
      }
   }

   public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
      tooltips = super.getStatInformation(tool, player, tooltips, key, tooltipFlag);
      CompoundTag heldAmmo = tool.getPersistentData().getCompound(KEY_CROSSBOW_AMMO);
      if (!heldAmmo.isEmpty()) {
         ItemStack heldStack = modernmods.modernfoundry.library.utils.TagUtil.readItem(heldAmmo);
         if (!heldStack.isEmpty()) {
            MutableComponent component = Component.translatable("item.minecraft.crossbow.projectile");
            int count = heldStack.getCount();
            if (count > 1) {
               component.append(" " + count + " ");
            } else {
               component.append(" ");
            }

            tooltips.add(component.append(heldStack.getDisplayName()));
            if (tooltipFlag.isAdvanced() && player != null) {
               List<Component> nestedTooltip = new ArrayList<>();
               heldStack.getItem().appendHoverText(heldStack, net.minecraft.world.item.Item.TooltipContext.of(player.level()), nestedTooltip, tooltipFlag);

               for (Component nested : nestedTooltip) {
                  tooltips.add(Component.literal("  ").append(nested).withStyle(ChatFormatting.GRAY));
               }
            }
         }
      }

      return tooltips;
   }

   public Predicate<ItemStack> getSupportedHeldProjectiles() {
      return this.supportedHeldProjectiles;
   }
}
