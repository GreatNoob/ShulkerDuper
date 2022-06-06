package duper;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ClickType;

public class Util {
    public static final Minecraft CLIENT = Minecraft.getInstance();

    public static void log(String msg) {
        CLIENT.player.displayClientMessage(new TextComponent("[Shulker Dupe]: " + msg), false);
    }
    public static void quickMoveAllItems() {
        for (int i = 0; i < 27; i++) {
            quickMoveItem(i);
        }
    }

    public static void quickMoveItem(int slot) {
        if (CLIENT.screen instanceof ShulkerBoxScreen) {
            ShulkerBoxMenu menu = ((ShulkerBoxScreen) CLIENT.screen).getMenu();
            Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap<>();
            stack.put(slot, menu.getSlot(slot).getItem());
            CLIENT.getConnection().send(new ServerboundContainerClickPacket(menu.containerId, 0, slot, 0, ClickType.QUICK_MOVE, menu.getSlot(0).getItem(), stack));
        }
    }
}
