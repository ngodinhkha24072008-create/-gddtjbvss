package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.data.ModModifierIds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolActionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.hook.interaction.UsingToolModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.tools.TinkerTools;
import modernmods.modernfoundry.tools.modules.armor.CounterModule;

public class CounterAttackModifier
   extends NoLevelsModifier
   implements GeneralInteractionModifierHook,
   OnAttackedModifierHook,
   MeleeDamageModifierHook,
   ToolActionModifierHook,
   UsingToolModifierHook,
   ModifierUtils {
   private static boolean isblocking = false;

   protected void registerHooks(Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(
         this,
         new ModuleHook[]{
            ModifierHooks.GENERAL_INTERACT, ModifierHooks.ON_ATTACKED, ModifierHooks.MELEE_DAMAGE, ModifierHooks.TOOL_ACTION, ModifierHooks.TOOL_USING
         }
      );
   }

   public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      if (isblocking) {
         damage *= 2.0F + 0.35F * tool.getModifierLevel(ModModifierIds.CounterAdvanced);
      }

      return damage;
   }

   public void onAttacked(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage
   ) {
      LivingEntity living = context.getEntity();
      Entity attacker = source.getEntity();
      if (isDirectDamage
         && !source.is(DamageTypeTags.BYPASSES_SHIELD)
         && living instanceof Player player
         && CounterModule.isBlocking(tool, slotType, player)
         && attacker != null
         && ToolAttackUtil.isAttackable(living, attacker)) {
         isblocking = true;
         InteractionHand hand = living.getUsedItemHand();
         modernmods.modernfoundry.library.tools.context.ToolAttackContext.Builder builder = ToolAttackContext.attacker(living)
            .target(attacker)
            .hand(hand)
            .cooldown(1.0F);
         if (hand == InteractionHand.MAIN_HAND) {
            builder.applyAttributes();
            builder.baseKnockback(0.8F);
         } else {
            builder.toolAttributes(tool);
         }

         ToolAttackUtil.performAttack(tool, builder.build());
         isblocking = false;
         this.addEffect(player, MobEffects.MOVEMENT_SPEED, 80, 3);
         ToolAttackUtil.spawnAttackParticle((ParticleOptions)TinkerTools.hammerAttackParticle.get(), living, 0.6);
         if (player instanceof ServerPlayer playerMP) {
            TinkerNetwork.getInstance().sendVanillaPacket(new ClientboundSetEntityMotionPacket(player), playerMP);
         }

         context.getLevel().playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, living.getSoundSource(), 1.0F, 0.5F);
         player.causeFoodExhaustion(0.1F);
         ToolDamageUtil.damageAnimated(tool, 1, player);
      }
   }

   public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
      if (source == InteractionSource.RIGHT_CLICK && !tool.isBroken()) {
         GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
         player.useItemRemaining -= 5;
         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void onFinishUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity) {
      if (entity instanceof Player player) {
         player.getCooldowns().addCooldown(tool.getItem(), (int)(20.0F / (Float)tool.getStats().get(ToolStats.ATTACK_SPEED)));
      }
   }

   public UseAnim getUseAction(IToolStackView tool, ModifierEntry modifier) {
      return UseAnim.BLOCK;
   }

   public int getUseDuration(IToolStackView tool, ModifierEntry modifier) {
      return 15;
   }

   public boolean canPerformAction(IToolStackView tool, ModifierEntry modifier, ItemAbility toolAction) {
      return toolAction == ItemAbilities.SHIELD_BLOCK;
   }
}
