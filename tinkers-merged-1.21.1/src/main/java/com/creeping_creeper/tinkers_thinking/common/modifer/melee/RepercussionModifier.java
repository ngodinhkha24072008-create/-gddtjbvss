package com.creeping_creeper.tinkers_thinking.common.modifer.melee;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.Objects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

public class RepercussionModifier
   extends Modifier
   implements MeleeDamageModifierHook,
   MeleeHitModifierHook,
   RedirectAfter,
   ProjectileHitModifierHook,
   ModifierUtils {
   private static boolean repercussion = true;

   public int getPriority() {
      return 230;
   }

   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(
         this,
         new ModuleHook[]{
            ModifierHooks.MELEE_DAMAGE,
            ModifierHooks.MELEE_HIT,
            ModifierHooks.MONSTER_MELEE_DAMAGE,
            ModifierHooks.MONSTER_MELEE_HIT,
            ModifierHooks.PROJECTILE_HIT
         }
      );
   }

   public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      LivingEntity target = context.getLivingTarget();
      LivingEntity attacker = context.getAttacker();
      if (!context.isExtraAttack() && context.isFullyCharged() && target != null && this.reverse(tool) && repercussion) {
         modernmods.modernfoundry.library.tools.context.ToolAttackContext.Builder builder = ToolAttackContext.attacker(attacker)
            .target(target)
            .hand(context.getHand())
            .cooldown(1.0F);
         if (context.getHand() == InteractionHand.MAIN_HAND) {
            builder.applyAttributes();
            builder.baseKnockback(0.4F);
         } else {
            builder.toolAttributes(tool);
         }

         ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
         int x = target.invulnerableTime;
         target.invulnerableTime = 0;
         repercussion = false;
         ToolAttackUtil.performAttack(tool, builder.build());
         repercussion = true;
         target.invulnerableTime = x;
         persistentData.remove(reverse_key);
         if (attacker instanceof Player player) {
            player.causeFoodExhaustion(0.1F);
         }

         ToolDamageUtil.damageAnimated(tool, 1, attacker);
      }

      return damage;
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      if (!context.isExtraAttack() && !this.reverse(tool) && context.getTarget().isAlive()) {
         this.addEffect(context.getAttacker(), ModEffects.disintegration, 200 / modifier.getLevel(), 2);
      }
   }
}
