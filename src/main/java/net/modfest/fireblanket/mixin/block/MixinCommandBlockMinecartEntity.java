package net.modfest.fireblanket.mixin.block;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.jspecify.annotations.Nullable;
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
	public void fireblanket$setOwner(@Nullable UUID uuid) {
		((CommandBE) commandBlock).fireblanket$setOwner(uuid);
	}

	@Override
	public void fireblanket$setLastUpdate(@Nullable UUID uuid) {
		((CommandBE) commandBlock).fireblanket$setLastUpdate(uuid);
	}

	@Override
	public @Nullable UUID fireblanket$getOwner() {
		return ((CommandBE) commandBlock).fireblanket$getOwner();
	}

	@Override
	public @Nullable UUID fireblanket$getLastUpdate() {
		return ((CommandBE) commandBlock).fireblanket$getLastUpdate();
	}

	@Override
	public Component fireblanket$getNameWithBlame() {
		return ((CommandBE) commandBlock).fireblanket$getNameWithBlame();
	}

	@Override
	public HoverEvent fireblanket$getBlame() {
		return ((CommandBE) commandBlock).fireblanket$getBlame();
	}
}
