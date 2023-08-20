package net.modfest.fireblanket.mixin.zstd;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.luben.zstd.ZstdOutputStream;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.PersistentState;

@Mixin(PersistentState.class)
public class MixinPersistentState {

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/nbt/NbtIo.writeCompressed(Lnet/minecraft/nbt/NbtCompound;Ljava/io/File;)V"),
			method="save")
	public void fireblanket$writeZstd(NbtCompound nbt, File vanilla) throws IOException {
		String path = vanilla.getPath();
		if (path.endsWith(".dat")) {
			File zstd = new File(path.substring(0, path.length()-4)+".zat");
			try (ZstdOutputStream z = new ZstdOutputStream(new FileOutputStream(zstd))) {
				z.setChecksum(true);
				z.setLevel(4);
				NbtIo.write(nbt, new DataOutputStream(z));
			}
			vanilla.delete();
		} else {
			// oookay, I dunno what you want. have fun.
			NbtIo.writeCompressed(nbt, vanilla);
		}
	}
	
}
