package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.utils.Util;

public class SculkDashModifier
   extends Modifier
   implements MeleeHitModifierHook,
   MeleeDamageModifierHook,
   RedirectAfter,
   ModifierRemovalHook,
   TooltipModifierHook {
   private static final Component Times = TinkersThinking.makeTranslation("modifier", "sculk_dash.times");
   private final ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath("tinkers_thinking", "sculk_dash");

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(
         this,
         new ModuleHook[]{
            ModifierHooks.MELEE_HIT,
            ModifierHooks.MELEE_DAMAGE,
            ModifierHooks.MONSTER_MELEE_HIT,
            ModifierHooks.MONSTER_MELEE_DAMAGE,
            ModifierHooks.REMOVE,
            ModifierHooks.TOOLTIP
         }
      );
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      Player attacker = context.getPlayerAttacker();
      ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
      if (attacker != null) {
         if (attacker.hasEffect((MobEffect)ModEffects.sculk_power.get()) && (context.getLivingTarget() == null || context.getLivingTarget().isDeadOrDying())) {
            persistentData.putInt(this.KEY, persistentData.getInt(this.KEY) + modifier.getLevel());
            attacker.addEffect(new MobEffectInstance((MobEffect)ModEffects.strength_reset.get(), 5, 0, true, false));
         }
      }
   }

   @Nullable
   public Component onRemoved(IToolStackView tool, Modifier modifier) {
      if (tool.getModifierLevel(this.getId()) == 0) {
         tool.getPersistentData().remove(this.KEY);
      }

      return null;
   }

   public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
      if (context.isFullyCharged() && !context.isExtraAttack() && persistentData.contains(this.KEY, 3) && persistentData.getInt(this.KEY) > 0) {
         persistentData.putInt(this.KEY, persistentData.getInt(this.KEY) - 1);
         damage *= 1.25F;
      }

      return damage;
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag tooltipFlag
   ) {
      ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
      if (player != null) {
         int x = persistentData.getInt(this.KEY);
         tooltip.add(this.applyStyle(Component.literal(Util.COMMA_FORMAT.format(x) + " ").append(Times)));
      }
   }
}
