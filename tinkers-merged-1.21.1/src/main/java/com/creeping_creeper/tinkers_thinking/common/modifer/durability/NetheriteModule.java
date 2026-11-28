package com.creeping_creeper.tinkers_thinking.common.modifer.durability;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.common.TinkerTags.Items;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

public enum NetheriteModule implements ModifierModule, ToolDamageModifierHook, ModifyDamageModifierHook {
   INSTANCE;

   private static final ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath("tinkers_thinking", "netherite");
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(new ModuleHook[]{ModifierHooks.TOOL_DAMAGE, ModifierHooks.MODIFY_HURT});
   public static final RecordLoadable<NetheriteModule> LOADER = new SingletonLoader(INSTANCE);

   @NotNull
   public RecordLoadable<NetheriteModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
      if (tool.getPersistentData().getBoolean(KEY)) {
         amount = 0;
         tool.getPersistentData().remove(KEY);
      }

      return amount;
   }

   public float modifyDamageTaken(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage
   ) {
      if (source.is(DamageTypeTags.IS_FIRE) && !source.is(DamageTypeTags.BYPASSES_ARMOR) && tool.hasTag(Items.WORN_ARMOR)) {
         tool.getPersistentData().putBoolean(KEY, true);
         amount = 0.0F;
      }

      return amount;
   }
}
