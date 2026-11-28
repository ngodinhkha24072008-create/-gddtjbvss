package com.creeping_creeper.tinkers_thinking.common.modifer.harvest;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import com.creeping_creeper.tinkers_thinking.data.ModDataKeys;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.common.TinkerTags.Items;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.Holder;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

public enum SculkBoostModule implements ModifierModule, TooltipModifierHook, ConditionalStatModifierHook, BreakSpeedModifierHook, EquipmentChangeModifierHook {
   INSTANCE;

   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(
      new ModuleHook[]{ModifierHooks.TOOLTIP, ModifierHooks.BREAK_SPEED, ModifierHooks.CONDITIONAL_STAT, ModifierHooks.EQUIPMENT_CHANGE}
   );
   public static final RecordLoadable<SculkBoostModule> LOADER = new SingletonLoader(INSTANCE);
   private static final Component Boost = TinkersThinking.makeTranslation("modifier", "sculk_boost");
   public static final UUID ATTRIBUTE_BONUS = UUID.fromString("2307DE5E-7CE8-4030-940E-514C1F170002");

   @NotNull
   public RecordLoadable<SculkBoostModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public void onBreakSpeed(
      @NotNull IToolStackView tool,
      @NotNull ModifierEntry modifier,
      @NotNull BreakSpeed event,
      @NotNull Direction sideHit,
      boolean isEffective,
      float miningSpeedModifier
   ) {
      if (isEffective && event.getEntity().hasEffect((MobEffect)ModEffects.sculk_power.get())) {
         event.setNewSpeed(event.getNewSpeed() * (1.0F + modifier.getLevel() * 0.2F));
      }
   }

   public float modifyStat(
      @NotNull IToolStackView tool, @NotNull ModifierEntry modifier, LivingEntity living, @NotNull FloatToolStat stat, float baseValue, float multiplier
   ) {
      if (living.hasEffect((MobEffect)ModEffects.sculk_power.get())) {
         if (tool.hasTag(Items.ARMOR) && stat == ToolStats.ARMOR_TOUGHNESS) {
            AtomicInteger x = new AtomicInteger();
            Optional<Holder> dataCap = living.getCapability(TinkerDataCapability.CAPABILITY).resolve();
            dataCap.ifPresent(data -> x.set((Integer)data.get(ModDataKeys.SculkBoost, 1)));
            return baseValue * (1.0F + x.get() * 0.2F);
         }

         if (stat == ToolStats.DRAW_SPEED) {
            return baseValue * (1.0F + 0.2F * modifier.getLevel());
         }
      }

      return baseValue;
   }

   public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
      IToolStackView oldTool = context.getOriginalTool();
      if (!tool.isBroken() && (oldTool == null || oldTool.getModifier(modifier.getModifier()).getLevel() != modifier.getLevel())) {
         this.reset(context.getEntity());
      }
   }

   public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
      IToolStackView newTool = context.getReplacementTool();
      if (newTool == null || newTool.isBroken() || newTool.getModifier(modifier.getModifier()).getLevel() == 0) {
         this.reset(context.getEntity());
      }
   }

   private void reset(LivingEntity living) {
      AttributeInstance attribute = living.getAttribute(Attributes.ARMOR_TOUGHNESS);
      if (attribute.getModifier(ATTRIBUTE_BONUS) != null) {
         attribute.removeModifier(ATTRIBUTE_BONUS);
      }

      Optional<Holder> dataCap = living.getCapability(TinkerDataCapability.CAPABILITY).resolve();
      dataCap.ifPresent(
         data -> {
            int x = (Integer)data.get(ModDataKeys.SculkBoost, 0);
            if (x > 0 && living.hasEffect((MobEffect)ModEffects.sculk_power.get())) {
               living.getAttribute(Attributes.ARMOR_TOUGHNESS)
                  .addPermanentModifier(new AttributeModifier(ATTRIBUTE_BONUS, "tinkers_thinking.modifier.sculk_boost", x * 0.2F, Operation.MULTIPLY_BASE));
            }
         }
      );
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag
   ) {
      if (player != null) {
         float bonus = 0.0F;
         if (player.hasEffect((MobEffect)ModEffects.sculk_power.get())) {
            bonus = 0.2F * modifier.getLevel();
         }

         TooltipModifierHook.addPercentBoost(modifier.getModifier(), Boost, bonus, tooltip);
      }
   }
}
