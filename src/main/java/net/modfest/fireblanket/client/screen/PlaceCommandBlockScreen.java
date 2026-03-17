package net.modfest.fireblanket.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class PlaceCommandBlockScreen extends Screen {
	public PlaceCommandBlockScreen() {
		super(Component.empty());
	}

	@Override
	protected void init() {
		super.init();
		this.addRenderableWidget(
			new Button.Builder(Component.literal("Continue..."), (bw) -> minecraft.setScreen(null))
				.bounds((this.width / 2) + 50, (this.height / 2) + 58, 100, 20)
				.build()
		);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {

	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		this.renderTransparentBackground(context);

		int w = this.width / 2;
		int h = this.height / 2;

		context.fill(RenderPipelines.GUI, w - 175, h - 80, w + 175, h + 80, 0xFFC6C6C6);
		context.fillGradient(w - 173, h - 78, w + 173, h + 56, 0xFF0000AA, 0xFF000066);
		context.drawString(minecraft.font, "We trust you have received the usual lecture from the local", w - 170, h - 75, 0xFFFFFFFF, false);
		context.drawString(minecraft.font, "Performance Witch. It usually boils down to these three things:", w - 170, h - 75 + 12, 0xFFFFFFFF, false);
		context.drawString(minecraft.font, "    #1) Ensure your command is performant.", w - 170, h - 75 + (12 * 3), 0xFFFFFFFF, false);
		context.drawString(minecraft.font, "    #2) Think before you type.", w - 170, h - 75 + (12 * 4), 0xFFFFFFFF, false);
		context.drawString(minecraft.font, "    #3) With great power comes great responsibility.", w - 170, h - 75 + (12 * 5), 0xFFFFFFFF, false);

		context.drawString(minecraft.font, "This message shows only once.", w - 170, h + 63, 0xFF404040, false);

		super.render(context, mouseX, mouseY, delta);
	}
}
