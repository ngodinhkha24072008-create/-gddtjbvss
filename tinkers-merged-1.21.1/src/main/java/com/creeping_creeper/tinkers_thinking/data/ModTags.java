package com.creeping_creeper.tinkers_thinking.data;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import modernmods.hilt.Hilt;

public class ModTags {
   public static class Blocks {
      public static final TagKey<Block> cooling_fast = local("cooling_fast");
      public static final TagKey<Block> zith = local("zith");

      private static TagKey<Block> local(String name) {
         return TagKey.create(Registries.BLOCK, TinkersThinking.getResource(name));
      }

      private static TagKey<Block> common(String name) {
         return TagKey.create(Registries.BLOCK, Hilt.commonResource(name));
      }
   }

   public static class DamageTypes {
      public static final TagKey<DamageType> drop_ashes = local("drop_ashes");

      private static TagKey<DamageType> local(String name) {
         return TagKey.create(Registries.DAMAGE_TYPE, TinkersThinking.getResource(name));
      }

      private static TagKey<Block> common(String name) {
         return TagKey.create(Registries.BLOCK, Hilt.commonResource(name));
      }
   }

   public static class EntityTypes {
      public static final TagKey<EntityType<?>> resisting = local("resisting");

      private static TagKey<EntityType<?>> local(String name) {
         return TagKey.create(Registries.ENTITY_TYPE, TinkersThinking.getResource(name));
      }

      private static TagKey<EntityType<?>> common(String name) {
         return TagKey.create(Registries.ENTITY_TYPE, Hilt.commonResource(name));
      }
   }

   public static class Items {
      public static final TagKey<Item> LOADING_ANIMATION = local("loading_animation");

      private static TagKey<Item> local(String name) {
         return TagKey.create(Registries.ITEM, TinkersThinking.getResource(name));
      }

      private static TagKey<Item> common(String name) {
         return TagKey.create(Registries.ITEM, Hilt.commonResource(name));
      }
   }
}
