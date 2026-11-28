package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ProtectionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

public class SculkProtectionModifier extends Modifier implements ProtectionModifierHook, TooltipModifierHook {
   private static final Component Boost = TinkersThinking.makeTranslation("modifier", "sculk_protection.boost");

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.PROTECTION, ModifierHooks.TOOLTIP);
   }

   private float getBono(LivingEntity living, ModifierEntry modifier) {
      return living.hasEffect(ModEffects.sculk_power)
         ? Math.min(1.5F, living.getEffect(ModEffects.sculk_power).duration / 1600.0F) * modifier.getLevel()
         : 0.0F;
   }

   public float getProtectionModifier(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float modifierValue
   ) {
      return modifierValue + this.getBono(context.getEntity(), modifier);
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag
   ) {
      if (player != null) {
         tooltip.add(this.applyStyle(Component.literal(Util.PERCENT_BOOST_FORMAT.format(this.getBono(player, modifier) / 25.0F) + " ").append(Boost)));
      }
   }
}
