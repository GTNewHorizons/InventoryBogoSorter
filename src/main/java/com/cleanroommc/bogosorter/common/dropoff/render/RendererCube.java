package com.cleanroommc.bogosorter.common.dropoff.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;

public class RendererCube {

    public static final RendererCube INSTANCE = new RendererCube();
    private List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
    private long currentTime;

    public enum RenderStyle {
        CUBE, // Full wireframe box
        BRACE, // Corner brackets
        FILLED, // Solid translucent box
        SMALL_CUBE, // Floating inner wireframe
        FILLED_CORE // Floating inner solid translucent box
    }

    public void draw(List<RendererCubeTarget> rendererCubeTargets) {
        this.rendererCubeTargets = rendererCubeTargets;
        this.currentTime = System.currentTimeMillis();
    }

    /**
     * This method is called by the RenderWorldLastEvent handler.
     */
    public void tryToRender(RenderWorldLastEvent event) {
        long timeAlive = System.currentTimeMillis() - currentTime;
        long totalLife = 3000;
        long fadeDuration = 1000;
        long solidDuration = totalLife - fadeDuration;

        // Lifecycle Check
        if (rendererCubeTargets.isEmpty() || timeAlive >= totalLife) {
            return;
        }

        // Alpha Calculation
        int alphaByte = getAlphaByte(timeAlive, solidDuration, fadeDuration);

        // --- Render Setup ---
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-playerX, -playerY, -playerZ);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST); // Show through walls

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Render Style Logic
        RenderStyle style = BogoSorterConfig.dropOff.dropoffRenderStyle;
        boolean isFilledStyle = (style == RenderStyle.FILLED || style == RenderStyle.FILLED_CORE);

        if (isFilledStyle) {
            GL11.glBegin(GL11.GL_QUADS);
        } else {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(1.5f);
            GL11.glBegin(GL11.GL_LINES);
        }

        for (RendererCubeTarget target : rendererCubeTargets) {
            Color c = target.getColor();
            int a = alphaByte;

            // Reduce opacity for filled shapes so they aren't too dense
            if (isFilledStyle) {
                a = (int) (alphaByte * 0.35f);
            }

            GL11.glColor4ub((byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue(), (byte) a);

            switch (style) {
                case CUBE -> renderFullCube(target.getBlockPos());
                case BRACE -> renderCorners(target.getBlockPos());
                case FILLED -> renderFilledCube(target.getBlockPos());
                case SMALL_CUBE -> renderSmallCube(target.getBlockPos());
                case FILLED_CORE -> renderFilledCore(target.getBlockPos());
            }
        }

        GL11.glEnd();

        if (!isFilledStyle) {
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private static int getAlphaByte(long timeAlive, long solidDuration, long fadeDuration) {
        float alpha = 1.0f;

        if (BogoSorterConfig.dropOff.dropoffRenderFadeOut) {
            if (timeAlive > solidDuration) {
                long timeIntoFade = timeAlive - solidDuration;
                alpha = 1.0f - ((float) timeIntoFade / fadeDuration);
            }
        }

        if (alpha < 0f) alpha = 0f;
        if (alpha > 1f) alpha = 1f;

        return (int) (alpha * 255);
    }

    /**
     * Renders a full wireframe box
     */
    private void renderFullCube(BlockPos pos) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float o = -0.01f; // Slight offset

        float minX = x - o;
        float maxX = x + 1 + o;
        float minY = y - o;
        float maxY = y + 1 + o;
        float minZ = z - o;
        float maxZ = z + 1 + o;

        // Bottom Square
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX, minY, minZ);

        // Top Square
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, minZ);

        // Vertical Lines
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
    }

    /**
     * Renders corner braces
     */
    private void renderCorners(BlockPos pos) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();

        float o = -0.01f;
        float b = 0.25f;

        float minX = x - o;
        float maxX = x + 1 + o;
        float minY = y - o;
        float maxY = y + 1 + o;
        float minZ = z - o;
        float maxZ = z + 1 + o;

        // Bottom Corners
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(minX + b, minY, minZ);
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(minX, minY + b, minZ);
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(minX, minY, minZ + b);

        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX - b, minY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, minY + b, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, minY, minZ + b);

        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX + b, minY, maxZ);
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX, minY + b, maxZ);
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX, minY, maxZ - b);

        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(maxX - b, minY, maxZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(maxX, minY + b, maxZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(maxX, minY, maxZ - b);

        // Top Corners
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(minX + b, maxY, minZ);
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(minX, maxY - b, minZ);
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(minX, maxY, minZ + b);

        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX - b, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY - b, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ + b);

        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(minX + b, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY - b, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ - b);

        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(maxX - b, maxY, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(maxX, maxY - b, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ - b);
    }

    private void renderFilledCube(BlockPos pos) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float o = -0.01f; // Expand slightly to cover the block

        float minX = x - o;
        float maxX = x + 1 + o;
        float minY = y - o;
        float maxY = y + 1 + o;
        float minZ = z - o;
        float maxZ = z + 1 + o;

        // Bottom
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(minX, minY, maxZ);
        // Top
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        // Front
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        // Back
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        // Left
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, minZ);
        // Right
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(maxX, minY, maxZ);
    }

    private void renderFilledCore(BlockPos pos) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();

        // 0.5f = Center of block.
        // 0.25f = Half the size of our small cube (Total size will be 0.5 blocks)
        float cx = x + 0.5f;
        float cy = y + 0.5f;
        float cz = z + 0.5f;
        float r = 0.25f; // Radius (distance from center to wall)

        float minX = cx - r;
        float maxX = cx + r;
        float minY = cy - r;
        float maxY = cy + r;
        float minZ = cz - r;
        float maxZ = cz + r;

        // Bottom
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(minX, minY, maxZ);
        // Top
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        // Front
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        // Back
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        // Left
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, minZ);
        // Right
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(maxX, minY, maxZ);
    }

    /**
     * Renders a smaller wireframe box centered inside the block
     */
    private void renderSmallCube(BlockPos pos) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();

        // 0.5f = Center of block.
        // 0.25f = Half the size of our small cube (Total size will be 0.5 blocks)
        float cx = x + 0.5f;
        float cy = y + 0.5f;
        float cz = z + 0.5f;
        float r = 0.25f; // Radius (distance from center to wall)

        float minX = cx - r;
        float maxX = cx + r;
        float minY = cy - r;
        float maxY = cy + r;
        float minZ = cz - r;
        float maxZ = cz + r;

        // Bottom Square
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX, minY, minZ);

        // Top Square
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
        GL11.glVertex3f(minX, maxY, minZ);

        // Vertical Connecting Lines
        GL11.glVertex3f(minX, minY, minZ);
        GL11.glVertex3f(minX, maxY, minZ);
        GL11.glVertex3f(maxX, minY, minZ);
        GL11.glVertex3f(maxX, maxY, minZ);
        GL11.glVertex3f(maxX, minY, maxZ);
        GL11.glVertex3f(maxX, maxY, maxZ);
        GL11.glVertex3f(minX, minY, maxZ);
        GL11.glVertex3f(minX, maxY, maxZ);
    }
}
