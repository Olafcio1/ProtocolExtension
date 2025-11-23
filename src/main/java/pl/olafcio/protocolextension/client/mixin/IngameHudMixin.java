package pl.olafcio.protocolextension.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.olafcio.protocolextension.client.state.hud.HudState;

import java.awt.*;

@Mixin(InGameHud.class)
public class IngameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("TAIL"), method = "render")
    public void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        for (var item : HudState.elements.values()) {
            var pos = item.pos();
            var text = item.text();

            context.drawTextWithShadow(
                    client.textRenderer,
                    text,
                    (int) (pos.x() * context.getScaledWindowWidth()),
                    (int) (pos.y() * context.getScaledWindowHeight()),
                    Color.WHITE.getRGB()
            );
        }
    }
}
