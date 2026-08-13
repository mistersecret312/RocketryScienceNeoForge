package net.mistersecret312.rocketry_science.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.orbits.ArtificialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.TransferOrbit;
import org.joml.Matrix4f;
import org.joml.Vector2d;

public class OrbitRenderer
{
	public static void drawOrbitCircle(GuiGraphics graphics, float x, float y, float radius,
									   float thickness, double zoom, int color)
	{
		ShaderInstance shader = RocketryScience.ClientModEvents.orbit;
		if(shader == null) return;

		RenderSystem.setShader(() -> shader);

		float a = (color >> 24 & 255) / 255.0F;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;

		float normalizedOuter = 0.5f;

		float normalizedInner = 0.5f - (thickness / (2.0f * radius));
		if(normalizedInner < 0.0f) normalizedInner = 0.0f;

		shader.safeGetUniform("Color").set(r, g, b, a);
		shader.safeGetUniform("InnerRadius").set(normalizedInner);
		shader.safeGetUniform("OuterRadius").set(normalizedOuter);
		shader.safeGetUniform("QuadSize").set((float) (radius * 2.0f));
		shader.safeGetUniform("SquishFactor").set(1.0f);
		shader.safeGetUniform("DashSegments").set(0.0f);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		Matrix4f matrix = graphics.pose().last().pose();
		Tesselator tesselator = Tesselator.getInstance();

		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

		buffer.addVertex(matrix, x - radius, y - radius, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 0);
		buffer.addVertex(matrix, x - radius, y + radius, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 1);
		buffer.addVertex(matrix, x + radius, y + radius, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 1);
		buffer.addVertex(matrix, x + radius, y - radius, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 0);

		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.disableBlend();
	}

	public static void drawOrbitTransfer(GuiGraphics graphics, float x, float y, double departureAltitude,
										 double arrivalAltitude, double departureAngle, double zoom, double progress, int color)
	{
		float r1 = (float) (departureAltitude / 5.0);
		float r2 = (float) (arrivalAltitude / 5.0);

		float maxR = Math.max(r1, r2);

		ShaderInstance shader = RocketryScience.ClientModEvents.transferOrbit;
		if(shader == null) return;

		RenderSystem.setShader(() -> shader);

		float a = (color >> 24 & 255) / 255.0F;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;

		float uvScale = 1.0f / (maxR * 2.0f);

		shader.safeGetUniform("Color").set(r, g, b, a);
		shader.safeGetUniform("R1").set(r1 * uvScale);
		shader.safeGetUniform("R2").set(r2 * uvScale);
		shader.safeGetUniform("Rotation").set((float) departureAngle);

		shader.safeGetUniform("Thickness").set(0.25f * uvScale);

		shader.safeGetUniform("QuadSize").set((float) (maxR * 2.0f));

		shader.safeGetUniform("SquishFactor").set(1.0f);

		shader.safeGetUniform("DashSegments").set(0.0f);
		shader.safeGetUniform("Progress").set((float) progress);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		Matrix4f matrix = graphics.pose().last().pose();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

		buffer.addVertex(matrix, x - maxR, y - maxR, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 0);
		buffer.addVertex(matrix, x - maxR, y + maxR, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 1);
		buffer.addVertex(matrix, x + maxR, y + maxR, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 1);
		buffer.addVertex(matrix, x + maxR, y - maxR, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 0);

		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.disableBlend();
	}
}
