package net.modfest.fireblanket.mixin.criterion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerPlayer.class)
public class MixinServerPlayerEntity {
	/**
	 * @author jaskarth
	 *
	 * @reason It's not worth it to call the criterion.
	 */
	@Overwrite
	public void onInsideBlock(BlockState state) {
	}
}
