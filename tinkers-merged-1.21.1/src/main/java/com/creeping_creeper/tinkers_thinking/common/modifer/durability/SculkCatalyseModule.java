package com.creeping_creeper.tinkers_thinking.common.modifer.durability;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import com.creeping_creeper.tinkers_thinking.common.register.ModEffects;
import com.creeping_creeper.tinkers_thinking.data.ModModifierIds;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.display.DurabilityDisplayModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public enum SculkCatalyseModule
   implements ModifierModule,
   ToolDamageModifierHook,
   DurabilityDisplayModifierHook,
   InventoryTickModifierHook,
   ModifierRemovalHook,
   ModifierUtils {
   INSTANCE;

   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(
      new ModuleHook[]{ModifierHooks.TOOL_DAMAGE, ModifierHooks.DURABILITY_DISPLAY, ModifierHooks.INVENTORY_TICK, ModifierHooks.REMOVE}
   );
   public static final RecordLoadable<SculkCatalyseModule> LOADER = new SingletonLoader(INSTANCE);
   private static final ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath("tinkers_thinking", "sculk_catalyse");

   @NotNull
   public RecordLoadable<SculkCatalyseModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public int onDamageTool(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
      if (holder != null && holder.hasEffect(ModEffects.sculk_power)) {
         amount--;
      }

      return amount;
   }

   @Nullable
   public Boolean showDurabilityBar(IToolStackView tool, ModifierEntry modifier) {
      return tool.getPersistentData().contains(KEY, 1) ? true : null;
   }

   public int getDurabilityWidth(IToolStackView tool, ModifierEntry modifier) {
      return 0;
   }

   public int getDurabilityRGB(IToolStackView tool, ModifierEntry modifier) {
      return tool.getPersistentData().contains(KEY, 1) ? 37525 : -1;
   }

   public void onInventoryTick(
      IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack
   ) {
      if (holder instanceof Player) {
         if (holder.hasEffect(ModEffects.sculk_power)) {
            tool.getPersistentData().putBoolean(KEY, true);
         }

         if (!holder.hasEffect(ModEffects.sculk_power)) {
            tool.getPersistentData().remove(KEY);
         }
      }
   }

   @Nullable
   public Component onRemoved(IToolStackView tool, Modifier modifier) {
      if (tool.getModifierLevel(ModModifierIds.SculkCatalyse) == 0) {
         tool.getPersistentData().remove(KEY);
      }

      return null;
   }
}
