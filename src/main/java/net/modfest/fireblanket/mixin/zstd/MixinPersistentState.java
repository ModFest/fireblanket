package net.modfest.fireblanket.mixin.zstd;

import com.github.luben.zstd.ZstdOutputStream;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.PersistentState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(PersistentState.class)
public class MixinPersistentState {
//	@WrapOperation(at = @At(value = "INVOKE", target = "net/minecraft/nbt/NbtIo.writeCompressed(Lnet/minecraft/nbt/NbtCompound;Ljava/nio/file/Path;)V"), method = "save")
//	public void fireblanket$writeZstd(NbtCompound nbt, Path vanilla, Operation<Void> original) throws IOException {
//		String path = vanilla.toAbsolutePath().toString();
//		if (path.endsWith(".dat")) {
//			File zstd = new File(path.substring(0, path.length() - 4) + ".zat");
//			try (ZstdOutputStream z = new ZstdOutputStream(new FileOutputStream(zstd))) {
//				z.setChecksum(true);
//				z.setLevel(4);
//				NbtIo.write(nbt, new DataOutputStream(z));
//			}
//
//			Files.deleteIfExists(vanilla);
//		} else {
//			// oookay, I dunno what you want. have fun.
//			original.call(nbt, vanilla);
//		}
//	}
}
