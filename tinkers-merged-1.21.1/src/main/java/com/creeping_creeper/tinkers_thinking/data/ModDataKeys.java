package com.creeping_creeper.tinkers_thinking.data;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import modernmods.modernfoundry.library.tools.capability.TinkerDataKeys;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.TinkerDataKey;

public interface ModDataKeys {
   TinkerDataKey<Integer> SculkCatalyse = intKey("sculk_catalyse");
   TinkerDataKey<Integer> Antibrute = intKey("antibrute");
   TinkerDataKey<Integer> Reburning = intKey("reburning");
   TinkerDataKey<Integer> SculkStruggle = intKey("sculk_struggle");
   TinkerDataKey<Integer> Retransit = intKey("retransit");
   TinkerDataKey<Integer> Remisdirection = intKey("remisdirection");
   TinkerDataKey<Integer> SculkBoost = intKey("sculk_boost");

   static void init() {
   }

   private static TinkerDataKey<Integer> intKey(String name) {
      return (TinkerDataKey<Integer>)TinkerDataKeys.INTEGER_REGISTRY.register(TinkersThinking.createKey(name));
   }
}
