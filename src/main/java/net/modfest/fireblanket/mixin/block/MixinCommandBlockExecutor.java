package net.modfest.fireblanket.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.world.CommandBlockExecutor;
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
@Mixin(CommandBlockExecutor.class)
public class MixinCommandBlockExecutor implements CommandBE {
	@Unique
	private UUID fireblanket$owner;
	@Unique
	private UUID fireblanket$lastUpdated;
	@Unique
	private HoverEvent fireblanket$blame;
	@Unique
	private Text fireblanket$lastName;
	@Unique
	private Text fireblanket$name;

	/**
	 * @return self as {@link CommandBlockExecutor}
	 */
	@Override
	public CommandBlockExecutor fireblanket$getCommandExecutor() {
		//noinspection ConstantConditions
		return (CommandBlockExecutor) (Object) this;
	}

	@Inject(method = "writeData", at = @At("TAIL"))
	private void fireblanket$writeNbt(WriteView nbt, CallbackInfo ci) {
		if (fireblanket$owner != null) {
			nbt.put("FB:Owner", Uuids.INT_STREAM_CODEC, fireblanket$owner);
		}

		if (fireblanket$lastUpdated != null) {
			nbt.put("FB:LastUpdated", Uuids.INT_STREAM_CODEC, fireblanket$lastUpdated);
		}
	}

	@Inject(method = "readData", at = @At("TAIL"))
	private void fireblanket$readNbt(ReadView nbt, CallbackInfo ci) {
		fireblanket$owner = nbt.read("FB:Owner", Uuids.INT_STREAM_CODEC).orElse(null);
		fireblanket$lastUpdated = nbt.read("FB:LastUpdated", Uuids.INT_STREAM_CODEC).orElse(null);
	}

	@ModifyReturnValue(method = "getName", at = @At("RETURN"))
	private Text fireblanket$augmentName(final Text name) {
		if (!FireblanketConfig.get(ConfigSpecs.TATTLETALE_COMMANDS)) {
			return name;
		}

		if (this.fireblanket$lastName != name) {
			this.fireblanket$lastName = name;
			this.fireblanket$name = name.copy().styled(style -> style.withHoverEvent(this.fireblanket$getBlame()));
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

	@Unique
	protected final HoverEvent fireblanket$getBlame() {
		if (this.fireblanket$blame != null) {
			return this.fireblanket$blame;
		}

		return this.fireblanket$blame = TextUtil.toBlameHover((CommandBlockExecutor) (Object) this);
	}
}
