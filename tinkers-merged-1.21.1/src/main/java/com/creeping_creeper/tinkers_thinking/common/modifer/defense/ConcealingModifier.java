package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.hilt.data.predicate.damage.DamageSourcePredicate;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.armor.ProtectionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.armor.ProtectionModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class ConcealingModifier extends Modifier implements TooltipModifierHook, ModifyDamageModifierHook, ProtectionModifierHook, ModifierUtils {
   public int getPriority() {
      return 90;
   }

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.TOOLTIP, ModifierHooks.MODIFY_DAMAGE, ModifierHooks.PROTECTION);
   }

   public float modifyDamageTaken(
      @NotNull IToolStackView tool,
      @NotNull ModifierEntry modifier,
      @NotNull EquipmentContext context,
      @NotNull EquipmentSlot slotType,
      DamageSource source,
      float amount,
      boolean isDirectDamage
   ) {
      if (source.getEntity() != null) {
         this.addEffect(context.getEntity(), MobEffects.INVISIBILITY, 200, 0);
      }

      return amount;
   }

   public float getProtectionModifier(
      @NotNull IToolStackView tool,
      @NotNull ModifierEntry modifier,
      @NotNull EquipmentContext context,
      @NotNull EquipmentSlot slotType,
      @NotNull DamageSource source,
      float modifierValue
   ) {
      if (context.getEntity().hasEffect(MobEffects.INVISIBILITY) && DamageSourcePredicate.CAN_PROTECT.matches(source)) {
         modifierValue += (float)(modifier.getLevel() * 1.5);
      }

      return modifierValue;
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, @NotNull List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag
   ) {
      ProtectionModule.addResistanceTooltip(tool, this, modifier.getLevel(), player, tooltip);
   }
}
