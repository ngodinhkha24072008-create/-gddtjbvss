package pyre.tinkerslevellingaddon.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ModCommands {
   public static final int PERMISSION_GAME_COMMANDS = 2;
   public static final DynamicCommandExceptionType TOOL_VALIDATION_ERROR = new DynamicCommandExceptionType(Component.class::cast);

   public static void init() {
      NeoForge.EVENT_BUS.addListener(ModCommands::registerCommand);
   }

   private static void register(LiteralArgumentBuilder<CommandSourceStack> root, String name, Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer) {
      LiteralArgumentBuilder<CommandSourceStack> subCommand = Commands.literal(name);
      consumer.accept(subCommand);
      root.then(subCommand);
   }

   private static void registerCommand(RegisterCommandsEvent event) {
      LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("tinkerslevellingaddon");
      register(builder, "levels", LevelsCommand::register);
      register(builder, "xp", ExperienceCommand::register);
      event.getDispatcher().register(builder);
   }

   static enum Operation {
      ADD,
      SET;
   }
}
