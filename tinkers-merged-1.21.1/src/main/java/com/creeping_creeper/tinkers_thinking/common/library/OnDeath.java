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
   bus = Bus.GAME
)
public class OnDeath implements ModifierUtils {
   @SubscribeEvent
   public void onLivingDying(LivingDeathEvent event) {
      LivingEntity living = event.getEntity();
      DamageSource source = event.getSource();
      if (living instanceof Player player && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
         if (player.hasEffect(ModEffects.last_effort)) {
            event.setCanceled(true);
            player.setHealth(1.0F);
            this.block(living);
            if (player.getEffect(ModEffects.last_effort).getAmplifier() == 0) {
               int x = Objects.requireNonNull(player.getEffect(ModEffects.last_effort)).getDuration();
               player.addEffect(new MobEffectInstance(ModEffects.last_effort, x, 1));
            }
         } else {
            Optional<Holder> dataCap = TinkerDataCapability.getCapability(living).resolve();
            dataCap.ifPresent(data -> {
               int level = (Integer)data.get(ModDataKeys.SculkStruggle, 0);
               if (level > 0 && player.hasEffect(ModEffects.sculk_power)) {
                  event.setCanceled(true);
                  player.setHealth(1.0F);
                  this.block(living);
                  player.addEffect(new MobEffectInstance(ModEffects.last_effort, level * 60 + 180, 1));
                  if (player.level().isClientSide) {
                     Minecraft.getInstance().gameRenderer.displayItemActivation(ModCommonItems.warden_steel.getIngot().getDefaultInstance());
                  }
               }
            });
         }
      }
   }
}
