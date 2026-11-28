package com.creeping_creeper.tinkers_thinking.common.library;

import com.creeping_creeper.tinkers_thinking.common.register.ModCommonItems;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import com.creeping_creeper.tinkers_thinking.data.ModDataKeys;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.Holder;

@EventBusSubscriber(
   modid = "tinkers_thinking",
   bus = Bus.FORGE
)
public class OnDeath implements ModifierUtils {
   @SubscribeEvent
   public void onLivingDying(LivingDeathEvent event) {
      LivingEntity living = event.getEntity();
      DamageSource source = event.getSource();
      if (living instanceof Player player && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
         if (player.hasEffect((MobEffect)ModEffects.last_effort.get())) {
            event.setCanceled(true);
            player.setHealth(1.0F);
            this.block(living);
            if (player.getEffect((MobEffect)ModEffects.last_effort.get()).getAmplifier() == 0) {
               int x = Objects.requireNonNull(player.getEffect((MobEffect)ModEffects.last_effort.get())).getDuration();
               player.addEffect(new MobEffectInstance((MobEffect)ModEffects.last_effort.get(), x, 1));
            }
         } else {
            Optional<Holder> dataCap = living.getCapability(TinkerDataCapability.CAPABILITY).resolve();
            dataCap.ifPresent(data -> {
               int level = (Integer)data.get(ModDataKeys.SculkStruggle, 0);
               if (level > 0 && player.hasEffect((MobEffect)ModEffects.sculk_power.get())) {
                  event.setCanceled(true);
                  player.setHealth(1.0F);
                  this.block(living);
                  player.addEffect(new MobEffectInstance((MobEffect)ModEffects.last_effort.get(), level * 60 + 180, 1));
                  if (player.level().isClientSide) {
                     Minecraft.getInstance().gameRenderer.displayItemActivation(ModCommonItems.warden_steel.getIngot().getDefaultInstance());
                  }
               }
            });
         }
      }
   }
}
