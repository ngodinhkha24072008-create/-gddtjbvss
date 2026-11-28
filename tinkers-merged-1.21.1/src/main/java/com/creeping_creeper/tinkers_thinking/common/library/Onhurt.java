package com.creeping_creeper.tinkers_thinking.common.library;

import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import java.util.Objects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "tinkers_thinking",
   bus = Bus.GAME
)
public class Onhurt {
   @SubscribeEvent
   public void onLivingHurt(LivingIncomingDamageEvent event) {
      LivingEntity living = event.getEntity();
      Level level = living.level();
      DamageSource source = event.getSource();
      if (living.hasEffect(ModEffects.cataclysm)) {
         if (source.is(DamageTypeTags.IS_FIRE) && !level.isClientSide) {
            level.explode(
               living,
               living.getX(),
               living.getY(),
               living.getZ(),
               Objects.requireNonNull(living.getEffect(ModEffects.cataclysm)).getAmplifier() + 1,
               ExplosionInteraction.MOB
            );
            living.removeEffect(ModEffects.cataclysm);
         }

         if (source.is(DamageTypes.FREEZE)) {
            living.removeEffect(ModEffects.cataclysm);
         }
      }

      if (living.hasEffect(ModEffects.disarm)) {
         living.removeEffect(ModEffects.disarm);
      }
   }
}
