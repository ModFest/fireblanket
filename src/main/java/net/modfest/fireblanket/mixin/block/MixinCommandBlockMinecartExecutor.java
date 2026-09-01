package net.modfest.fireblanket.mixin.block;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import net.modfest.fireblanket.util.TextUtil;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author Ampflower
 **/
@Mixin(MinecartCommandBlock.MinecartCommandBase.class)
public class MixinCommandBlockMinecartExecutor extends MixinCommandBlockExecutor {
	@Shadow
	@Final
	MinecartCommandBlock this$0;
	@Unique
	private @Nullable Component fireblanket$lastName;
	@Unique
	private @Nullable Component fireblanket$name;

	@Override
	public Component fireblanket$getNameWithBlame() {
		final Component name = this.this$0.getDisplayName();

		if (!FireblanketConfig.get(ConfigSpecs.TATTLETALE_COMMANDS)) {
			return name;
		}

		if (this.fireblanket$lastName != name || this.fireblanket$name == null) {
			this.fireblanket$lastName = name;
			this.fireblanket$name = name.copy().withStyle(style -> style.withHoverEvent(this.fireblanket$getBlame()));
		}

		return this.fireblanket$name;
	}

	@Override
	public @Nullable Entity fireblanket$getEntity() {
		return this.this$0;
	}

	@Override
	protected void fireblanket$clearCache() {
		super.fireblanket$clearCache();
		this.fireblanket$name = null;
		this.fireblanket$lastName = null;
	}

	@Override
	protected HoverEvent fireblanket$generateBlame() {
		if (!(this$0.level() instanceof ServerLevel serverLevel)) {
			throw new IllegalStateException("Blame generated client side");
		}

		return TextUtil.toBlameHover(this, serverLevel, this$0.blockPosition(), this$0.getType().getDescription());
	}
}
