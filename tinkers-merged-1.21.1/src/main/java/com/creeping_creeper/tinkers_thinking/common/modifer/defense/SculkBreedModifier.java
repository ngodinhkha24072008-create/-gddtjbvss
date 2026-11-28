package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class SculkBreedModifier extends Modifier implements ModifyDamageModifierHook, EquipmentChangeModifierHook, TooltipModifierHook {
   public static final UUID ATTRIBUTE_BONUS = UUID.fromString("2307DE5E-7CE8-4030-940E-514C1F170001");
   private static final Component Boost = TinkersThinking.makeTranslation("modifier", "sculk_breed.boost");

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE, ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.TOOLTIP);
   }

   public float modifyDamageTaken(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage
   ) {
      LivingEntity living = context.getEntity();
      AttributeInstance attribute = living.getAttribute(Attributes.MAX_HEALTH);
      float x = (float)(amount * modifier.getLevel() * 0.5);
      if (living.hasEffect((MobEffect)ModEffects.sculk_power.get()) && !tool.isBroken()) {
         if (attribute.getModifier(ATTRIBUTE_BONUS) == null) {
            attribute.addTransientModifier(new AttributeModifier(ATTRIBUTE_BONUS, "tinkers_thinking.modifier.sculk_breed", x, Operation.ADDITION));
            attribute.getModifier(ATTRIBUTE_BONUS).getAmount();
         }

         if (attribute.getModifier(ATTRIBUTE_BONUS) != null && x > Objects.requireNonNull(attribute.getModifier(ATTRIBUTE_BONUS)).getAmount()) {
            attribute.removeModifier(ATTRIBUTE_BONUS);
            attribute.addTransientModifier(new AttributeModifier(ATTRIBUTE_BONUS, "tinkers_thinking.modifier.sculk_breed", x, Operation.ADDITION));
         }
      }

      return amount;
   }

   public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
      LivingEntity living = context.getEntity();
      IToolStackView newTool = context.getReplacementTool();
      if (newTool == null || newTool.isBroken() || newTool.getModifier(this).getLevel() < modifier.getLevel()) {
         AttributeInstance attribute = living.getAttribute(Attributes.MAX_HEALTH);
         if (attribute.getModifier(ATTRIBUTE_BONUS) != null) {
            attribute.removeModifier(ATTRIBUTE_BONUS);
            if (living.getHealth() > living.getMaxHealth()) {
               living.setHealth(living.getMaxHealth());
            }
         }
      }
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag tooltipFlag
   ) {
      float x = 0.0F;
      if (player != null && key == TooltipKey.SHIFT) {
         AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
         if (attribute != null && attribute.getModifier(ATTRIBUTE_BONUS) != null) {
            x = (float)Objects.requireNonNull(attribute.getModifier(ATTRIBUTE_BONUS)).getAmount();
         }

         TooltipModifierHook.addFlatBoost(this, Boost, x, tooltip);
      }
   }
}
