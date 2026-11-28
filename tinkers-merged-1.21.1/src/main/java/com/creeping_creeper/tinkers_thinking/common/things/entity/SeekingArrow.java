package com.creeping_creeper.tinkers_thinking.common.things.entity;

import java.util.ArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.NetworkHooks;
import modernmods.modernfoundry.tools.entity.ModifiableArrow;

public class SeekingArrow extends ModifiableArrow {
   private static final EntityDataAccessor<Integer> ARC_TOWARDS_ENTITY_ID = SynchedEntityData.defineId(SeekingArrow.class, EntityDataSerializers.INT);
   private boolean stopSeeking;

   public SeekingArrow(EntityType<SeekingArrow> entityType, Level level) {
      super(entityType, level);
   }

   public SeekingArrow(Level level, double x, double y, double z) {
      super(level, x, y, z);
   }

   public SeekingArrow(Level level, LivingEntity shooter) {
      super(level, shooter);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ARC_TOWARDS_ENTITY_ID, -1);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public void tick() {
      super.tick();
      int id = (Integer)this.entityData.get(ARC_TOWARDS_ENTITY_ID);
      if (!this.inGround && !this.stopSeeking) {
         if (id == -1) {
            if (!this.level().isClientSide) {
               LivingEntity closest = null;
               Entity owner = this.getOwner();
               float boxExpandBy = Math.min(10, 3 + this.tickCount / 4);

               for (Entity entity : new ArrayList(this.level().getEntities(this, this.getBoundingBox().inflate(boxExpandBy), x$0 -> this.canHitEntity(x$0)))) {
                  if ((closest == null || entity.distanceTo(this) < closest.distanceTo(this))
                     && entity instanceof LivingEntity living
                     && !this.ownedBy(entity)
                     && (owner == null || !entity.isAlliedTo(owner))
                     && !living.hasEffect(MobEffects.INVISIBILITY)) {
                     closest = living;
                  }
               }

               if (closest != null) {
                  this.entityData.set(ARC_TOWARDS_ENTITY_ID, closest.getId());
               }
            }
         } else {
            Entity arcTowards = this.level().getEntity(id);
            if (arcTowards != null) {
               Vec3 arcVec = arcTowards.position().add(0.0, 0.65F * arcTowards.getBbHeight(), 0.0).subtract(this.position());
               if (arcVec.length() > arcTowards.getBbWidth()) {
                  this.setDeltaMovement(this.getDeltaMovement().scale(0.3).add(arcVec.normalize().scale(0.7)));
               }
            }
         }
      }
   }

   public void doPostHurtEffects(LivingEntity entity) {
      this.stopSeeking = true;
   }
}
