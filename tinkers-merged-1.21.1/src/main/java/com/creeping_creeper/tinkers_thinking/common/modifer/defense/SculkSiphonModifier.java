package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModCommonItems;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class SculkSiphonModifier extends Modifier implements MeleeHitModifierHook, RedirectAfter, ModifyDamageModifierHook, ModifierUtils {
   protected void registerHooks(Builder hookBuilder) {
      hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.MODIFY_DAMAGE);
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      LivingEntity living = context.getLivingTarget();
      LivingEntity attacker = context.getAttacker();
      if (context.isFullyCharged()
         && !context.isExtraAttack()
         && attacker.hasEffect((MobEffect)ModEffects.sculk_power.get())
         && living != null
         && RANDOM.nextFloat() < 0.3) {
         this.dropItem(living, (Item)ModCommonItems.soul_shard_a.get(), modifier.getLevel());
      }
   }

   public float modifyDamageTaken(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage
   ) {
      Entity entity = source.getEntity();
      if (context.getEntity().hasEffect((MobEffect)ModEffects.sculk_power.get()) && entity != null && entity.isAlive() && RANDOM.nextFloat() < 0.3) {
         this.dropItem(entity, (Item)ModCommonItems.soul_shard_b.get(), modifier.getLevel());
      }

      return amount;
   }

   private void dropItem(Entity living, Item item, int x) {
      ItemEntity itementity = new ItemEntity(living.level(), living.getX(), living.getY(), living.getZ(), new ItemStack(item, x));
      itementity.setPickUpDelay(10);
      itementity.lifespan = 600;
      living.level().addFreshEntity(itementity);
   }
}
