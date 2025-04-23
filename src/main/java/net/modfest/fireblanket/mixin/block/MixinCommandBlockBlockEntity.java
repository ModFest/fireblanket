package net.modfest.fireblanket.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(CommandBlockBlockEntity.class)
public abstract class MixinCommandBlockBlockEntity extends BlockEntity implements CommandBE {
	@Unique
	private UUID fireblanket$owner;
	@Unique
	private UUID fireblanket$lastUpdated;

	public MixinCommandBlockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void fireblanket$writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
		if (fireblanket$owner != null) {
			nbt.putUuid("FB:Owner", fireblanket$owner);
		}

		if (fireblanket$lastUpdated != null) {
			nbt.putUuid("FB:LastUpdated", fireblanket$lastUpdated);
		}
	}

	@Inject(method = "readNbt", at = @At("TAIL"))
	private void fireblanket$readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
		if (nbt.contains("FB:Owner")) {
			fireblanket$owner = nbt.getUuid("FB:Owner");
		}

		if (nbt.contains("FB:LastUpdated")) {
			fireblanket$lastUpdated = nbt.getUuid("FB:LastUpdated");
		}
	}

	@Override
	public void fireblanket$setOwner(UUID uuid) {
		this.fireblanket$owner = uuid;
		markDirty();
	}

	@Override
	public void fireblanket$setLastUpdate(UUID uuid) {
		this.fireblanket$lastUpdated = uuid;
		markDirty();
	}

	@Override
	public UUID fireblanket$getOwner() {
		return fireblanket$owner;
	}

	@Override
	public UUID fireblanket$getLastUpdate() {
		return fireblanket$lastUpdated;
	}
}
