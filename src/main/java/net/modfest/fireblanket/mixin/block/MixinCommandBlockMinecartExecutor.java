package net.modfest.fireblanket.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.text.Text;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author Ampflower
 **/
@Mixin(CommandBlockMinecartEntity.CommandExecutor.class)
public class MixinCommandBlockMinecartExecutor extends MixinCommandBlockExecutor {
	@Unique
	private Text fireblanket$lastName;
	@Unique
	private Text fireblanket$name;

	@WrapOperation(
		method = "getSource",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/vehicle/CommandBlockMinecartEntity;getDisplayName()Lnet/minecraft/text/Text;"
		)
	)
	private Text fireblanket$augmentSourceName(final CommandBlockMinecartEntity self, final Operation<Text> operation) {
		if (!FireblanketConfig.get(ConfigSpecs.TATTLETALE_COMMANDS)) {
			return operation.call(self);
		}

		final Text name = self.getName();

		if (this.fireblanket$lastName != name) {
			this.fireblanket$lastName = name;
			this.fireblanket$name = name.copy().styled(style -> style.withHoverEvent(this.fireblanket$getBlame()));
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
