package com.creeping_creeper.tinkers_thinking.common.things.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added;
import modernmods.modernfoundry.common.TinkerEffect;
import modernmods.modernfoundry.library.tools.capability.PersistentDataCapability;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

public class ReminiscenceEffect extends TinkerEffect {
   private static final ResourceLocation KEY = ResourceLocation.parse("tinkers_thinking:n");

   public ReminiscenceEffect(MobEffectCategory typeIn, boolean show) {
      super(typeIn, 12074426, show);
      NeoForge.EVENT_BUS.<Added>addListener(this::onEffectAdded);
   }

   private void onEffectAdded(Added event) {
      LivingEntity entity = event.getEntity();
      if (!entity.level().isClientSide() && event.getOldEffectInstance() == null && event.getEffectInstance().getEffect() == this) {
         ModDataNBT data = PersistentDataCapability.getOrWarn(entity);
         CompoundTag health = new CompoundTag();
         health.putFloat("health", entity.getHealth());
         data.put(KEY, health);
      }
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return duration == 1;
   }

   public boolean applyEffectTick(LivingEntity living, int amplifier) {
      ModDataNBT data = PersistentDataCapability.getOrWarn(living);
      if (data.contains(KEY, 10)) {
         CompoundTag health = data.getCompound(KEY);
         if (living.isAlive()) {
            living.setHealth(health.getFloat("health"));
         }
      }
      return true;
   }
}
