package net.modfest.fireblanket.mixin.zstd;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.storage.ChunkStreamVersion;
import net.minecraft.world.storage.RegionFile;
import net.modfest.fireblanket.mixinsupport.ChunkStreamVersionExt;

@Mixin(RegionFile.class)
public class MixinRegionFile {

	@Redirect(at=@At(value="FIELD", target="net/minecraft/world/storage/ChunkStreamVersion.DEFLATE:Lnet/minecraft/world/storage/ChunkStreamVersion;"),
			method="<init>(Ljava/nio/file/Path;Ljava/nio/file/Path;Z)V")
	private static ChunkStreamVersion fireblanket$useZstd() {
		ChunkStreamVersion.exists(0); // initialize class
		return ChunkStreamVersionExt.ZSTD;
	}
	
}
