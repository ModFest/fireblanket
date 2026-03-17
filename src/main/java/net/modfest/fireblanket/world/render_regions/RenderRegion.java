package net.modfest.fireblanket.world.render_regions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.AbstractLongIterator;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.NoSuchElementException;

public record RenderRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Mode mode) {
	// Save data: need to flatten the map down for compatibility.
	public static final MapCodec<RenderRegion> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			Codec.INT.listOf(6, 6)
				.fieldOf("Box")
				.forGetter(RenderRegion::toArrayBox),
			StringRepresentable.fromEnum(RenderRegion.Mode::values)
				.orElse(Mode.UNKNOWN)
				.fieldOf("Mode")
				.forGetter(RenderRegion::mode)
		).apply(instance, RenderRegion::new)
	);

	public RenderRegion {
		int x1 = minX;
		int y1 = minY;
		int z1 = minZ;
		int x2 = maxX;
		int y2 = maxY;
		int z2 = maxZ;
		minX = Math.min(x1, x2);
		minY = Math.min(y1, y2);
		minZ = Math.min(z1, z2);
		maxX = Math.max(x1, x2);
		maxY = Math.max(y1, y2);
		maxZ = Math.max(z1, z2);
	}

	private RenderRegion(int[] box, Mode mode) {
		this(box[0], box[1], box[2], box[3], box[4], box[5], mode);
	}

	// To be called by trusted code only.
	private RenderRegion(List<Integer> box, Mode mode) {
		this(box.get(0), box.get(1), box.get(2), box.get(3), box.get(4), box.get(5), mode);
	}

	public enum Mode implements StringRepresentable {
		UNKNOWN, ALLOW, DENY, EXCLUSIVE,
		;
		public static final ImmutableList<Mode> VALUES = ImmutableList.copyOf(values());

		@Override
		public String getSerializedName() {
			return this.name();
		}
	}

	public Iterable<SectionPos> affectedChunks() {
		LongIterable li = affectedChunkLongs();
		return () -> new AbstractObjectIterator<SectionPos>() {
			private final LongIterator iter = li.iterator();

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public SectionPos next() {
				return SectionPos.of(iter.nextLong());
			}

		};
	}

	public LongIterable affectedChunkLongs() {
		int minX = this.minX >> 4;
		int minY = this.minY >> 4;
		int minZ = this.minZ >> 4;
		int maxX = (this.maxX + 15) >> 4;
		int maxY = (this.maxY + 15) >> 4;
		int maxZ = (this.maxZ + 15) >> 4;

		int width = (maxX - minX) + 1;
		int height = (maxY - minY) + 1;
		int depth = (maxZ - minZ) + 1;
		int length = width * height * depth;
		return () -> new AbstractLongIterator() {
			private int index;

			@Override
			public long nextLong() {
				if (index == length) {
					throw new NoSuchElementException();
				} else {
					int x = index % width;
					int next = index / width;
					int y = next % height;
					int z = next / height;
					index++;
					return SectionPos.asLong(minX + x, minY + y, minZ + z);
				}
			}

			@Override
			public boolean hasNext() {
				return index < length;
			}
		};
	}

	public boolean contains(BlockPos bp) {
		return contains(bp.getX(), bp.getY(), bp.getZ());
	}

	public boolean contains(Vec3 v3d) {
		return contains(v3d.x(), v3d.y(), v3d.z());
	}

	public boolean contains(int x, int y, int z) {
		return x >= minX && x <= maxX
			&& y >= minY && y <= maxY
			&& z >= minZ && z <= maxZ;
	}

	public boolean contains(double x, double y, double z) {
		return x >= minX && x < maxX + 1
			&& y >= minY && y < maxY + 1
			&& z >= minZ && z < maxZ + 1;
	}

	public AABB toBox() {
		return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
	}

	private IntList toArrayBox() {
		return IntList.of(minX, minY, minZ, maxX, maxY, maxZ);
	}

	// reference semantics for performance
	// technically breaks the record contract. oh well

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

}
