package com.example.engine3d

import android.opengl.GLES30
import java.nio.FloatBuffer

class GpuMesh(buffer: FloatBuffer, val vertexCount: Int, val texId: TexId, val emissive: Float = 0f) {
    val vboId: Int

    init {
        val ids = IntArray(1)
        GLES30.glGenBuffers(1, ids, 0)
        vboId = ids[0]
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GLES30.GL_STATIC_DRAW)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
    }

    fun draw(shader: PsxShader, textureAtlas: TextureAtlas, modelMatrix: FloatArray, mvpMatrix: FloatArray) {
        if (vertexCount == 0) return
        GLES30.glUniformMatrix4fv(shader.uModelLoc, 1, false, modelMatrix, 0)
        GLES30.glUniformMatrix4fv(shader.uMvpLoc, 1, false, mvpMatrix, 0)
        GLES30.glUniform1f(shader.uEmissiveLoc, emissive)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureAtlas.getHandle(texId))
        GLES30.glUniform1i(shader.uTextureLoc, 0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        val stride = 11 * 4
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, stride, 0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, stride, 3 * 4)
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, stride, 6 * 4)
        GLES30.glEnableVertexAttribArray(3)
        GLES30.glVertexAttribPointer(3, 3, GLES30.GL_FLOAT, false, stride, 8 * 4)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
    }

    fun release() {
        GLES30.glDeleteBuffers(1, intArrayOf(vboId), 0)
    }
}

class MeshBatcher {
    private val builders = mutableMapOf<Pair<TexId, Float>, MeshBuilder>()

    fun get(texId: TexId, emissive: Float = 0f): MeshBuilder {
        return builders.getOrPut(texId to emissive) { MeshBuilder() }
    }

    fun buildAll(): List<GpuMesh> {
        return builders.entries.mapNotNull { (key, mb) ->
            if (mb.vertexCount == 0) null
            else GpuMesh(mb.toFloatBuffer(), mb.vertexCount, key.first, key.second)
        }
    }
}

