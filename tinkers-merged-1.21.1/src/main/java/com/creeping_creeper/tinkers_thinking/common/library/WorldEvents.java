package com.creeping_creeper.tinkers_thinking.common.library;

import com.creeping_creeper.tinkers_thinking.common.register.ModCommonItems;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import com.creeping_creeper.tinkers_thinking.common.register.ModToolItems;
import com.creeping_creeper.tinkers_thinking.data.ModTags;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChainBlock;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import modernmods.modernfoundry.common.TinkerDamageTypes;

@EventBusSubscriber(
   modid = "tinkers_thinking",
   bus = Bus.GAME
)
public class WorldEvents {
   @SubscribeEvent
   static void mobKilled(LivingDropsEvent event) {
      DamageSource source = event.getSource();
      LivingEntity dying = event.getEntity();
      boolean a = source.getDirectEntity() instanceof Snowball;
      boolean b = source.getDirectEntity() instanceof ThrownPotion potion
         && potion.getItem().getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS, net.minecraft.world.item.alchemy.PotionContents.EMPTY).is(Potions.WATER);
      if (dying instanceof Blaze && (source.is(ModTags.DamageTypes.drop_ashes) || a || b)) {
         dropItem(dying, (Item)ModCommonItems.ashes.get());
      }

      if (source.is(DamageTypes.SONIC_BOOM) && dying.hasEffect(ModEffects.sculk_power)) {
         if (dying instanceof Creeper) {
            dropItem(dying, ((ChainBlock)ModCommonItems.soul_vine.get()).asItem());
         }

         if (dying instanceof IronGolem) {
            dropItem(dying, ((ChainBlock)ModCommonItems.bound_chain.get()).asItem());
         }
      }
   }

   @SubscribeEvent
   static void blockBroke(BreakEvent event) {
      Player player = event.getPlayer();
      Level level = (Level)event.getLevel();
      if (!level.isClientSide() && event.getState().is(ModTags.Blocks.zith) && player != null) {
         player.hurt(TinkerDamageTypes.source(level.registryAccess(), TinkerDamageTypes.BLEEDING), 20.0F);
      }
   }

   private static void dropItem(LivingEntity dying, Item item) {
      ItemEntity itementity = new ItemEntity(dying.level(), dying.getX(), dying.getY(), dying.getZ(), new ItemStack(item));
      itementity.setDefaultPickUpDelay();
      dying.level().addFreshEntity(itementity);
   }

   @SubscribeEvent
   static void villagerTrades(VillagerTradesEvent event) {
      if (event.getType().name().equals("weaponsmith")) {
         ((List)event.getTrades().get(5)).add(emeraldToItemsTrade(ModToolItems.cutlass, Mth.nextInt(RandomSource.create(), 25, 45), 1, 5));
      }
   }

   public static BasicItemListing emeraldToItemsTrade(ItemLike item, int count, int maxTrades, int xp) {
      return new BasicItemListing(count, new ItemStack(item), maxTrades, xp, 0.2F);
   }
}
