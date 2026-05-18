package net.modfest.fireblanket.mixin.client.bakery;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 **/
@Mixin({ShelfBlockEntity.class, SignBlockEntity.class})
public abstract class MixinGenericBlockEntity extends BlockEntity {

	public MixinGenericBlockEntity(
		final BlockEntityType<?> type,
		final BlockPos worldPosition,
		final BlockState blockState
	) {
		super(type, worldPosition, blockState);
	}

	@Inject(method = "loadAdditional", at = @At("RETURN"))
	protected void onLoadAdditional(CallbackInfo ci) {
		this.rebake(false);
	}

	@Unique
	protected void rebake(boolean immediate) {
		if (!this.hasLevel() || !this.getLevel().isClientSide()) {
			return;
		}
		this.getLevel()
			.sendBlockUpdated(this.getBlockPos(),
				this.getBlockState(),
				this.getBlockState(),
				immediate ? Block.UPDATE_IMMEDIATE : 0
			);
	}
}
