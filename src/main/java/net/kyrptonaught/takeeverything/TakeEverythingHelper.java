package net.kyrptonaught.takeeverything;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;

public class TakeEverythingHelper {
    public static boolean takeEverything(ServerPlayerEntity player) {
        if (!TakeEverythingMod.getConfig().Enabled || player.currentScreenHandler instanceof PlayerScreenHandler) {
            return false;
        }

        if(player.isSpectator() && !TakeEverythingMod.getConfig().worksInSpectator) {
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

    public static Boolean canEquip(PlayerEntity player, ItemStack armor) {
        if (!TakeEverythingMod.getConfig().Enabled) return false;
        EquipmentSlot equipmentSlot = ((ArmorItem) armor.getItem()).getSlotType();
        ItemStack equipped = player.getEquippedStack(equipmentSlot);
        return equipped.isEmpty();
    }

    public static Boolean canSwap(PlayerEntity player, ItemStack armor) {
        if (!TakeEverythingMod.getConfig().Enabled) return false;
        EquipmentSlot equipmentSlot = ((ArmorItem) armor.getItem()).getSlotType();
        ItemStack equipped = player.getEquippedStack(equipmentSlot);
        if (equipped.getItem() instanceof ArmorItem onArmor && canRemove(equipped))
            return ((ArmorItem) armor.getItem()).getProtection() > onArmor.getProtection();
        return false;
    }

    public static ItemStack equipOrSwapArmor(PlayerEntity player, ItemStack armor) {
        EquipmentSlot equipmentSlot = ((ArmorItem) armor.getItem()).getSlotType();
        ItemStack equipped = player.getEquippedStack(equipmentSlot);
        if (equipped.isEmpty()) {
            player.equipStack(equipmentSlot, armor.copy());
            playSound(armor, (ServerPlayerEntity) player);
            armor.setCount(0);
        } else if (equipped.getItem() instanceof ArmorItem onArmor && canRemove(equipped)) {
            if (((ArmorItem) armor.getItem()).getProtection() > onArmor.getProtection()) {
                ItemStack copy = equipped.copy();
                player.equipStack(equipmentSlot, armor.copy());
                playSound(armor, (ServerPlayerEntity) player);
                armor.setCount(0);
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }

    public static Boolean canRemove(ItemStack stack) {
        return !stack.hasEnchantments();
    }

    public static void playSound(ItemStack stack, ServerPlayerEntity player) {
        SoundEvent soundEvent = stack.getEquipSound();
        if (stack.isEmpty() || soundEvent == null) {
            return;
        }
        player.playSound(soundEvent, player.getSoundCategory(), 1, 1);
    }
}
