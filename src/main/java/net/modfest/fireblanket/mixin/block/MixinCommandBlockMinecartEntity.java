package net.modfest.fireblanket.mixin.block;

import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.world.CommandBlockExecutor;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

/**
 * @author Ampflower
 */
@Mixin(CommandBlockMinecartEntity.class)
public class MixinCommandBlockMinecartEntity implements CommandBE {
	@Shadow
	@Final
	private CommandBlockExecutor commandExecutor;

	@Override
	public CommandBlockExecutor fireblanket$getCommandExecutor() {
		return this.commandExecutor;
	}

	@Override
	public void fireblanket$setOwner(UUID uuid) {
		((CommandBE) commandExecutor).fireblanket$setOwner(uuid);
	}

	@Override
	public void fireblanket$setLastUpdate(UUID uuid) {
		((CommandBE) commandExecutor).fireblanket$setLastUpdate(uuid);
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
