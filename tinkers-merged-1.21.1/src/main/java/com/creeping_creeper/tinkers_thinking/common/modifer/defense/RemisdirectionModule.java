package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.data.ModDataKeys;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.events.teleport.SlingModifierTeleportEvent;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.DamageBlockModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.Holder;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.TinkerModifiers;

public enum RemisdirectionModule implements ModifierModule, DamageBlockModifierHook, ModifierUtils {
   INSTANCE;

   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.DAMAGE_BLOCK});
   public static final RecordLoadable<RemisdirectionModule> LOADER = new SingletonLoader(INSTANCE);

   @NotNull
   public RecordLoadable<RemisdirectionModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public boolean isDamageBlocked(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount
   ) {
      LivingEntity living = context.getEntity();
      if (source.getEntity() != null && !living.hasEffect((MobEffect)TinkerModifiers.teleportCooldownEffect.get()) && this.reverse(tool)) {
         Vec3 look = living.getLookAngle();
         Level level = context.getLevel();
         double offX = look.x * 5.0;
         double offZ = look.y * 5.0;

         BlockPos furthestPos;
         for (furthestPos = null; Math.abs(offX) > 0.5 || Math.abs(offZ) > 0.5; offZ -= Math.abs(offZ) > 0.25 ? (offZ >= 0.0 ? 1 : -1) * 0.25 : 0.0) {
            BlockPos posAttempt = BlockPos.containing(living.getX() + offX, living.getY(), living.getZ() + offZ);
            if (furthestPos == null) {
               if (level.getWorldBorder().isWithinBounds(posAttempt) && !level.getBlockState(posAttempt).isSuffocating(level, posAttempt)) {
                  furthestPos = posAttempt;
               }
            } else if (level.getBlockState(posAttempt).getDestroySpeed(level, posAttempt) == -1.0F) {
               furthestPos = null;
            }

            offX -= Math.abs(offX) > 0.25 ? (offX >= 0.0 ? 1 : -1) * 0.25 : 0.0;
         }

         if (furthestPos != null) {
            SlingModifierTeleportEvent event = new SlingModifierTeleportEvent(
               living, furthestPos.getX() + 0.5F, furthestPos.getY(), furthestPos.getZ() + 0.5F, tool, modifier
            );
            NeoForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
               this.block(living);
               Optional<Holder> dataCap = living.getCapability(TinkerDataCapability.CAPABILITY).resolve();
               dataCap.ifPresent(
                  data -> this.addEffect(
                     living,
                     (MobEffect)TinkerModifiers.teleportCooldownEffect.get(),
                     Mth.clamp((int)amount / (Integer)data.get(ModDataKeys.Remisdirection, 1), 10, 60) * 20
                  )
               );
               living.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
               return true;
            }
         }
      }

      return false;
   }
}
