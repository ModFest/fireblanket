package net.modfest.fireblanket.mixin.adventure_fix;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * @author Ampflower
 * @since 0.10.1
 **/
@Mixin(AnvilMenu.class)
public class MixinAnvilMenu {

	@ModifyExpressionValue(
		method = "*",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"),
		slice = {
			@Slice(
				to = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/AnvilBlock;damage(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"
				)
			)
		},
		require = 1,
		allow = 1
	)
	private static boolean infiniteOrAdventure(
		final boolean infiniteMaterials,
		final @Local(argsOnly = true) Player player
	) {
		return infiniteMaterials || !player.getAbilities().mayBuild;
	}
}
