package duper.mixin;

import duper.SharedVariables;
import duper.Util;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ClientConnectionMixin {
    @Inject(at = @At("TAIL"), method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", cancellable = true)
    public void send(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo ci) {
        if (packet instanceof ServerboundPlayerActionPacket) {
            if (((ServerboundPlayerActionPacket) packet).getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                if (SharedVariables.shouldDupe) {
                    Util.quickMoveItem(0);
                    SharedVariables.shouldDupe = false;
                } else if (SharedVariables.shouldDupeAll) {
                    Util.quickMoveAllItems();
                    SharedVariables.shouldDupeAll = false;
                }
            }
        }
    }
}
