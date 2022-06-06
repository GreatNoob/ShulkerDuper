package duper.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import duper.ShulkerDuper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static duper.SharedVariables.*;

@Mixin(ShulkerBoxScreen.class)
public class ShulkerBoxScreenMixin extends Screen {
    public ShulkerBoxScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V")
    public void renderScreen(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (isFra()) {
            setFra(false);
            ShulkerDuper.thex = this.width;
            ShulkerDuper.they = this.height;
            this.addRenderableWidget(new Button(this.width/2-90, this.height/2+35-145, 50, 20, new TextComponent("Dupe"), (button) -> {
                if (shouldDupeAll) shouldDupeAll = false;
                shouldDupe = true;
            }));

            this.addRenderableWidget(new Button(this.width/2+40, this.height/2+35-145, 50, 20, new TextComponent("Dupe All"), (button) -> {
                if (shouldDupe) shouldDupe = false;
                shouldDupeAll = true;
            }));
        }

        if (this.width != ShulkerDuper.thex || this.height != ShulkerDuper.they) {
            setFra(true);
        }

    }

    public boolean isFra() {
        return ShulkerDuper.fra;
    }

    public void setFra(boolean fra) {
        ShulkerDuper.fra = fra;
    }
}
