package com.creeping_creeper.tinkers_thinking.common.modifer.misc;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public record HurriedModule(LevelingValue amount) implements ModifierModule, EquipmentChangeModifierHook, TooltipModifierHook, InventoryTickModifierHook {
   private static final UUID ATTRIBUTE_BONUS = UUID.fromString("2307DE5E-7CE8-4030-940E-514C1F170002");
   private static final Component Boost = TinkersThinking.makeTranslation("modifier", "hurried.boost");
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(
      new ModuleHook[]{ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.TOOLTIP, ModifierHooks.INVENTORY_TICK}
   );
   public static final RecordLoadable<HurriedModule> LOADER = RecordLoadable.create(
      LevelingValue.LOADABLE.directField(HurriedModule::amount), HurriedModule::new
   );

   @NotNull
   public RecordLoadable<HurriedModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
      LivingEntity living = context.getEntity();
      IToolStackView newTool = context.getReplacementTool();
      AttributeInstance attribute = living.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attribute != null && (newTool == null || newTool.isBroken() || newTool.getModifier(modifier.getModifier()).getLevel() != modifier.getLevel())) {
         attribute.removeModifier(ATTRIBUTE_BONUS);
      }
   }

   public void onInventoryTick(
      IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack
   ) {
      AttributeInstance attribute = holder.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attribute != null && attribute.getModifier(ATTRIBUTE_BONUS) == null) {
         attribute.addTransientModifier(new AttributeModifier(ATTRIBUTE_BONUS, "tinkers_thinking.modifier.hurried", this.getBonus(holder, modifier), Operation.ADDITION));
      }
   }

   private boolean isEmpty(LivingEntity living, int stack) {
      return living.getSlot(stack).get().isEmpty();
   }

   private float getBonus(LivingEntity living, ModifierEntry modifier) {
      int x = 0;

      for (int i = 0; i < 9; i++) {
         if (this.isEmpty(living, i)) {
            x++;
         }
      }

      return this.amount.eachLevel() * x * modifier.getLevel();
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag tooltipFlag
   ) {
      float x = 0.0F;
      if (player != null && key == TooltipKey.SHIFT) {
         x = 10.0F * this.getBonus(player, modifier);
      }

      if (x > 0.0F) {
         TooltipModifierHook.addPercentBoost(modifier.getModifier(), Boost, x, tooltip);
      }
   }
}
