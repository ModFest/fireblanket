package net.modfest.fireblanket.mixin.block;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.CommandBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import net.modfest.fireblanket.net.CommandBlockPacket;
import net.modfest.fireblanket.world.RepeatingBlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandBlock.class)
public abstract class MixinCommandBlock extends BlockWithEntity {

	protected MixinCommandBlock(Settings settings) {
		super(settings);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		if (!world.isClient() && placer instanceof ServerPlayerEntity player) {
			RepeatingBlockState st = ((ServerWorld) world).getServer().getOverworld().getPersistentStateManager().getOrCreate(RepeatingBlockState.getType(), "commandplaced");

			if (st.add(player.getUuid())) {
				st.markDirty();
				ServerPlayNetworking.send(player, CommandBlockPacket.INST);
			}

			if (world.getBlockEntity(pos) instanceof CommandBE cmd) {
				cmd.fireblanket$setOwner(placer.getUuid());
				cmd.fireblanket$setLastUpdate(placer.getUuid());
			}
		}
	}
}
