package com.example.engine3d

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Packed vertex format: [x, y, z, nx, ny, nz, u, v, r, g, b] (11 floats = 44 bytes per vertex)
 */
class MeshBuilder(initialFloats: Int = 8192) {
    private var data = FloatArray(initialFloats)
    private var size = 0

    val vertexCount: Int get() = size / 11

    private fun ensureCapacity(extraFloats: Int) {
        if (size + extraFloats > data.size) {
            var newCap = data.size * 2
            while (newCap < size + extraFloats) newCap *= 2
            data = data.copyOf(newCap)
        }
    }

    fun addVertex(
        x: Float, y: Float, z: Float,
        nx: Float, ny: Float, nz: Float,
        u: Float, v: Float,
        r: Float = 1f, g: Float = 1f, b: Float = 1f
    ) {
        ensureCapacity(11)
        data[size++] = x
        data[size++] = y
        data[size++] = z
        data[size++] = nx
        data[size++] = ny
        data[size++] = nz
        data[size++] = u
        data[size++] = v
        data[size++] = r
        data[size++] = g
        data[size++] = b
    }

    fun addTri(
        x1: Float, y1: Float, z1: Float, u1: Float, v1: Float,
        x2: Float, y2: Float, z2: Float, u2: Float, v2: Float,
        x3: Float, y3: Float, z3: Float, u3: Float, v3: Float,
        r: Float = 1f, g: Float = 1f, b: Float = 1f
    ) {
        val ax = x2 - x1; val ay = y2 - y1; val az = z2 - z1
        val bx = x3 - x1; val by = y3 - y1; val bz = z3 - z1
        var nx = ay * bz - az * by
        var ny = az * bx - ax * bz
        var nz = ax * by - ay * bx
        val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(1e-6f)
        nx /= len; ny /= len; nz /= len
        addVertex(x1, y1, z1, nx, ny, nz, u1, v1, r, g, b)
        addVertex(x2, y2, z2, nx, ny, nz, u2, v2, r, g, b)
        addVertex(x3, y3, z3, nx, ny, nz, u3, v3, r, g, b)
    }

    fun addQuad(
        x1: Float, y1: Float, z1: Float, u1: Float, v1: Float,
        x2: Float, y2: Float, z2: Float, u2: Float, v2: Float,
        x3: Float, y3: Float, z3: Float, u3: Float, v3: Float,
        x4: Float, y4: Float, z4: Float, u4: Float, v4: Float,
        r: Float = 1f, g: Float = 1f, b: Float = 1f
    ) {
        addTri(x1, y1, z1, u1, v1, x2, y2, z2, u2, v2, x3, y3, z3, u3, v3, r, g, b)
        addTri(x1, y1, z1, u1, v1, x3, y3, z3, u3, v3, x4, y4, z4, u4, v4, r, g, b)
    }

    fun addBox(
        minX: Float, minY: Float, minZ: Float,
        maxX: Float, maxY: Float, maxZ: Float,
        r: Float = 1f, g: Float = 1f, b: Float = 1f,
        uvScaleX: Float = 1f, uvScaleY: Float = 1f, uvScaleZ: Float = 1f
    ) {
        val w = (maxX - minX) * uvScaleX
        val h = (maxY - minY) * uvScaleY
        val d = (maxZ - minZ) * uvScaleZ
        // Front (+Z)
        addQuad(minX, minY, maxZ, 0f, h, maxX, minY, maxZ, w, h, maxX, maxY, maxZ, w, 0f, minX, maxY, maxZ, 0f, 0f, r, g, b)
        // Back (-Z)
        addQuad(maxX, minY, minZ, 0f, h, minX, minY, minZ, w, h, minX, maxY, minZ, w, 0f, maxX, maxY, minZ, 0f, 0f, r, g, b)
        // Left (-X)
        addQuad(minX, minY, minZ, 0f, h, minX, minY, maxZ, d, h, minX, maxY, maxZ, d, 0f, minX, maxY, minZ, 0f, 0f, r, g, b)
        // Right (+X)
        addQuad(maxX, minY, maxZ, 0f, h, maxX, minY, minZ, d, h, maxX, maxY, minZ, d, 0f, maxX, maxY, maxZ, 0f, 0f, r, g, b)
        // Top (+Y)
        addQuad(minX, maxY, maxZ, 0f, d, maxX, maxY, maxZ, w, d, maxX, maxY, minZ, w, 0f, minX, maxY, minZ, 0f, 0f, r, g, b)
        // Bottom (-Y)
        addQuad(minX, minY, minZ, 0f, d, maxX, minY, minZ, w, d, maxX, minY, maxZ, w, 0f, minX, minY, maxZ, 0f, 0f, r, g, b)
    }

