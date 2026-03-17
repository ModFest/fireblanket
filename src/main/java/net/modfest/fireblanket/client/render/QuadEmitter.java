package net.modfest.fireblanket.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;

public class QuadEmitter {

	public static void buildBox(VertexConsumer buffer, PoseStack matrices, float x1, float x2, float y1, float y2, float z1, float z2, int r, int g, int b, int a) {
		PoseStack.Pose entry = matrices.last();
		buildTopFacing(buffer, entry, x1, x2, z1, z2, y1, r, g, b, a);
		buildTopFacing(buffer, entry, x1, x2, z1, z2, y2, r, g, b, a);

		buildNorthFacing(buffer, entry, x1, x2, y1, y2, z1, r, g, b, a);
		buildNorthFacing(buffer, entry, x1, x2, y1, y2, z2, r, g, b, a);

		buildEastFacing(buffer, entry, y1, y2, z1, z2, x1, r, g, b, a);
		buildEastFacing(buffer, entry, y1, y2, z1, z2, x2, r, g, b, a);

		// TODO: proper normals
	}

	public static void buildTopFacing(VertexConsumer buffer, PoseStack.Pose entry, float x1, float x2, float z1, float z2, float y, int r, int g, int b, int a) {
		Matrix4f model = entry.pose();
		// -X, +Z
		buffer.addVertex(model, x1, y, z2).setColor(r, g, b, a)
			.setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		// +X, +Z
		buffer.addVertex(model, x2, y, z2).setColor(r, g, b, a)
			.setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		// +X, -Z
		buffer.addVertex(model, x2, y, z1).setColor(r, g, b, a)
			.setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		// -X, -Z
		buffer.addVertex(model, x1, y, z1).setColor(r, g, b, a)
			.setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);

		// Reverse

		buffer.addVertex(model, x1, y, z2).setColor(r, g, b, a)
			.setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		buffer.addVertex(model, x1, y, z1).setColor(r, g, b, a)
			.setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		buffer.addVertex(model, x2, y, z1).setColor(r, g, b, a)
			.setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		buffer.addVertex(model, x2, y, z2).setColor(r, g, b, a)
			.setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
	}

	public static void buildNorthFacing(VertexConsumer buffer, PoseStack.Pose entry, float x1, float x2, float y1, float y2, float z, int r, int g, int b, int a) {
		Matrix4f model = entry.pose();

		// -X, +Z
		buffer.addVertex(model, x1, y2, z).setColor(r, g, b, a)
			.setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		// +X, +Z
		buffer.addVertex(model, x2, y2, z).setColor(r, g, b, a)
			.setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		// +X, -Z
		buffer.addVertex(model, x2, y1, z).setColor(r, g, b, a)
			.setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		// -X, -Z
		buffer.addVertex(model, x1, y1, z).setColor(r, g, b, a)
			.setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);

		// Reverse

		buffer.addVertex(model, x1, y2, z).setColor(r, g, b, a)
			.setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		buffer.addVertex(model, x1, y1, z).setColor(r, g, b, a)
			.setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		buffer.addVertex(model, x2, y1, z).setColor(r, g, b, a)
			.setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		buffer.addVertex(model, x2, y2, z).setColor(r, g, b, a)
			.setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
	}

	public static void buildEastFacing(VertexConsumer buffer, PoseStack.Pose entry, float y1, float y2, float z1, float z2, float x, int r, int g, int b, int a) {
		Matrix4f model = entry.pose();

		// -X, +Z
		buffer.addVertex(model, x, y1, z2).setColor(r, g, b, a)
			.setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		// +X, +Z
		buffer.addVertex(model, x, y2, z2).setColor(r, g, b, a)
			.setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		// +X, -Z
		buffer.addVertex(model, x, y2, z1).setColor(r, g, b, a)
			.setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		// -X, -Z
		buffer.addVertex(model, x, y1, z1).setColor(r, g, b, a)
			.setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);

		// Reverse

		buffer.addVertex(model, x, y1, z2).setColor(r, g, b, a)
			.setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		buffer.addVertex(model, x, y1, z1).setColor(r, g, b, a)
			.setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		buffer.addVertex(model, x, y2, z1).setColor(r, g, b, a)
			.setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
		buffer.addVertex(model, x, y2, z2).setColor(r, g, b, a)
			.setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, 0.0F, 1.0F, 0.0F);
	}
}