package net.modfest.fireblanket.mixin.itemban;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.modfest.fireblanket.world.ItemBan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {
	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void fireblanket$playerTick(CallbackInfo ci) {
		PlayerInventory inventory = this.getInventory();
		int size = inventory.size();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getStack(i);
			String string = Registries.ITEM.getId(stack.getItem()).toString();
			if (ItemBan.BANNED_IDS.contains(string)) {
				inventory.setStack(i, ItemStack.EMPTY);
			}
		}
	}
}
