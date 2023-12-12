package net.modfest.fireblanket.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class PlaceCommandBlockScreen extends Screen {
	public PlaceCommandBlockScreen() {
		super(Text.empty());
	}

	@Override
	protected void init() {
		super.init();
		this.addDrawableChild(
				new ButtonWidget.Builder(Text.literal("Continue..."), (bw) -> client.setScreen(null))
						.dimensions((this.width / 2) + 50, (this.height / 2) + 58, 100, 20)
						.build()
		);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);

		int w = this.width / 2;
		int h = this.height / 2;

		context.fill(w - 175, h - 80, w + 175, h + 80, 0, 0xFFC6C6C6);
		context.fillGradient(w - 173, h - 78, w + 173, h + 56, 0, 0xFF0000AA, 0xFF000066);
		context.drawText(client.textRenderer, "We trust you have received the usual lecture from the local", w - 170, h - 75, 0xFFFFFFFF, false);
		context.drawText(client.textRenderer, "Performance Witch. It usually boils down to these three things:", w - 170, h - 75 + 12, 0xFFFFFFFF, false);
		context.drawText(client.textRenderer, "    #1) Ensure your command is performant.", w - 170, h - 75 + (12 * 3), 0xFFFFFFFF, false);
		context.drawText(client.textRenderer, "    #2) Think before you type.", w - 170, h - 75 + (12 * 4), 0xFFFFFFFF, false);
		context.drawText(client.textRenderer, "    #3) With great power comes great responsibility.", w - 170, h - 75 + (12 * 5), 0xFFFFFFFF, false);

		context.drawText(client.textRenderer, "This message shows only once.", w - 170, h + 63, 0xFF404040, false);

		super.render(context, mouseX, mouseY, delta);
	}
}
