package net.modfest.fireblanket.mixin.block;

import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.modfest.fireblanket.util.TextUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Yet another case of Mojang opting to use anonymous classes.
 *
 * @author Ampflower
 */
@Mixin(targets = "net.minecraft.world.level.block.entity.CommandBlockEntity$1")
public class MixinCommandBlockEntityExecutor extends MixinCommandBlockExecutor {
	@Shadow
	@Final
	CommandBlockEntity this$0;

	@Override
	protected HoverEvent fireblanket$generateBlame() {
		if (!(this$0.getLevel() instanceof ServerLevel serverLevel)) {
			throw new IllegalStateException("Blame generated client side");
		}

		return TextUtil.toBlameHover(this, serverLevel, this$0.getBlockPos(), TextUtil.getBlockEntityName(this$0));
	}
}
