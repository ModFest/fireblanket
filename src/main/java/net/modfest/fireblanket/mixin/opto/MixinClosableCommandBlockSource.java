package net.modfest.fireblanket.mixin.opto;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;
import net.modfest.fireblanket.mixinsupport.CommandBlockShim;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Ampflower
 **/
@Mixin(targets = "net/minecraft/world/level/BaseCommandBlock$CloseableCommandBlockSource")
public class MixinClosableCommandBlockSource {
	@Shadow
	@Final
	@NonNull
	BaseCommandBlock this$0;

	@Shadow
	@Final
	private ServerLevel level;

	@Shadow
	private boolean closed;

	/**
	 * @author Ampflower
	 * @reason Optimize message tracking. DATE_FORMAT takes a lot of CPU time.
	 */
	@Overwrite
	public void sendSystemMessage(final Component message) {
		if (this.closed) {
			return;
		}

		final CommandBlockShim self = (CommandBlockShim) this$0;

		if (self.fireblanket$setLastOutput(message)) {
			this$0.onUpdated(this.level);
		}
	}
}
