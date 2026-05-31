package net.mistersecret312.rocketry_science.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.mistersecret312.rocketry_science.RocketryScience;
import org.joml.Matrix4f;
import org.joml.Vector2d;

public class OrbitRenderer
{
	public static void drawOrbitCircle(GuiGraphics graphics, float x, float y, float radius, float thickness, int color)
	{
		ShaderInstance shader = RocketryScience.ClientModEvents.orbit;
		if (shader == null)
			return;

		RenderSystem.setShader(() -> shader);

		float a = (color >> 24 & 255) / 255.0F;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;

		float normalizedOuter = 0.5f;

		float normalizedInner = 0.5f - (thickness / (2.0f * radius));
		if (normalizedInner < 0.0f) normalizedInner = 0.0f;

		shader.safeGetUniform("Color").set(r, g, b, a);
		shader.safeGetUniform("InnerRadius").set(normalizedInner);
		shader.safeGetUniform("OuterRadius").set(normalizedOuter);
		shader.safeGetUniform("QuadSize").set(radius * 2.0f);

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

	public static void drawOrbitTransfer(GuiGraphics graphics, Vector2d start, Vector2d end, int color) {
		Matrix4f matrix = graphics.pose().last().pose();

		float a = (color >> 24 & 255) / 255.0F;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

		bufferBuilder.addVertex(matrix, (float) start.x, (float) start.y, 0).setColor(r, g, b, a);
		bufferBuilder.addVertex(matrix, (float) end.x, (float) end.y, 0).setColor(r, g, b, a);

		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
		RenderSystem.disableBlend();
	}
}
