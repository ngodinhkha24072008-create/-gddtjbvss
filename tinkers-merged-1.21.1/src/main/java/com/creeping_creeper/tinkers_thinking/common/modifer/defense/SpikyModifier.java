package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.OnAttackedModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public class SpikyModifier extends Modifier implements OnAttackedModifierHook, ModifierUtils {
   protected void registerHooks(@NotNull Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
   }

   public void onAttacked(
      @NotNull IToolStackView tool,
      @NotNull ModifierEntry modifier,
      @NotNull EquipmentContext context,
      @NotNull EquipmentSlot slotType,
      DamageSource source,
      float amount,
      boolean isDirectDamage
   ) {
      if (source.getEntity() instanceof LivingEntity attacker
         && !attacker.hasEffect((MobEffect)ModEffects.modifier_immune.get())
         && attacker != context.getEntity()
         && !(source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() != 0)) {
         attacker.hurt(context.getEntity().damageSources().thorns(context.getEntity()), amount);
         context.getEntity()
            .level()
            .playSound(
               null,
               context.getEntity().getX(),
               context.getEntity().getY(),
               context.getEntity().getZ(),
               SoundEvents.THORNS_HIT,
               SoundSource.PLAYERS,
               1.0F,
               1.0F
            );
         this.addEffect(attacker, (MobEffect)ModEffects.modifier_immune.get(), 60 / modifier.getLevel());
      }
   }
}
