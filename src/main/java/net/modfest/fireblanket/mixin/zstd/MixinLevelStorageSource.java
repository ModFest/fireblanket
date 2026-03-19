package net.modfest.fireblanket.mixin.zstd;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.modfest.fireblanket.mixinsupport.ZestySupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Ampflower
 */
@Mixin(LevelStorageSource.class)
public class MixinLevelStorageSource {
	@WrapOperation(
		method = "readExistingSavedData",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/nbt/NbtIo;readCompressed(Ljava/nio/file/Path;Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;"
		)
	)
	private static CompoundTag readZestyTag(Path path, NbtAccounter accounter, Operation<CompoundTag> original) throws IOException {
		final Path zstd = ZestySupport.zestifyIfVanilla(path);
		if (zstd != null && Files.isRegularFile(zstd)) {
			try (final DataInputStream input = new DataInputStream(ZestySupport.inputStream(zstd))) {
				return NbtIo.read(input, accounter);
			}
		}
		return original.call(path, accounter);
	}

}
