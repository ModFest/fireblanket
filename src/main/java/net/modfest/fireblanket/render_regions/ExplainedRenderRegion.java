package net.modfest.fireblanket.render_regions;

import java.util.Set;
import java.util.UUID;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.Identifier;

public class ExplainedRenderRegion {

	public final String name;
	public final RenderRegion reg;

	public boolean blanketDeny;
	
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
		nw.entityAttachments.addAll(entityAttachments);
		nw.blockAttachments.addAll(blockAttachments);
		nw.entityTypeAttachments.addAll(entityTypeAttachments);
		nw.beTypeAttachments.addAll(beTypeAttachments);
		return nw;
	}
	
}
