package net.modfest.fireblanket.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import net.modfest.fireblanket.util.TextUtil;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * @author Ampflower
 **/
@Mixin(BaseCommandBlock.class)
public class MixinCommandBlockExecutor implements CommandBE {
	@Unique
	private UUID fireblanket$owner;
	@Unique
	private UUID fireblanket$lastUpdated;
	@Unique
	private HoverEvent fireblanket$blame;
	@Unique
	private Component fireblanket$lastName;
	@Unique
	private Component fireblanket$name;

	/**
	 * @return self as {@link BaseCommandBlock}
	 */
	@Override
	public BaseCommandBlock fireblanket$getCommandExecutor() {
		//noinspection ConstantConditions
		return (BaseCommandBlock) (Object) this;
	}

	@Inject(method = "save", at = @At("TAIL"))
	private void fireblanket$writeNbt(ValueOutput nbt, CallbackInfo ci) {
		if (fireblanket$owner != null) {
			nbt.store("FB:Owner", UUIDUtil.CODEC, fireblanket$owner);
		}

		if (fireblanket$lastUpdated != null) {
			nbt.store("FB:LastUpdated", UUIDUtil.CODEC, fireblanket$lastUpdated);
		}
	}

	@Inject(method = "load", at = @At("TAIL"))
	private void fireblanket$readNbt(ValueInput nbt, CallbackInfo ci) {
		fireblanket$owner = nbt.read("FB:Owner", UUIDUtil.CODEC).orElse(null);
		fireblanket$lastUpdated = nbt.read("FB:LastUpdated", UUIDUtil.CODEC).orElse(null);
	}

	@ModifyReturnValue(method = "getName", at = @At("RETURN"))
	private Component fireblanket$augmentName(final Component name) {
		if (!FireblanketConfig.get(ConfigSpecs.TATTLETALE_COMMANDS)) {
			return name;
		}

		if (this.fireblanket$lastName != name) {
			this.fireblanket$lastName = name;
			this.fireblanket$name = name.copy().withStyle(style -> style.withHoverEvent(this.fireblanket$getBlame()));
		}

		return this.fireblanket$name;
	}

	@Override
	public final void fireblanket$setOwner(final UUID uuid) {
		this.fireblanket$owner = uuid;
		this.fireblanket$clearCache();
	}

	@Override
	public final void fireblanket$setLastUpdate(final UUID uuid) {
		this.fireblanket$lastUpdated = uuid;
		this.fireblanket$clearCache();
	}

	@Override
	public final UUID fireblanket$getOwner() {
		return this.fireblanket$owner;
	}

	@Override
	public final UUID fireblanket$getLastUpdate() {
		return this.fireblanket$lastUpdated;
	}

	@Unique
	@MustBeInvokedByOverriders
	protected void fireblanket$clearCache() {
		this.fireblanket$blame = null;
		this.fireblanket$lastName = null;
		this.fireblanket$name = null;
	}

	@Override
	public final HoverEvent fireblanket$getBlame() {
		if (this.fireblanket$blame != null) {
			return this.fireblanket$blame;
		}

		return this.fireblanket$blame = TextUtil.toBlameHover((BaseCommandBlock) (Object) this);
	}
}
