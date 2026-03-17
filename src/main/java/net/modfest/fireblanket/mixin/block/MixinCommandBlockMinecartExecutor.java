package net.modfest.fireblanket.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author Ampflower
 **/
@Mixin(MinecartCommandBlock.MinecartCommandBase.class)
public class MixinCommandBlockMinecartExecutor extends MixinCommandBlockExecutor {
	@Unique
	private Component fireblanket$lastName;
	@Unique
	private Component fireblanket$name;

	@WrapOperation(
		method = "createCommandSourceStack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/vehicle/MinecartCommandBlock;getDisplayName()Lnet/minecraft/network/chat/Component;"
		)
	)
	private Component fireblanket$augmentSourceName(final MinecartCommandBlock self, final Operation<Component> operation) {
		if (!FireblanketConfig.get(ConfigSpecs.TATTLETALE_COMMANDS)) {
			return operation.call(self);
		}

		final Component name = self.getName();

		if (this.fireblanket$lastName != name) {
			this.fireblanket$lastName = name;
			this.fireblanket$name = name.copy().withStyle(style -> style.withHoverEvent(this.fireblanket$getBlame()));
		}

		return this.fireblanket$name;
	}

	@Override
	protected void fireblanket$clearCache() {
		super.fireblanket$clearCache();
		this.fireblanket$name = null;
		this.fireblanket$lastName = null;
	}
}
