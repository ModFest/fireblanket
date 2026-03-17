package net.modfest.fireblanket.mixin.block;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import net.modfest.fireblanket.net.CommandBlockPacket;
import net.modfest.fireblanket.world.RepeatingBlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandBlock.class)
public abstract class MixinCommandBlock extends BaseEntityBlock {

	protected MixinCommandBlock(Properties settings) {
		super(settings);
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		if (!world.isClientSide() && placer instanceof ServerPlayer player) {
			RepeatingBlockState st = ((ServerLevel) world).getServer().overworld().getDataStorage().computeIfAbsent(RepeatingBlockState.getType());

			if (st.add(player.getUUID())) {
				st.setDirty();
				ServerPlayNetworking.send(player, CommandBlockPacket.INST);
			}

			if (world.getBlockEntity(pos) instanceof CommandBE cmd) {
				cmd.fireblanket$setOwner(placer.getUUID());
				cmd.fireblanket$setLastUpdate(placer.getUUID());
			}
		}
	}
}