    fun addCylinder(
        cx: Float, yBottom: Float, cz: Float,
        radiusBottom: Float, radiusTop: Float, height: Float,
        segments: Int = 10,
        r: Float = 1f, g: Float = 1f, b: Float = 1f
    ) {
        val yTop = yBottom + height
        for (i in 0 until segments) {
            val a0 = (2.0 * PI * i / segments).toFloat()
            val a1 = (2.0 * PI * (i + 1) / segments).toFloat()
            val c0 = cos(a0); val s0 = sin(a0)
            val c1 = cos(a1); val s1 = sin(a1)

            val x0b = cx + c0 * radiusBottom; val z0b = cz + s0 * radiusBottom
            val x1b = cx + c1 * radiusBottom; val z1b = cz + s1 * radiusBottom
            val x0t = cx + c0 * radiusTop; val z0t = cz + s0 * radiusTop
            val x1t = cx + c1 * radiusTop; val z1t = cz + s1 * radiusTop

            val u0 = i.toFloat() / segments
            val u1 = (i + 1).toFloat() / segments

            // Side quad (CCW facing outward)
            addTri(x1b, yBottom, z1b, u1, 1f, x0b, yBottom, z0b, u0, 1f, x0t, yTop, z0t, u0, 0f, r, g, b)
            addTri(x1b, yBottom, z1b, u1, 1f, x0t, yTop, z0t, u0, 0f, x1t, yTop, z1t, u1, 0f, r, g, b)

            // Top cap
            if (radiusTop > 0.001f) {
                addTri(cx, yTop, cz, 0.5f, 0.5f, x1t, yTop, z1t, 0.5f + c1 * 0.5f, 0.5f + s1 * 0.5f, x0t, yTop, z0t, 0.5f + c0 * 0.5f, 0.5f + s0 * 0.5f, r, g, b)
            }
            // Bottom cap
            if (radiusBottom > 0.001f) {
                addTri(cx, yBottom, cz, 0.5f, 0.5f, x0b, yBottom, z0b, 0.5f + c0 * 0.5f, 0.5f + s0 * 0.5f, x1b, yBottom, z1b, 0.5f + c1 * 0.5f, 0.5f + s1 * 0.5f, r, g, b)
            }
        }
    }

    fun addSphere(
        cx: Float, cy: Float, cz: Float,
        rx: Float, ry: Float, rz: Float,
        stacks: Int = 6, slices: Int = 10,
        r: Float = 1f, g: Float = 1f, b: Float = 1f
    ) {
        for (i in 0 until stacks) {
            val phi0 = (PI * i / stacks - PI / 2.0).toFloat()
            val phi1 = (PI * (i + 1) / stacks - PI / 2.0).toFloat()
            val y0 = cy + sin(phi0) * ry
            val y1 = cy + sin(phi1) * ry
            val r0 = cos(phi0)
            val r1 = cos(phi1)

            for (j in 0 until slices) {
                val t0 = (2.0 * PI * j / slices).toFloat()
                val t1 = (2.0 * PI * (j + 1) / slices).toFloat()

                val x00 = cx + cos(t0) * r0 * rx; val z00 = cz + sin(t0) * r0 * rz
                val x10 = cx + cos(t1) * r0 * rx; val z10 = cz + sin(t1) * r0 * rz
                val x01 = cx + cos(t0) * r1 * rx; val z01 = cz + sin(t0) * r1 * rz
                val x11 = cx + cos(t1) * r1 * rx; val z11 = cz + sin(t1) * r1 * rz

                val u0 = j.toFloat() / slices
                val u1 = (j + 1).toFloat() / slices
                val v0 = 1f - i.toFloat() / stacks
                val v1 = 1f - (i + 1).toFloat() / stacks

                if (i > 0) {
                    addTri(x00, y0, z00, u0, v0, x01, y1, z01, u0, v1, x10, y0, z10, u1, v0, r, g, b)
                }
                if (i < stacks - 1) {
                    addTri(x10, y0, z10, u1, v0, x01, y1, z01, u0, v1, x11, y1, z11, u1, v1, r, g, b)
                }
            }
        }
    }

    fun addGabledRoof(
        minX: Float, baseY: Float, minZ: Float,
        maxX: Float, peakY: Float, maxZ: Float,
        r: Float = 1f, g: Float = 1f, b: Float = 1f
    ) {
        val midX = (minX + maxX) * 0.5f
        // Left slope
        addQuad(minX, baseY, maxZ, 0f, 1f, midX, peakY, maxZ, 0f, 0f, midX, peakY, minZ, 3f, 0f, minX, baseY, minZ, 3f, 1f, r, g, b)
        // Right slope
        addQuad(midX, peakY, maxZ, 0f, 0f, maxX, baseY, maxZ, 0f, 1f, maxX, baseY, minZ, 3f, 1f, midX, peakY, minZ, 3f, 0f, r, g, b)
        // Front gable triangle
        addTri(minX, baseY, maxZ, 0f, 1f, maxX, baseY, maxZ, 2f, 1f, midX, peakY, maxZ, 1f, 0f, r * 0.85f, g * 0.85f, b * 0.85f)
        // Back gable triangle
        addTri(maxX, baseY, minZ, 0f, 1f, minX, baseY, minZ, 2f, 1f, midX, peakY, minZ, 1f, 0f, r * 0.85f, g * 0.85f, b * 0.85f)
    }

    fun toFloatBuffer(): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(data, 0, size)
        fb.position(0)
        return fb
    }
}
