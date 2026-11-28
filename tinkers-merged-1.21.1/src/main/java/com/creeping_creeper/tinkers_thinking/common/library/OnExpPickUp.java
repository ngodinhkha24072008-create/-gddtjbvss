package com.creeping_creeper.tinkers_thinking.common.library;

import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import com.creeping_creeper.tinkers_thinking.data.ModDataKeys;
import java.util.Optional;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent.PickupXp;
import net.neoforged.bus.api.SubscribeEvent;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.Holder;

public class OnExpPickUp implements ModifierUtils {
   @SubscribeEvent
   public void onPlayerPickupXp(PickupXp event) {
      ExperienceOrb exp = event.getOrb();
      Player player = event.getEntity();
      Optional<Holder> dataCap = player.getCapability(TinkerDataCapability.CAPABILITY).resolve();
      dataCap.ifPresent(data -> {
         if ((Integer)data.get(ModDataKeys.SculkCatalyse, 0) > 0) {
            int time = exp.getValue() * 60;
            if (player.hasEffect((MobEffect)ModEffects.sculk_power.get())) {
               time += player.getEffect((MobEffect)ModEffects.sculk_power.get()).getDuration();
            }

            this.addEffect(player, (MobEffect)ModEffects.sculk_power.get(), time);
            event.setCanceled(true);
            exp.discard();
         }
      });
   }
}
