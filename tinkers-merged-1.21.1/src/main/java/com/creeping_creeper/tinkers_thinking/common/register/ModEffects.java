package com.creeping_creeper.tinkers_thinking.common.register;

import com.creeping_creeper.tinkers_thinking.common.things.effect.DisintegrationEffect;
import com.creeping_creeper.tinkers_thinking.common.things.effect.FreezingColdEffect;
import com.creeping_creeper.tinkers_thinking.common.things.effect.LastEffortEffect;
import com.creeping_creeper.tinkers_thinking.common.things.effect.ReminiscenceEffect;
import com.creeping_creeper.tinkers_thinking.common.things.effect.SculkPowerEffect;
import com.creeping_creeper.tinkers_thinking.common.things.effect.StrengthResetEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredHolder;
import modernmods.modernfoundry.tools.modifiers.effect.NoMilkEffect;

public class ModEffects extends ModModule {
   public static final DeferredHolder<MobEffect, MobEffect> overweight = MOB_EFFECTS.register(
      "overweight",
      () -> new NoMilkEffect(MobEffectCategory.HARMFUL, 9383569, true)
         .addAttributeModifier((Attribute)NeoForgeMod.ENTITY_GRAVITY.get(), "2307DE5E-7CE8-4030-940E-514C1F160001", 2.0, Operation.MULTIPLY_BASE)
   );
   public static final DeferredHolder<MobEffect, MobEffect> weightless = MOB_EFFECTS.register(
      "weightless",
      () -> new NoMilkEffect(MobEffectCategory.BENEFICIAL, 1489220, true)
         .addAttributeModifier((Attribute)NeoForgeMod.ENTITY_GRAVITY.get(), "2307DE5E-7CE8-4030-940E-514C1F160002", -0.5, Operation.MULTIPLY_BASE)
   );
   public static final DeferredHolder<MobEffect, MobEffect> sculk_power = MOB_EFFECTS.register(
      "sculk_power", () -> new SculkPowerEffect(MobEffectCategory.BENEFICIAL, 37525, true)
   );
   public static final DeferredHolder<MobEffect, MobEffect> quick_attack = MOB_EFFECTS.register(
      "quick_attack",
      () -> new NoMilkEffect(MobEffectCategory.BENEFICIAL, 37525, true)
         .addAttributeModifier(Attributes.ATTACK_SPEED, "2307DE5E-7CE8-4030-940E-514C1F160003", 0.1, Operation.MULTIPLY_BASE)
   );
   public static final DeferredHolder<MobEffect, MobEffect> modifier_immune = MOB_EFFECTS.register(
      "modifier_immune", () -> new NoMilkEffect(MobEffectCategory.BENEFICIAL, 16744231, true)
   );
   public static final DeferredHolder<MobEffect, MobEffect> antibrute_cooldown = MOB_EFFECTS.register(
      "antibrute_cooldown", () -> new NoMilkEffect(MobEffectCategory.HARMFUL, 16743814, true)
   );
   public static final DeferredHolder<MobEffect, MobEffect> armor = MOB_EFFECTS.register(
      "armor",
      () -> new NoMilkEffect(MobEffectCategory.BENEFICIAL, 16743814, true)
         .addAttributeModifier(Attributes.ARMOR, "2307DE5E-7CE8-4030-940E-514C1F160004", 1.0, Operation.ADDITION)
   );
   public static final DeferredHolder<MobEffect, MobEffect> cataclysm = MOB_EFFECTS.register(
      "cataclysm", () -> new NoMilkEffect(MobEffectCategory.BENEFICIAL, 7500402, true)
   );
   public static final DeferredHolder<MobEffect, MobEffect> freezing_cold = MOB_EFFECTS.register(
      "freezing_cold", () -> new FreezingColdEffect(MobEffectCategory.HARMFUL, 8189692, true)
   );
   public static final DeferredHolder<MobEffect, MobEffect> disintegration = MOB_EFFECTS.register(
      "disintegration", () -> new DisintegrationEffect(MobEffectCategory.HARMFUL, 10822984, true)
   );
   public static final DeferredHolder<MobEffect, MobEffect> disarm = MOB_EFFECTS.register(
      "disarm",
      () -> new NoMilkEffect(MobEffectCategory.HARMFUL, 12696654, true)
         .addAttributeModifier(Attributes.ATTACK_DAMAGE, "2307DE5E-7CE8-4030-940E-514C1F160005", -1.0, Operation.MULTIPLY_BASE)
   );
   public static final DeferredHolder<MobEffect, MobEffect> reminiscence = MOB_EFFECTS.register(
      "reminiscence", () -> new ReminiscenceEffect(MobEffectCategory.BENEFICIAL, true)
   );
   public static final DeferredHolder<MobEffect, MobEffect> jumpless = MOB_EFFECTS.register(
      "jumpless",
      () -> new NoMilkEffect(MobEffectCategory.HARMFUL, 9383569, true)
         .addAttributeModifier(Attributes.JUMP_STRENGTH, "2307DE5E-7CE8-4030-940E-514C1F160006", -1.0, Operation.MULTIPLY_BASE)
   );
   public static final DeferredHolder<MobEffect, MobEffect> last_effort = MOB_EFFECTS.register(
      "last_effort", () -> new LastEffortEffect(MobEffectCategory.BENEFICIAL, 10661766, true)
   );
   public static final DeferredHolder<MobEffect, MobEffect> strength_reset = MOB_EFFECTS.register(
      "strength_reset", () -> new StrengthResetEffect(MobEffectCategory.BENEFICIAL, 12557441, true)
   );
}