object ArchitectureBuilder {
    fun buildWorldArchitecture(batch: MeshBatcher) {
        val grass = batch.get(TexId.GRASS)
        val sidewalk = batch.get(TexId.SIDEWALK)
        val asphalt = batch.get(TexId.ASPHALT)
        val woodFloor = batch.get(TexId.WOOD_FLOOR)
        val tile = batch.get(TexId.KITCHEN_TILE)
        val plaster = batch.get(TexId.PLASTER_WALL)
        val brick = batch.get(TexId.EXTERIOR_BRICK)
        val metal = batch.get(TexId.METAL_DARK)
        val sign = batch.get(TexId.STORE_SIGN, 0.72f)
        val lampGlow = batch.get(TexId.CRT_SCREEN, 0.95f)

        // === Ground, Lawn, Sidewalk & Wet Asphalt Road ===
        grass.addQuad(-22f, -0.02f, 28f, 0f, 18f, 36f, -0.02f, 28f, 18f, 18f, 36f, -0.02f, -15f, 18f, 0f, -22f, -0.02f, -15f, 0f, 0f, 0.85f, 0.92f, 0.85f)
        // Sidewalk slab along Z in [8.5, 10.8]
        sidewalk.addBox(-15f, -0.02f, 8.5f, 29f, 0.08f, 10.8f, 0.92f, 0.92f, 0.95f, 0.5f, 1f, 0.5f)
        // Walkway from sidewalk gate to porch steps
        sidewalk.addBox(-1.15f, -0.01f, 5.2f, 1.15f, 0.06f, 8.5f, 0.90f, 0.90f, 0.92f, 0.5f, 1f, 0.5f)
        // Asphalt road Z in [10.8, 17.5]
        asphalt.addBox(-20f, -0.04f, 10.8f, 34f, 0.0f, 17.5f, 0.9f, 0.9f, 0.95f, 0.35f, 1f, 0.35f)
        // Road dashed yellow lane markings
        val yellowLine = batch.get(TexId.PLASTER_WALL)
        var lx = -16f
        while (lx < 30f) {
            yellowLine.addBox(lx, 0.005f, 14.05f, lx + 1.8f, 0.012f, 14.22f, 0.85f, 0.68f, 0.22f)
            lx += 3.8f
        }

        // === House No. 12 Interior Floors & Ceiling ===
        // Living Room & Hallway hardwood (Z in [-2.0, 3.0], X in [-5.0, 5.0]) + Bedroom
        woodFloor.addQuad(-5.0f, 0.0f, 3.0f, 0f, 4f, 5.0f, 0.0f, 3.0f, 6f, 4f, 5.0f, 0.0f, -7.0f, 6f, 0f, -5.0f, 0.0f, -7.0f, 0f, 0f)
        // Kitchen Ceramic Tile (X in [-5.0, -0.8], Z in [-7.0, -2.0])
        tile.addQuad(-5.0f, 0.005f, -2.0f, 0f, 4f, -0.8f, 0.005f, -2.0f, 3.5f, 4f, -0.8f, 0.005f, -7.0f, 3.5f, 0f, -5.0f, 0.005f, -7.0f, 0f, 0f)
        // Ceiling
        plaster.addQuad(-5.0f, 3.0f, -7.0f, 0f, 0f, 5.0f, 3.0f, -7.0f, 4f, 0f, 5.0f, 3.0f, 3.0f, 4f, 4f, -5.0f, 3.0f, 3.0f, 0f, 4f, 0.78f, 0.76f, 0.72f)

        // === House No. 12 Walls (Inner Plaster + Outer Brick) ===
        // West wall
        plaster.addBox(-5.0f, 0f, -7.0f, -4.92f, 3.0f, 3.0f, 0.94f, 0.91f, 0.86f, 1f, 0.45f, 0.5f)
        brick.addBox(-5.16f, 0f, -7.15f, -5.0f, 3.1f, 3.15f, 0.92f, 0.88f, 0.85f, 1f, 0.5f, 0.5f)
        // East wall
        plaster.addBox(4.92f, 0f, -7.0f, 5.0f, 3.0f, 3.0f, 0.94f, 0.91f, 0.86f, 1f, 0.45f, 0.5f)
        brick.addBox(5.0f, 0f, -7.15f, 5.16f, 3.1f, 3.15f, 0.92f, 0.88f, 0.85f, 1f, 0.5f, 0.5f)

        // Front wall left & right + transom above front door
        plaster.addBox(-5.0f, 0f, 2.86f, -0.65f, 3.0f, 2.94f, 0.94f, 0.91f, 0.86f, 0.5f, 0.45f, 1f)
        brick.addBox(-5.15f, 0f, 2.94f, -0.65f, 3.1f, 3.12f, 0.94f, 0.88f, 0.84f, 0.5f, 0.5f, 1f)
        plaster.addBox(0.65f, 0f, 2.86f, 5.0f, 3.0f, 2.94f, 0.94f, 0.91f, 0.86f, 0.5f, 0.45f, 1f)
        brick.addBox(0.65f, 0f, 2.94f, 5.15f, 3.1f, 3.12f, 0.94f, 0.88f, 0.84f, 0.5f, 0.5f, 1f)
        // Transom above front door (Y in [2.25, 3.10])
        brick.addBox(-0.65f, 2.25f, 2.86f, 0.65f, 3.1f, 3.12f, 0.92f, 0.88f, 0.84f)
        // Living room front window frame & dark glass glow on front exterior
        metal.addBox(-3.65f, 1.05f, 3.08f, -2.05f, 2.30f, 3.16f, 0.25f, 0.28f, 0.34f)
        lampGlow.addBox(-3.55f, 1.15f, 3.14f, -2.15f, 2.20f, 3.18f, 0.85f, 0.68f, 0.42f)

        // Back wall left & right + transom above back door
        plaster.addBox(-5.0f, 0f, -6.94f, -0.60f, 3.0f, -6.86f, 0.92f, 0.89f, 0.84f, 0.5f, 0.45f, 1f)
        brick.addBox(-5.15f, 0f, -7.12f, -0.60f, 3.1f, -6.94f, 0.88f, 0.85f, 0.82f, 0.5f, 0.5f, 1f)
        plaster.addBox(0.60f, 0f, -6.94f, 5.0f, 3.0f, -6.86f, 0.92f, 0.89f, 0.84f, 0.5f, 0.45f, 1f)
        brick.addBox(0.60f, 0f, -7.12f, 5.15f, 3.1f, -6.94f, 0.88f, 0.85f, 0.82f, 0.5f, 0.5f, 1f)
        brick.addBox(-0.60f, 2.25f, -7.12f, 0.60f, 3.1f, -6.86f, 0.88f, 0.85f, 0.82f)

        // Interior Partition Walls
        plaster.addBox(-4.92f, 0f, -2.08f, -2.2f, 3.0f, -1.90f, 0.92f, 0.90f, 0.85f, 0.5f, 0.45f, 1f)
        plaster.addBox(1.02f, 0f, -0.2f, 1.18f, 3.0f, 2.90f, 0.92f, 0.89f, 0.84f, 1f, 0.45f, 0.5f)
        plaster.addBox(1.02f, 0f, -5.4f, 1.18f, 3.0f, -1.5f, 0.92f, 0.89f, 0.84f, 1f, 0.45f, 0.5f)
        plaster.addBox(1.02f, 2.25f, -1.5f, 1.18f, 3.0f, -0.2f, 0.92f, 0.89f, 0.84f)

        // House Pitched Roof & Front Porch
        brick.addGabledRoof(-5.45f, 3.08f, -7.35f, 5.45f, 5.10f, 3.35f, 0.42f, 0.36f, 0.34f)
        // Porch concrete deck & steps
        sidewalk.addBox(-2.45f, 0f, 3.1f, 2.45f, 0.16f, 5.2f, 0.88f, 0.88f, 0.90f)
        sidewalk.addBox(-1.45f, 0f, 5.2f, 1.45f, 0.08f, 5.6f, 0.84f, 0.84f, 0.86f)
        // Porch wooden pillars & roof overhang
        val woodProp = batch.get(TexId.WOOD_DOOR)
        woodProp.addBox(-2.25f, 0.16f, 4.92f, -2.05f, 2.65f, 5.12f, 0.95f, 0.92f, 0.88f)
        woodProp.addBox(2.05f, 0.16f, 4.92f, 2.25f, 2.65f, 5.12f, 0.95f, 0.92f, 0.88f)
        brick.addGabledRoof(-2.55f, 2.62f, 3.1f, 2.55f, 3.45f, 5.35f, 0.45f, 0.38f, 0.35f)
        // Porch wall lantern next to front door
        metal.addBox(0.82f, 1.88f, 3.10f, 1.02f, 2.15f, 3.26f)
        lampGlow.addBox(0.85f, 1.92f, 3.13f, 0.99f, 2.10f, 3.28f, 1.0f, 0.82f, 0.45f)

        // House "12" brass plaque
        val plaque = batch.get(TexId.STORE_SIGN, 0.15f)
        plaque.addBox(-1.15f, 1.65f, 3.11f, -0.78f, 1.90f, 3.14f, 0.85f, 0.72f, 0.42f)

        // === Yard Fences ===
        // Front fence left & right with posts & horizontal rails
        buildPicketFence(woodProp, -6.5f, 8.45f, -1.35f, 8.45f, 1.05f)
        buildPicketFence(woodProp, 1.35f, 8.45f, 6.5f, 8.45f, 1.05f)
        buildPicketFence(woodProp, -6.45f, -9.1f, -6.45f, 8.45f, 1.05f)
        buildPicketFence(woodProp, 6.45f, -9.1f, 6.45f, 8.45f, 1.05f)
        buildPicketFence(woodProp, -6.45f, -9.15f, 6.45f, -9.15f, 1.25f)

        // === Neighbor Houses (Atmospheric Backdrop) ===
        brick.addBox(9.5f, 0f, -5.5f, 16.5f, 3.3f, 2.5f, 0.78f, 0.76f, 0.78f, 0.5f, 0.5f, 0.5f)
        brick.addGabledRoof(9.2f, 3.3f, -5.8f, 16.8f, 5.2f, 2.8f, 0.36f, 0.34f, 0.36f)
        // House across the road (dark silhouettes with faint porch)
        brick.addBox(-4.5f, 0f, 19.2f, 4.5f, 3.4f, 25.0f, 0.65f, 0.66f, 0.70f, 0.4f, 0.4f, 0.4f)
        brick.addGabledRoof(-4.8f, 3.4f, 19.0f, 4.8f, 5.4f, 25.2f, 0.30f, 0.30f, 0.34f)
        lampGlow.addBox(-1.6f, 1.2f, 19.16f, -0.4f, 2.1f, 19.22f, 0.45f, 0.42f, 0.35f)

        // === Streetlamps along Sidewalk ===
        for (sx in floatArrayOf(-8.0f, 3.0f, 14.0f, 24.5f)) {
            metal.addCylinder(sx, 0.08f, 10.52f, 0.11f, 0.06f, 3.9f, 8, 0.35f, 0.38f, 0.42f)
            metal.addBox(sx - 0.06f, 3.95f, 10.48f, sx + 0.06f, 4.06f, 11.25f, 0.35f, 0.38f, 0.42f)
            lampGlow.addSphere(sx, 3.88f, 11.15f, 0.16f, 0.12f, 0.16f, 4, 8, 1.0f, 0.85f, 0.50f)
        }

        // === Al-Amal Convenience Store (X in [18.0, 26.5], Z in [0.5, 8.5]) ===
        tile.addQuad(18.0f, 0.02f, 8.5f, 0f, 5f, 26.5f, 0.02f, 8.5f, 5f, 5f, 26.5f, 0.02f, 0.5f, 5f, 0f, 18.0f, 0.02f, 0.5f, 0f, 0f, 1f, 1f, 1f)
        plaster.addQuad(18.0f, 3.1f, 0.5f, 0f, 0f, 26.5f, 3.1f, 0.5f, 4f, 0f, 26.5f, 3.1f, 8.5f, 4f, 4f, 18.0f, 3.1f, 8.5f, 0f, 4f, 0.92f, 0.94f, 0.98f)
        // Store walls
        plaster.addBox(17.90f, 0f, 0.5f, 18.12f, 3.15f, 8.5f, 0.95f, 0.95f, 0.98f, 0.5f, 0.5f, 0.5f)
        plaster.addBox(26.38f, 0f, 0.5f, 26.60f, 3.15f, 8.5f, 0.95f, 0.95f, 0.98f, 0.5f, 0.5f, 0.5f)
        plaster.addBox(17.90f, 0f, 0.45f, 26.60f, 3.15f, 0.68f, 0.95f, 0.95f, 0.98f, 0.5f, 0.5f, 0.5f)
        // Storefront left & right with display window
        brick.addBox(17.90f, 0f, 8.38f, 20.8f, 0.95f, 8.62f, 0.72f, 0.76f, 0.82f)
        brick.addBox(17.90f, 2.40f, 8.38f, 20.8f, 3.25f, 8.62f, 0.72f, 0.76f, 0.82f)
        brick.addBox(22.2f, 0f, 8.38f, 26.60f, 0.95f, 8.62f, 0.72f, 0.76f, 0.82f)
        brick.addBox(22.2f, 2.40f, 8.38f, 26.60f, 3.25f, 8.62f, 0.72f, 0.76f, 0.82f)
        brick.addBox(20.8f, 2.32f, 8.38f, 22.2f, 3.25f, 8.62f, 0.72f, 0.76f, 0.82f)
        // Store flat fascia roof
        metal.addBox(17.75f, 3.15f, 0.35f, 26.75f, 3.55f, 8.85f, 0.24f, 0.28f, 0.35f)
        // Glowing Arabic signboard above entrance
        sign.addQuad(19.6f, 2.75f, 8.87f, 0f, 1f, 24.4f, 2.75f, 8.87f, 1f, 1f, 24.4f, 3.50f, 8.87f, 1f, 0f, 19.6f, 3.50f, 8.87f, 0f, 0f)
        // Interior fluorescent tube strips
        lampGlow.addBox(19.5f, 3.04f, 2.5f, 24.5f, 3.09f, 2.7f, 0.92f, 0.98f, 1.0f)
        lampGlow.addBox(19.5f, 3.04f, 5.8f, 24.5f, 3.09f, 6.0f, 0.92f, 0.98f, 1.0f)
    }

    private fun buildPicketFence(mb: MeshBuilder, x1: Float, z1: Float, x2: Float, z2: Float, h: Float) {
        val dx = x2 - x1
        val dz = z2 - z1
        val len = kotlin.math.sqrt(dx * dx + dz * dz)
        val steps = (len / 0.45f).toInt().coerceAtLeast(2)
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val px = x1 + dx * t
            val pz = z1 + dz * t
            mb.addBox(px - 0.045f, 0f, pz - 0.045f, px + 0.045f, h, pz + 0.045f, 0.65f, 0.58f, 0.52f)
        }
        // Horizontal rails
        val minX = minOf(x1, x2) - 0.03f
        val maxX = maxOf(x1, x2) + 0.03f
        val minZ = minOf(z1, z2) - 0.03f
        val maxZ = maxOf(z1, z2) + 0.03f
        mb.addBox(minX, h * 0.30f, minZ, maxX, h * 0.38f, maxZ, 0.62f, 0.56f, 0.49f)
        mb.addBox(minX, h * 0.78f, minZ, maxX, h * 0.86f, maxZ, 0.62f, 0.56f, 0.49f)
    }
}
