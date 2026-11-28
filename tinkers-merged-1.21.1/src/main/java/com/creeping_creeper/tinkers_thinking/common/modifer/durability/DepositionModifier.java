package com.creeping_creeper.tinkers_thinking.common.modifer.durability;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class DepositionModifier extends Modifier implements ToolDamageModifierHook, TooltipModifierHook {
   private static final Component prefix = TinkersThinking.makeTranslation("modifier", "deposition.chance");

   public int getPriority() {
      return 180;
   }

   protected void registerHooks(@NotNull Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE, ModifierHooks.TOOLTIP);
   }

   public int onDamageTool(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
      if (holder != null) {
         float y = (float)holder.getY();
         if (y < 72.0F) {
            float chance = (float)Math.min((72.0F - y) / 136.0F * modifier.getLevel() * 0.4, 0.8);
            int maxDamage = amount;

            for (int i = 0; i < maxDamage; i++) {
               if (RANDOM.nextFloat() < chance) {
                  amount--;
               }
            }
         }
      }

      return amount;
   }

   public void addTooltip(
      IToolStackView tool,
      @NotNull ModifierEntry modifier,
      @Nullable Player player,
      @NotNull List<Component> tooltip,
      TooltipKey tooltipKey,
      TooltipFlag tooltipFlag
   ) {
      if (player != null) {
         float chance = 0.0F;
         float y = (float)player.getY();
         if (y < 72.0F) {
            chance = (float)Math.min((72.0F - y) / 136.0F * modifier.getLevel() * 0.4, 0.8);
         }

         TooltipModifierHook.addPercentBoost(this, prefix, chance, tooltip);
      }
   }
}
