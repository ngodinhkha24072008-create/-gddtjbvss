package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import com.creeping_creeper.tinkers_thinking.data.ModTags;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;

public class ResistingModifier extends Modifier implements ProjectileHitModifierHook {
   protected void registerHooks(Builder hookBuilder) {
      super.registerHooks(hookBuilder);
      hookBuilder.addHook(this, ModifierHooks.PROJECTILE_HIT);
   }

   public boolean onProjectileHitEntity(
      ModifierNBT modifiers,
      ModDataNBT persistentData,
      ModifierEntry modifier,
      Projectile projectile,
      EntityHitResult hit,
      @Nullable LivingEntity attacker,
      @Nullable LivingEntity target,
      boolean notBlocked
   ) {
      if (target != null) {
         target.invulnerableTime = 0;
         if (target.getType().is(ModTags.EntityTypes.resisting) && projectile instanceof AbstractArrow arrow && attacker != null) {
            if (arrow.getPierceLevel() > 0) {
               if (arrow.piercingIgnoreEntityIds == null) {
                  arrow.piercingIgnoreEntityIds = new IntOpenHashSet(5);
               }

               if (arrow.piercedAndKilledEntities == null) {
                  arrow.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
               }

               if (arrow.piercingIgnoreEntityIds.size() >= arrow.getPierceLevel() + 1) {
                  arrow.discard();
                  return true;
               }

               arrow.piercingIgnoreEntityIds.add(target.getId());
            }

            int damage = Mth.ceil(Mth.clamp(arrow.getDeltaMovement().length() * arrow.getBaseDamage(), 0.0, 2.147483647E9));
            if (arrow.isCritArrow()) {
               damage = (int)Math.min((long)RANDOM.nextInt(damage / 2 + 2) + damage, 2147483647L);
            }

            Entity owner = arrow.getOwner();
            DamageSource damageSource = TinkerDamageTypes.source(attacker.level().registryAccess(), DamageTypes.ARROW, attacker, attacker);
            attacker.setLastHurtMob(target);
            int remainingFire = target.getRemainingFireTicks();
            if (arrow.isOnFire()) {
               target.igniteForSeconds(5);
            }

            Level level = arrow.level();
            if (target.hurt(damageSource, damage)) {
               if (!level.isClientSide && arrow.getPierceLevel() <= 0) {
                  target.setArrowCount(target.getArrowCount() + 1);
               }

               // 1.21: arrow knockback and post-hit effects come from enchantment effect components
               if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                  EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
               }

               arrow.doPostHurtEffects(target);
               if (!target.isAlive() && arrow.piercedAndKilledEntities != null) {
                  arrow.piercedAndKilledEntities.add(target);
               }
            }

            if (!level.isClientSide && arrow.shotFromCrossbow() && owner instanceof ServerPlayer player) {
               if (arrow.piercedAndKilledEntities != null) {
                  CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(player, arrow.piercedAndKilledEntities);
               } else if (!target.isAlive()) {
                  CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(player, List.of(target));
               }
            }

            arrow.playSound(arrow.soundEvent, 1.0F, 1.2F / (RANDOM.nextFloat() * 0.2F + 0.9F));
            if (arrow.getPierceLevel() <= 0) {
               arrow.discard();
            } else {
               target.setRemainingFireTicks(remainingFire);
               arrow.setDeltaMovement(arrow.getDeltaMovement().scale(-0.1));
               arrow.setYRot(arrow.getYRot() + 180.0F);
               arrow.yRotO += 180.0F;
               if (!level.isClientSide && arrow.getDeltaMovement().lengthSqr() < 1.0E-7) {
                  if (arrow.pickup == Pickup.ALLOWED) {
                     arrow.spawnAtLocation(arrow.getPickupItem(), 0.1F);
                  }

                  arrow.discard();
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
