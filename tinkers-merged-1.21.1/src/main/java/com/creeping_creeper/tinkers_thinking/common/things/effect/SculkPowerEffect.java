package com.creeping_creeper.tinkers_thinking.common.things.effect;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.modifer.defense.SculkBreedModifier;
import com.creeping_creeper.tinkers_thinking.common.modifer.harvest.SculkBoostModule;
import com.creeping_creeper.tinkers_thinking.data.ModDataKeys;
import java.util.Optional;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.Holder;
import modernmods.modernfoundry.tools.modifiers.effect.NoMilkEffect;

public class SculkPowerEffect extends NoMilkEffect implements ModifierUtils {
   public SculkPowerEffect(MobEffectCategory typeIn, int color, boolean show) {
      super(typeIn, color, show);
      NeoForge.EVENT_BUS.addListener(this::onEffectAdded);
      NeoForge.EVENT_BUS.addListener(this::onEffectRemove);
   }

   private void onEffectAdded(Added event) {
      LivingEntity entity = event.getEntity();
      Level level = entity.level();
      MobEffectInstance effect = event.getEffectInstance();
      if (effect.getEffect() == this) {
         if (event.getOldEffectInstance() == null) {
            Optional<Holder> dataCap = entity.getCapability(TinkerDataCapability.CAPABILITY).resolve();
            dataCap.ifPresent(
               data -> {
                  int x = (Integer)data.get(ModDataKeys.SculkBoost, 0);
                  if (x > 0) {
                     entity.getAttribute(Attributes.ARMOR_TOUGHNESS)
                        .addPermanentModifier(
                           new AttributeModifier(SculkBoostModule.ATTRIBUTE_BONUS, "tinkers_thinking.modifier.sculk_boost", x * 0.2F, Operation.MULTIPLY_BASE)
                        );
                  }
               }
            );
         }

         if (!level.isClientSide() && (event.getOldEffectInstance() == null || effect.getDuration() > event.getOldEffectInstance().getDuration() + 2)) {
            level.playSound(
               null, event.getEntity().getOnPos().above(), SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.PLAYERS, 1.0F, 1.6F + TConstruct.RANDOM.nextFloat() * 0.4F
            );
            this.particles(level, entity, new SculkChargeParticleOptions(0.0F), 4);
         }
      }
   }

   private void onEffectRemove(Remove event) {
      if (event.getEffect() == this) {
         this.removeAttribute(event.getEntity());
      }
   }

   public boolean isDurationEffectTick(int duration, int amplifier) {
      return duration == 1;
   }

   public void applyEffectTick(LivingEntity living, int amplifier) {
      this.removeAttribute(living);
   }

   private void removeAttribute(LivingEntity living) {
      AttributeInstance attribute1 = living.getAttribute(Attributes.MAX_HEALTH);
      AttributeInstance attribute2 = living.getAttribute(Attributes.ARMOR_TOUGHNESS);
      if (attribute1 != null && attribute1.getModifier(SculkBreedModifier.ATTRIBUTE_BONUS) != null) {
         attribute1.removeModifier(SculkBreedModifier.ATTRIBUTE_BONUS);
         if (living.getHealth() > living.getMaxHealth()) {
            living.setHealth(living.getMaxHealth());
         }
      }

      if (attribute2 != null && attribute2.getModifier(SculkBoostModule.ATTRIBUTE_BONUS) != null) {
         attribute2.removeModifier(SculkBoostModule.ATTRIBUTE_BONUS);
      }
   }
}
