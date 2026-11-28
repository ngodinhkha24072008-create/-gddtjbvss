package com.creeping_creeper.tinkers_thinking.common.modifer.ranged;

import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modifiers.ability.interaction.BlockingModifier;

public enum AtlatlModule implements ModifierModule, GeneralInteractionModifierHook {
   INSTANCE;

   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.GENERAL_INTERACT});
   public static final RecordLoadable<AtlatlModule> LOADER = new SingletonLoader(INSTANCE);

   public RecordLoadable<AtlatlModule> getLoader() {
      return LOADER;
   }

   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public Integer getPriority() {
      return 115;
   }

   public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
      return InteractionResult.PASS;
   }

   public UseAnim getUseAction(IToolStackView tool, ModifierEntry modifier) {
      return BlockingModifier.blockWhileCharging(tool, UseAnim.SPEAR);
   }
}
