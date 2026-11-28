package pyre.tinkerslevellingaddon.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import pyre.tinkerslevellingaddon.ImprovableModule;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.setup.AddonModifierIds;
import pyre.tinkerslevellingaddon.util.ModUtil;
import pyre.tinkerslevellingaddon.util.ToolLevellingUtil;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.shared.command.HeldModifiableItemIterator;

public class LevelsCommand {
   public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
      ((LiteralArgumentBuilder)subCommand.requires(sender -> sender.hasPermission(2)))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities())
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("add").executes(context -> run(context, ModCommands.Operation.ADD, 1)))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1)).executes(context -> run(context, ModCommands.Operation.ADD)))
                  ))
               .then(
                  Commands.literal("set")
                     .then(Commands.argument("count", IntegerArgumentType.integer(0)).executes(context -> run(context, ModCommands.Operation.SET)))
               )
         );
   }

   private static int run(CommandContext<CommandSourceStack> context, ModCommands.Operation op) throws CommandSyntaxException {
      return run(context, op, IntegerArgumentType.getInteger(context, "count"));
   }

   private static int run(CommandContext<CommandSourceStack> context, ModCommands.Operation op, int count) throws CommandSyntaxException {
      if (count > (Integer)Config.maxLevel.get() && op == ModCommands.Operation.SET) {
         throw new SimpleCommandExceptionType(ModUtil.makeTranslation("command", "levels.failure.set.invalid_count", Config.maxLevel.get())).create();
      } else {
         List<LivingEntity> successes = HeldModifiableItemIterator.apply(
            context,
            (living, stack) -> {
               if (ModifierUtil.getModifierLevel(stack, AddonModifierIds.IMPROVABLE) <= 0) {
                  return false;
               } else {
                  ToolStack tool = ToolStack.copyFrom(stack);
                  if (op == ModCommands.Operation.ADD) {
                     int levelsAdded = addLevel(tool, count);
                     if (levelsAdded == 0) {
                        throw new SimpleCommandExceptionType(ModUtil.makeTranslation("command", "levels.failure.add.already_max_level", stack.getDisplayName()))
                           .create();
                     }
                  } else if (!setLevel(tool, count)) {
                     throw new SimpleCommandExceptionType(ModUtil.makeTranslation("command", "levels.failure.set.current_level", stack.getDisplayName(), count))
                        .create();
                  }

                  Component error = tool.tryValidate();
                  if (error != null) {
                     throw ModCommands.TOOL_VALIDATION_ERROR.create(error);
                  } else {
                     living.setItemInHand(InteractionHand.MAIN_HAND, tool.createStack(stack.getCount()));
                     return true;
                  }
               }
            }
         );
         CommandSourceStack source = (CommandSourceStack)context.getSource();
         int size = successes.size();
         if (op == ModCommands.Operation.ADD) {
            if (size == 1) {
               source.sendSuccess(() -> ModUtil.makeTranslation("command", "levels.success.add.single", count, successes.get(0).getDisplayName()), true);
            } else {
               source.sendSuccess(() -> ModUtil.makeTranslation("command", "levels.success.add.multiple", count, size), true);
            }
         } else if (size == 1) {
            source.sendSuccess(() -> ModUtil.makeTranslation("command", "levels.success.set.single", count, successes.get(0).getDisplayName()), true);
         } else {
            source.sendSuccess(() -> ModUtil.makeTranslation("command", "levels.success.set.multiple", count, size), true);
         }

         return size;
      }
   }

   private static int addLevel(ToolStack tool, int count) {
      int currentLevel = tool.getPersistentData().getInt(ImprovableModule.LEVEL_KEY);
      boolean isBroad = ToolLevellingUtil.isBroadTool(tool);

      int levelsAdded;
      for (levelsAdded = 0; levelsAdded < count && ToolLevellingUtil.canLevelUp(currentLevel); levelsAdded++) {
         int xp = ToolLevellingUtil.getXpNeededForLevel(currentLevel + 1, isBroad);
         ToolLevellingUtil.addExperience(tool, xp);
         currentLevel++;
      }

      return levelsAdded;
   }

   private static boolean setLevel(ToolStack tool, int count) {
      ModDataNBT data = tool.getPersistentData();
      int currentLevel = data.getInt(ImprovableModule.LEVEL_KEY);
      int levelDiff = count - currentLevel;
      if (levelDiff == 0) {
         return false;
      } else {
         if (levelDiff > 0) {
            addLevel(tool, levelDiff);
         } else {
            data.putInt(ImprovableModule.LEVEL_KEY, count);
            trimHistory(ImprovableModule.SLOT_HISTORY_KEY, data, count);
            trimHistory(ImprovableModule.STAT_HISTORY_KEY, data, count);
         }

         tool.rebuildStats();
         return true;
      }
   }

   private static void trimHistory(ResourceLocation historyKey, ModDataNBT data, int size) {
      String newHistory = Arrays.stream(data.getString(historyKey).split(";")).limit(size).collect(Collectors.joining(";"));
      if (!newHistory.isBlank()) {
         newHistory = newHistory + ";";
      }

      data.putString(historyKey, newHistory);
   }
}
