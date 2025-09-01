package net.modfest.fireblanket.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CommandBlockExecutor;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(CommandBlockBlockEntity.class)
public abstract class MixinCommandBlockBlockEntity extends BlockEntity implements CommandBE {
	@Shadow
	@Final
	private CommandBlockExecutor commandExecutor;

	public MixinCommandBlockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public CommandBlockExecutor fireblanket$getCommandExecutor() {
		return this.commandExecutor;
	}

	@Override
	public void fireblanket$setOwner(UUID uuid) {
		((CommandBE) commandExecutor).fireblanket$setOwner(uuid);
		markDirty();
	}

	@Override
	public void fireblanket$setLastUpdate(UUID uuid) {
		((CommandBE) commandExecutor).fireblanket$setLastUpdate(uuid);
		markDirty();
	}

	@Override
	public UUID fireblanket$getOwner() {
		return ((CommandBE) commandExecutor).fireblanket$getOwner();
	}

	@Override
	public UUID fireblanket$getLastUpdate() {
		return ((CommandBE) commandExecutor).fireblanket$getLastUpdate();
	}

	@Override
	public HoverEvent fireblanket$getBlame() {
		return ((CommandBE) commandExecutor).fireblanket$getBlame();
	}
}
