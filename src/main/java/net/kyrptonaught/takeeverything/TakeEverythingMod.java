package net.kyrptonaught.takeeverything;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.kyrptonaught.kyrptconfig.config.ConfigManager;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.TypedActionResult;


public class TakeEverythingMod implements ModInitializer {
    public static final String MOD_ID = "takeeverything";
    public static ConfigManager.SingleConfigManager configManager = new ConfigManager.SingleConfigManager(MOD_ID, new TakeEverythingConfig());


    @Override
    public void onInitialize() {
        configManager.load();
        CommandRegistrationCallback.EVENT.register(TakeEverythingMod::registerCommand);
        TakeEverythingNetworking.registerReceivePacket();
        registerItemUse();
    }

    public static TakeEverythingConfig getConfig() {
        return (TakeEverythingConfig) configManager.getConfig();
    }

    public static void registerItemUse() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient && stack.getItem() instanceof ArmorItem) {
                stack = TakeEverythingHelper.equipOrSwapArmor(player, stack); //return already equippedStack or empty
                if (!stack.isEmpty()) player.setStackInHand(hand, stack);
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        });
    }

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("takeeverything")
                .requires((source) -> source.hasPermissionLevel(2))
                .executes(context -> {
                    if (!TakeEverythingHelper.takeEverything(context.getSource().getPlayer()))
                        context.getSource().sendFeedback(new LiteralText("You must have an inventory open"), false);
                    return 1;
                })
                .then(CommandManager.literal("enabled").then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().Enabled = BoolArgumentType.getBool(context, "enabled");
                            configManager.save();
                            return 1;
                        })))
                .then(CommandManager.literal("worksInSpectator").then(CommandManager.argument("worksInSpectator", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().worksInSpectator = BoolArgumentType.getBool(context, "worksInSpectator");
                            configManager.save();
                            return 1;
                        })))
                .then(CommandManager.literal("deleteItemNotDrop").then(CommandManager.argument("deleteItemNotDrop", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().deleteItemNotDrop = BoolArgumentType.getBool(context, "deleteItemNotDrop");
                            configManager.save();
                            return 1;
                        })))
        );
    }
}
