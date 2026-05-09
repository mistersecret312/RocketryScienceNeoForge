package net.mistersecret312.rocketry_science.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.menus.CombustionChamberMenu;

public class CombustionChamberScreen extends AbstractContainerScreen<CombustionChamberMenu>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/gui/steel_combustion_engine_gui.png");

    public CombustionChamberScreen(CombustionChamberMenu pMenu, Inventory pPlayerInventory, Component pTitle)
    {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 237;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY)
    {
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null)
            return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY)
    {

    }
}
