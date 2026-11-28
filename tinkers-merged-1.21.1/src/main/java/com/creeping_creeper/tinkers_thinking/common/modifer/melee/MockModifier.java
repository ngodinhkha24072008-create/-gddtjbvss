package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.utils.Util;

public class MockModifier
   extends Modifier
   implements TooltipModifierHook,
   MeleeDamageModifierHook,
   MeleeHitModifierHook,
   RedirectAfter,
   ProjectileHitModifierHook,
   ModifierUtils {
   private static final Component ATTACK_DAMAGE = TinkersThinking.makeTranslation("modifier", "mock.attack_damage");

   public int getPriority() {
      return 90;
   }

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(
         this,
         new ModuleHook[]{
            ModifierHooks.PROJECTILE_HIT,
            ModifierHooks.MELEE_DAMAGE,
            ModifierHooks.MONSTER_MELEE_DAMAGE,
            ModifierHooks.MELEE_HIT,
            ModifierHooks.MONSTER_MELEE_HIT,
            ModifierHooks.TOOLTIP
         }
      );
   }

   public float getMeleeDamage(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      LivingEntity target = context.getLivingTarget();
      if (target != null && target.isAlive() && !context.isExtraAttack() && context.isFullyCharged() && target.hasEffect(MobEffects.INVISIBILITY)) {
         float bonus = 0.2F * modifier.getLevel();
         damage *= 1.0F + bonus;
      }

      return damage;
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      if (!context.isExtraAttack() && context.isFullyCharged()) {
         LivingEntity target = context.getLivingTarget();
         if (target != null) {
            this.addEffect(target, MobEffects.INVISIBILITY, 200);
         }
      }
   }

   public boolean onProjectileHitEntity(
      @NotNull ModifierNBT modifiers,
      ModDataNBT persistentData,
      @NotNull ModifierEntry modifier,
      @NotNull Projectile projectile,
      EntityHitResult hit,
      @Nullable LivingEntity attacker,
      @Nullable LivingEntity target,
      boolean notBlocked
   ) {
      if (target != null) {
         this.addEffect(target, MobEffects.INVISIBILITY, 200);
      }

      if (target != null && target.isAlive() && target.hasEffect(MobEffects.INVISIBILITY)) {
         this.setPower(projectile, modifier.getLevel() * 0.2F);
      }

      return false;
   }

   public void addTooltip(
      IToolStackView tool,
      ModifierEntry modifier,
      @org.jetbrains.annotations.Nullable Player player,
      List<Component> tooltip,
      TooltipKey tooltipKey,
      TooltipFlag tooltipFlag
   ) {
      tooltip.add(this.applyStyle(Component.literal(Util.PERCENT_BOOST_FORMAT.format((float)(0.2 * modifier.getLevel())) + " ").append(ATTACK_DAMAGE)));
   }
}
