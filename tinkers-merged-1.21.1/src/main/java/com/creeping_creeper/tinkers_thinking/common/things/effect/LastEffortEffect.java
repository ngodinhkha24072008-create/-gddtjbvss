package com.creeping_creeper.tinkers_thinking.common.things.effect;

import com.creeping_creeper.tinkers_thinking.data.ModDamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.tools.modifiers.effect.NoMilkEffect;

public class LastEffortEffect extends NoMilkEffect {
   public LastEffortEffect(MobEffectCategory typeIn, int color, boolean show) {
      super(typeIn, color, show);
   }

   public boolean isDurationEffectTick(int duration, int amplifier) {
      return duration == 1 && amplifier == 1;
   }

   public void applyEffectTick(LivingEntity living, int amplifier) {
      Level level = living.level();
      if (!level.isClientSide) {
         living.hurt(TinkerDamageTypes.source(level.registryAccess(), ModDamageTypes.last_effort), 99999.0F);
      }
   }
}
