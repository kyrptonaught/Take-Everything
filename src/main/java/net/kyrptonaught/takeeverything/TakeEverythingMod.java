package net.kyrptonaught.takeeverything;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.sun.jdi.connect.Connector;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.TypedActionResult;


public class TakeEverythingMod implements ModInitializer {
    public static boolean Enabled = true;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(TakeEverythingMod::register);

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient && stack.getItem() instanceof ArmorItem) {
                stack = TakeEverythingMod.equipOrSwapArmor(player, stack); //return already equippedStack or empty
                if (!stack.isEmpty()) player.setStackInHand(hand, stack);
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        });
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("takeeverything")
                .requires((source) -> source.hasPermissionLevel(2))
                .executes(context -> {
                    if (!takeEverything(context.getSource().getPlayer()))
                        context.getSource().sendFeedback(new LiteralText("You must have an inventory open"), false);
                    return 1;
                })
                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            Enabled = BoolArgumentType.getBool(context, "enabled");
                            return 1;
                        })));
    }

    public static boolean takeEverything(ServerPlayerEntity player) {
        if (!Enabled || player.currentScreenHandler instanceof PlayerScreenHandler) {
            return false;
        }
        for (int i = 0; i < player.currentScreenHandler.slots.size(); i++) {
            Slot slot = player.currentScreenHandler.slots.get(i);
            if (!(slot.inventory instanceof PlayerInventory) && slot.canTakeItems(player) && !slot.getStack().isEmpty()) {
                player.currentScreenHandler.onSlotClick(i, 0, SlotActionType.QUICK_MOVE, player);
            }
        }
        return true;
    }

    public static ItemStack equipOrSwapArmor(PlayerEntity player, ItemStack armor) {
        EquipmentSlot equipmentSlot = ((ArmorItem) armor.getItem()).getSlotType();
        ItemStack equipped = player.getEquippedStack(equipmentSlot);
        if (equipped.isEmpty()) {
            player.equipStack(equipmentSlot, armor.copy());
            armor.setCount(0);
        } else if (equipped.getItem() instanceof ArmorItem onArmor) {
            if (((ArmorItem) armor.getItem()).getProtection() > onArmor.getProtection()) {
                ItemStack copy = equipped.copy();
                player.equipStack(equipmentSlot, armor.copy());
                armor.setCount(0);
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }
}
