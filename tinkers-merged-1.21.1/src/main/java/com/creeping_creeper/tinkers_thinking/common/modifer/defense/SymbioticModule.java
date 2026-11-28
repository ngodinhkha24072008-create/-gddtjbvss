package com.creeping_creeper.tinkers_thinking.common.modifer.defense;

import com.creeping_creeper.tinkers_thinking.common.library.ModifierUtils;
import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.common.TinkerTags.Items;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook.RedirectAfter;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

public record SymbioticModule(LevelingValue amount)
   implements ModifierModule,
   MeleeHitModifierHook,
   RedirectAfter,
   ProjectileLaunchModifierHook,
   ModifyDamageModifierHook,
   ModifierUtils {
   private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.defaultHooks(
      new ModuleHook[]{ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.MODIFY_DAMAGE}
   );
   public static final RecordLoadable<SymbioticModule> LOADER = RecordLoadable.create(
      LevelingValue.LOADABLE.directField(SymbioticModule::amount), SymbioticModule::new
   );

   @NotNull
   public RecordLoadable<SymbioticModule> getLoader() {
      return LOADER;
   }

   @NotNull
   public List<ModuleHook<?>> getDefaultHooks() {
      return DEFAULT_HOOKS;
   }

   public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
      LivingEntity entity = context.getPlayerAttacker();
      float level = modifier.getEffectiveLevel();
      if (!context.isExtraAttack()
         && context.isFullyCharged()
         && Modifier.RANDOM.nextFloat() < level * this.amount.eachLevel()
         && entity != null
         && entity.getHealth() < entity.getMaxHealth()
         && !tool.isBroken()) {
         this.eat(tool, modifier, entity);
      }
   }

   public void onProjectileLaunch(
      IToolStackView tool,
      ModifierEntry modifier,
      LivingEntity shooter,
      Projectile projectile,
      @Nullable AbstractArrow arrow,
      ModDataNBT persistentData,
      boolean primary
   ) {
      float level = modifier.getEffectiveLevel();
      if (primary && Modifier.RANDOM.nextFloat() < level * this.amount.eachLevel() && shooter.getHealth() < shooter.getMaxHealth() && !tool.isBroken()) {
         this.eat(tool, modifier, shooter);
      }
   }

   public float modifyDamageTaken(
      IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float damage, boolean isDirectDamage
   ) {
      LivingEntity living = context.getEntity();
      float level = modifier.getEffectiveLevel();
      if (Modifier.RANDOM.nextFloat() < level * this.amount.eachLevel()
         && living.getHealth() < living.getMaxHealth()
         && !tool.isBroken()
         && tool.hasTag(Items.ARMOR)) {
         this.eat(tool, modifier, living);
      }

      return damage;
   }

   private void eat(IToolStackView tool, ModifierEntry modifier, LivingEntity entity) {
      if (entity instanceof Player player) {
         int level = modifier.getLevel();
         this.heal(player, Math.max((float)level, (player.getMaxHealth() - player.getHealth()) * 0.3F));
         if (ToolDamageUtil.directDamage(tool, 8 * level, player, player.getUseItem())) {
            player.onEquippedItemBroken(player.getItemInHand(player.getUsedItemHand()).getItem(), net.minecraft.world.entity.LivingEntity.getSlotForHand(player.getUsedItemHand()));
         }
      }
   }
}
