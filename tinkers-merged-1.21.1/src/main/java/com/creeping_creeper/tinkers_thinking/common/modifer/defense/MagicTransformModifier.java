package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class MagicTransformModifier extends Modifier implements MeleeDamageModifierHook, ModifyDamageModifierHook {
   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.MODIFY_HURT);
   }

   public float getMeleeDamage(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
      LivingEntity target = context.getLivingTarget();
      if (target != null && target.isAlive()) {
         damage = this.transform(context.getAttacker(), target, damage, modifier.getLevel());
      }

      return damage;
   }

   public float modifyDamageTaken(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage
   ) {
      Entity attacker = source.getEntity();
      LivingEntity living = context.getEntity();
      if (living.isAlive() && attacker != null && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
         amount = this.transform(source.getEntity(), living, amount, modifier.getLevel());
      }

      return amount;
   }

   private float transform(Entity attacker, LivingEntity target, float value, int level) {
      float var9 = value * (float)Math.max(1.0 - level * 0.2, 0.0);
      float y = (float)((value - var9) * 0.75);
      int z = target.invulnerableTime;
      target.invulnerableTime = 0;
      DamageSource damageSource = TinkerDamageTypes.source(target.level().registryAccess(), DamageTypes.INDIRECT_MAGIC, attacker);
      target.hurt(damageSource, y);
      target.invulnerableTime = z;
      return var9;
   }
}
