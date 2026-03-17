package net.modfest.fireblanket.mixin.block;

import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

/**
 * @author Ampflower
 */
@Mixin(MinecartCommandBlock.class)
public class MixinCommandBlockMinecartEntity implements CommandBE {
	@Shadow
	@Final
	private BaseCommandBlock commandBlock;

	@Override
	public BaseCommandBlock fireblanket$getCommandExecutor() {
		return this.commandBlock;
	}

	@Override
	public void fireblanket$setOwner(UUID uuid) {
		((CommandBE) commandBlock).fireblanket$setOwner(uuid);
	}

	@Override
	public void fireblanket$setLastUpdate(UUID uuid) {
		((CommandBE) commandBlock).fireblanket$setLastUpdate(uuid);
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
