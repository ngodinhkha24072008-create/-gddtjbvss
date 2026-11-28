package com.creeping_creeper.tinkers_thinking.common.things.effect;

import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.tools.modifiers.effect.NoMilkEffect;

public class FreezingColdEffect extends NoMilkEffect {
   public static final UUID FREEZINGCOLDBONUS = UUID.fromString("2307DE5E-7CE8-4030-940E-514C1F170002");

   public FreezingColdEffect(MobEffectCategory typeIn, int color, boolean show) {
      super(typeIn, color, show);
   }

   public boolean isDurationEffectTick(int tick, int amplifier) {
      return tick % 10 == 0 || tick == 1;
   }

   public void applyEffectTick(LivingEntity living, int amplifier) {
      AttributeInstance attribute = living.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attribute != null && attribute.getValue() > 0.0) {
         float x = (float)(-0.002 * (amplifier + 1));
         if (attribute.getModifier(FREEZINGCOLDBONUS) != null) {
            x += (float)Objects.requireNonNull(attribute.getModifier(FREEZINGCOLDBONUS)).getAmount();
            attribute.removeModifier(FREEZINGCOLDBONUS);
         }

         attribute.addTransientModifier(new AttributeModifier(FREEZINGCOLDBONUS, "tinkers_thinking.effect.freezing_cold", x, Operation.ADDITION));
      }

      if (Objects.requireNonNull(living.getEffect((MobEffect)ModEffects.freezing_cold.get())).getDuration() == 1) {
         int y = 3 * amplifier + 3;
         LivingEntity lastAttacker = living.getLastHurtMob();
         DamageSource source = TinkerDamageTypes.source(living.level().registryAccess(), DamageTypes.FREEZE, lastAttacker);
         living.level().playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.GLASS_BREAK, SoundSource.MASTER, 1.0F, 1.0F);
         ToolAttackUtil.attackEntitySecondary(source, living.isInWaterOrRain() ? 2 * y : y, living, living, true);
         if (attribute != null) {
            attribute.removeModifier(FREEZINGCOLDBONUS);
         }
      }
   }
}
