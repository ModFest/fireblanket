package net.modfest.fireblanket.mixin.criterion;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {
	/**
	 * @author jaskarth
	 *
	 * @reason It's not worth it to call the criterion.
	 */
	@Overwrite
	public void onBlockCollision(BlockState state) {
	}
}
