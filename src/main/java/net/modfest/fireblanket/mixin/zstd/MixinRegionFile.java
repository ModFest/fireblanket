package net.modfest.fireblanket.mixin.zstd;

import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.modfest.fireblanket.mixinsupport.ChunkCompressionFormatExt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(RegionFile.class)
public class MixinRegionFile {

	@Mutable
	@Shadow
	@Final
	private RegionFileVersion version;

	@Inject(at = @At("TAIL"),
		method = "<init>(Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Ljava/nio/file/Path;Ljava/nio/file/Path;Lnet/minecraft/world/level/chunk/storage/RegionFileVersion;Z)V")
	private void fireblanket$useZstd(RegionStorageInfo storageKey, Path path, Path directory, RegionFileVersion compressionFormat, boolean dsync, CallbackInfo ci) {
		RegionFileVersion.isValidVersion(0); // initialize class
		this.version = ChunkCompressionFormatExt.ZSTD;
	}
}
