package net.modfest.fireblanket.mixin.zstd;

import com.mojang.datafixers.DataFixer;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.modfest.fireblanket.mixinsupport.ZestyPlayerSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public class MixinLevelStorageSession {

	@Redirect(at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lcom/mojang/datafixers/DataFixer;)Lnet/minecraft/world/level/storage/PlayerDataStorage;"),
		method = "createPlayerStorage")
	public PlayerDataStorage fireblanket$useZstd(LevelStorageSource.LevelStorageAccess session, DataFixer dataFixer) {
		return new ZestyPlayerSaveHandler(session, dataFixer);
	}
}
