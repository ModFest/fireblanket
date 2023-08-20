package net.modfest.fireblanket.mixin.zstd;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.datafixers.DataFixer;

import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.storage.LevelStorage;
import net.modfest.fireblanket.mixinsupport.ZestyWorldSaveHandler;

@Mixin(LevelStorage.Session.class)
public class MixinLevelStorageSession {

	@Redirect(at=@At(value="NEW", target="net/minecraft/world/WorldSaveHandler"),
			method="createSaveHandler")
	public WorldSaveHandler fireblanket$useZstd(LevelStorage.Session session, DataFixer dataFixer) {
		return new ZestyWorldSaveHandler(session, dataFixer);
	}
	
}
