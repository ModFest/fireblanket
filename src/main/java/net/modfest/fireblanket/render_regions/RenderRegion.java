package net.modfest.fireblanket.render_regions;

import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.longs.AbstractLongIterator;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;

public record RenderRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Mode mode) {
	
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

	public enum Mode {
		UNKNOWN, ALLOW, DENY,
		;
		public static final ImmutableList<Mode> VALUES = ImmutableList.copyOf(values());
	}
	
	public Iterable<ChunkSectionPos> affectedChunks() {
		LongIterable li = affectedChunkLongs();
		return () -> new AbstractObjectIterator<ChunkSectionPos>() {
			private final LongIterator iter = li.iterator();
			
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public ChunkSectionPos next() {
				return ChunkSectionPos.from(iter.nextLong());
			}
			
		};
	}
	
	public LongIterable affectedChunkLongs() {
		int minX = this.minX>>4;
		int minY = this.minY>>4;
		int minZ = this.minZ>>4;
		int maxX = (this.maxX+15)>>4;
		int maxY = (this.maxY+15)>>4;
		int maxZ = (this.maxZ+15)>>4;
		
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
					return ChunkSectionPos.asLong(minX + x, minY + y, minZ + z);
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
	
	public boolean contains(int x, int y, int z) {
		return x >= minX && x <= maxX
				&& y >= minY && y <= maxY
				&& z >= minZ && z <= maxZ;
	}
	
	public boolean contains(double x, double y, double z) {
		return x >= minX && x < maxX+1
				&& y >= minY && y < maxY+1
				&& z >= minZ && z < maxZ+1;
	}

	public Box toBox() {
		return new Box(minX, minY, minZ, maxX+1, maxY+1, maxZ+1);
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
