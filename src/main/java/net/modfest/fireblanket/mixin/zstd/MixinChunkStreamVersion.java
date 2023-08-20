package net.modfest.fireblanket.mixin.zstd;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import net.minecraft.world.storage.ChunkStreamVersion;
import net.modfest.fireblanket.mixinsupport.ChunkStreamVersionExt;

@Mixin(ChunkStreamVersion.class)
public class MixinChunkStreamVersion {

	@Shadow
	private static ChunkStreamVersion add(ChunkStreamVersion version) { throw new AbstractMethodError(); }
	
	@Inject(at=@At("TAIL"), method="<clinit>")
	private static void fireblanket$addZstd(CallbackInfo ci) {
		add(ChunkStreamVersionExt.ZSTD = new ChunkStreamVersion(53, // chosen by fair dice roll. guaranteed to be random.
				in -> new FastBufferedInputStream(new ZstdInputStream(in)),
				out -> {
					var z = new ZstdOutputStream(out);
					z.setLevel(7);
					z.setLong(18);
					z.setChecksum(true);
					return new FastBufferedOutputStream(z);
				}));
	}
	
}