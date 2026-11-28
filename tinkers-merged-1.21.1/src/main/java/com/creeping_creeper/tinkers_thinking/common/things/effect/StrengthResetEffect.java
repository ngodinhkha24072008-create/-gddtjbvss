package com.creeping_creeper.tinkers_thinking.common.things.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import modernmods.modernfoundry.tools.modifiers.effect.NoMilkEffect;

public class StrengthResetEffect extends NoMilkEffect {
   public StrengthResetEffect(MobEffectCategory typeIn, int color, boolean show) {
      super(typeIn, color, show);
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return duration == 1;
   }

   public boolean applyEffectTick(LivingEntity living, int amplifier) {
      living.attackStrengthTicker += 2000;
      return true;
   }
}
