package net.modfest.fireblanket.world.render_regions;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

import java.util.BitSet;
import java.util.Set;
import java.util.UUID;

public class ExplainedRenderRegion {

	public final String name;
	public final RenderRegion reg;

	public boolean blanketDeny;

	public boolean entityTypeAttachmentsInverted;
	public boolean entityTypeBoxBounded;
	public boolean beTypeAttachmentsInverted;
	public boolean beTypeBoxBounded;

	public final Set<UUID> entityAttachments = new ObjectOpenHashSet<>();
	public final LongSet blockAttachments = new LongOpenHashSet();
	public final Set<Identifier> entityTypeAttachments = new ObjectOpenHashSet<>();
	public final Set<Identifier> beTypeAttachments = new ObjectOpenHashSet<>();

	public ExplainedRenderRegion(String name, RenderRegion reg) {
		this.name = name;
		this.reg = reg;
	}

	public ExplainedRenderRegion copy() {
		ExplainedRenderRegion nw = new ExplainedRenderRegion(name, reg);
		nw.blanketDeny = blanketDeny;
		nw.entityTypeAttachmentsInverted = entityTypeAttachmentsInverted;
		nw.entityTypeBoxBounded = entityTypeBoxBounded;
		nw.beTypeAttachmentsInverted = beTypeAttachmentsInverted;
		nw.beTypeBoxBounded = beTypeBoxBounded;
		nw.entityAttachments.addAll(entityAttachments);
		nw.blockAttachments.addAll(blockAttachments);
		nw.entityTypeAttachments.addAll(entityTypeAttachments);
		nw.beTypeAttachments.addAll(beTypeAttachments);
		return nw;
	}

	public boolean isEntityTypeTargeted(Identifier id) {
		return entityTypeAttachmentsInverted ^ entityTypeAttachments.contains(id);
	}

	public boolean isEntityTypeTargeted(Entity entity) {
		return isEntityTypeTargeted(EntityType.getId(entity.getType()))
			&& (!entityTypeBoxBounded || reg.contains(entity.getPos()));
	}

	public boolean isBlockEntityTypeTargeted(Identifier id) {
		return beTypeAttachmentsInverted ^ beTypeAttachments.contains(id);
	}

	public boolean isBlockEntityTypeTargeted(BlockEntity blockEntity) {
		return isBlockEntityTypeTargeted(BlockEntityType.getId(blockEntity.getType()))
			&& (!beTypeBoxBounded || reg.contains(blockEntity.getPos()));
	}

	public BitSet getMeta() {
		final BitSet set = new BitSet();

		set.set(0, this.entityTypeAttachmentsInverted);
		set.set(1, this.entityTypeBoxBounded);
		set.set(2, this.beTypeAttachmentsInverted);
		set.set(3, this.beTypeBoxBounded);

		return set;
	}

	public void applyMeta(BitSet set) {
		this.entityTypeAttachmentsInverted = set.get(0);
		this.entityTypeBoxBounded = set.get(1);
		this.beTypeAttachmentsInverted = set.get(2);
		this.beTypeBoxBounded = set.get(3);
	}

	void copyMeta(ExplainedRenderRegion other) {
		this.entityTypeAttachmentsInverted = other.entityTypeAttachmentsInverted;
		this.entityTypeBoxBounded = other.entityTypeBoxBounded;
		this.beTypeAttachmentsInverted = other.beTypeAttachmentsInverted;
		this.beTypeBoxBounded = other.beTypeBoxBounded;
	}
}
