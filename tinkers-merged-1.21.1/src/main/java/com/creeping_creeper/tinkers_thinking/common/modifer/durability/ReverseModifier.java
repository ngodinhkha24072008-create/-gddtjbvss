package com.creeping_creeper.tinkers_thinking.common.modifer.durability;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.common.TinkerTags.Items;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.modifiers.hook.display.DurabilityDisplayModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BlockBreakModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.LauncherHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.capability.PersistentDataCapability;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.context.ToolHarvestContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

public class ReverseModifier
   extends NoLevelsModifier
   implements ToolDamageModifierHook,
   DurabilityDisplayModifierHook,
   ModifierRemovalHook,
   TooltipModifierHook,
   MeleeHitModifierHook,
   RedirectAfter,
   BlockBreakModifierHook,
   LauncherHitModifierHook,
   ProjectileLaunchModifierHook,
   ModifyDamageModifierHook,
   ModifierUtils {
   private static final Component n1 = TinkersThinking.makeTranslation("modifier", "reverse.1");
   private static final Component n2 = TinkersThinking.makeTranslation("modifier", "reverse.2");

   public int getPriority() {
      return 225;
   }

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(
         this,
         new ModuleHook[]{
            ModifierHooks.TOOL_DAMAGE,
            ModifierHooks.DURABILITY_DISPLAY,
            ModifierHooks.TOOLTIP,
            ModifierHooks.MELEE_HIT,
            ModifierHooks.MONSTER_MELEE_HIT,
            ModifierHooks.BLOCK_BREAK,
            ModifierHooks.LAUNCHER_HIT,
            ModifierHooks.PROJECTILE_LAUNCH,
            ModifierHooks.MODIFY_DAMAGE
         }
      );
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      if (!context.isExtraAttack()) {
         this.change(tool);
      }
   }

   public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
      if (context.isEffective()) {
         this.change(tool);
      }
   }

   public void onLauncherHitEntity(
      IToolStackView tool,
      ModifierEntry modifier,
      Projectile projectile,
      LivingEntity attacker,
      Entity target,
      @Nullable LivingEntity livingTarget,
      float damageDealt
   ) {
      this.change(tool);
   }

   public void onProjectileLaunch(
      IToolStackView tool,
      ModifierEntry modifier,
      LivingEntity shooter,
      Projectile projectile,
      @Nullable AbstractArrow arrow,
      ModDataNBT persistentData,
      boolean primary
   ) {
      if (primary) {
         this.change(tool);
         ModDataNBT data = PersistentDataCapability.getOrWarn(projectile);
         data.putBoolean(reverse_key, this.reverse(tool));
      }
   }

   public float modifyDamageTaken(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage
   ) {
      if (tool.hasTag(Items.ARMOR)) {
         this.change(tool);
      }

      return amount;
   }

   public Component onRemoved(IToolStackView tool, Modifier modifier) {
      if (tool.getModifierLevel(this.getId()) == 0) {
         tool.getPersistentData().remove(reverse_key);
      }

      return n1;
   }

   public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
      if (this.reverse(tool)) {
         amount *= 2;
      } else {
         amount = 0;
      }

      return amount;
   }

   @Nullable
   public Boolean showDurabilityBar(IToolStackView tool, ModifierEntry modifier) {
      return true;
   }

   public int getDurabilityWidth(IToolStackView tool, ModifierEntry modifier) {
      return 0;
   }

   public int getDurabilityRGB(IToolStackView tool, ModifierEntry modifier) {
      return tool.getPersistentData().getBoolean(reverse_key) ? 5592575 : 16733525;
   }

   public void addTooltip(
      IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag tooltipFlag
   ) {
      ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
      tooltip.add(this.applyStyle((MutableComponent)(persistentData.getBoolean(reverse_key) ? n2 : n1)));
   }
}
