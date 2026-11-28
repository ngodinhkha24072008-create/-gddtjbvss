package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.shared.TinkerEffects;
import modernmods.modernfoundry.tools.TinkerModifiers;

public class TeleportAdvancedModifier extends NoLevelsModifier implements GeneralInteractionModifierHook, MeleeHitModifierHook, ModifierUtils {
   private final ResourceLocation X = ResourceLocation.fromNamespaceAndPath("tinkersinnovation", "teleport_x");
   private final ResourceLocation Y = ResourceLocation.fromNamespaceAndPath("tinkersinnovation", "teleport_y");
   private final ResourceLocation Z = ResourceLocation.fromNamespaceAndPath("tinkersinnovation", "teleport_z");
   private final ResourceLocation WORLD = ResourceLocation.fromNamespaceAndPath("tinkersinnovation", "teleport_dimension");

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT, ModifierHooks.MELEE_HIT);
   }

   public int getPriority() {
      return 5;
   }

   private void applyEffect(LivingEntity living, int level) {
      this.addEffect(living, MobEffects.MOVEMENT_SPEED, level * 300, 1);
      this.addEffect(living, (MobEffect)ModEffects.sculk_power.get(), level * 300);
   }

   public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
      if (source == InteractionSource.RIGHT_CLICK
         && !tool.isBroken()
         && player.isCrouching()
         && !player.hasEffect((MobEffect)TinkerModifiers.teleportCooldownEffect.get())
         && !player.hasEffect((MobEffect)TinkerEffects.enderference.get())) {
         Level world = player.level();
         ModDataNBT data = tool.getPersistentData();
         if (data.contains(this.X, 5)
            && data.contains(this.Y, 5)
            && data.contains(this.Z, 5)
            && data.contains(this.WORLD, 8)
            && data.getString(this.WORLD).equals(world.dimension().location().getPath())) {
            this.applyEffect(player, modifier.getLevel());
         }
      }

      return InteractionResult.CONSUME;
   }

   public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      LivingEntity target = context.getLivingTarget();
      if (!tool.isBroken() && target != null && !target.hasEffect((MobEffect)TinkerEffects.enderference.get())) {
         Level world = target.level();
         ModDataNBT data = tool.getPersistentData();
         if (data.contains(this.X, 5)
            && data.contains(this.Y, 5)
            && data.contains(this.Z, 5)
            && data.contains(this.WORLD, 8)
            && data.getString(this.WORLD).equals(world.dimension().location().getPath())) {
            this.applyEffect(context.getAttacker(), modifier.getLevel());
         }
      }
   }
}
