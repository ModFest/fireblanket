package net.modfest.fireblanket.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(CommandBlockEntity.class)
public abstract class MixinCommandBlockBlockEntity extends BlockEntity implements CommandBE {
	@Shadow
	@Final
	private BaseCommandBlock commandBlock;

	public MixinCommandBlockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public BaseCommandBlock fireblanket$getCommandExecutor() {
		return this.commandBlock;
	}

	@Override
	public void fireblanket$setOwner(UUID uuid) {
		((CommandBE) commandBlock).fireblanket$setOwner(uuid);
		setChanged();
	}

	@Override
	public void fireblanket$setLastUpdate(UUID uuid) {
		((CommandBE) commandBlock).fireblanket$setLastUpdate(uuid);
		setChanged();
	}

	@Override
	public UUID fireblanket$getOwner() {
		return ((CommandBE) commandBlock).fireblanket$getOwner();
	}

	@Override
	public UUID fireblanket$getLastUpdate() {
		return ((CommandBE) commandBlock).fireblanket$getLastUpdate();
	}

	@Override
	public HoverEvent fireblanket$getBlame() {
		return ((CommandBE) commandBlock).fireblanket$getBlame();
	}
}
