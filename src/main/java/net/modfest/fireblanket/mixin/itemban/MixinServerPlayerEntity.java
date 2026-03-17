package net.modfest.fireblanket.mixin.itemban;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.modfest.fireblanket.world.ItemBan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerEntity extends Player {

	public MixinServerPlayerEntity(Level world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void fireblanket$playerTick(CallbackInfo ci) {
		Inventory inventory = this.getInventory();
		int size = inventory.getContainerSize();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getItem(i);
			String string = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
			if (ItemBan.BANNED_IDS.contains(string)) {
				inventory.setItem(i, ItemStack.EMPTY);
			}
		}
	}
}
