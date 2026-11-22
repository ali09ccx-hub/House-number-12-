package com.example.engine3d

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class AABB(
    val minX: Float,
    val minY: Float,
    val minZ: Float,
    val maxX: Float,
    val maxY: Float,
    val maxZ: Float,
    val tag: String = ""
) {
    fun intersectsCylinderXZ(px: Float, pz: Float, radius: Float, minPlayerY: Float, maxPlayerY: Float): Boolean {
        if (maxPlayerY <= minY || minPlayerY >= maxY) return false
        val closestX = px.coerceIn(minX, maxX)
        val closestZ = pz.coerceIn(minZ, maxZ)
        val dx = px - closestX
        val dz = pz - closestZ
        return (dx * dx + dz * dz) < (radius * radius)
    }

    fun intersectRay(
        ox: Float, oy: Float, oz: Float,
        dx: Float, dy: Float, dz: Float,
        maxDist: Float
    ): Float? {
        var tMin = 0f
        var tMax = maxDist

        val axes = arrayOf(
            Triple(ox, dx, minX to maxX),
            Triple(oy, dy, minY to maxY),
            Triple(oz, dz, minZ to maxZ)
        )
        for ((origin, dir, bounds) in axes) {
            val (bMin, bMax) = bounds
            if (abs(dir) < 1e-6f) {
                if (origin < bMin || origin > bMax) return null
            } else {
                val invD = 1f / dir
                var t1 = (bMin - origin) * invD
                var t2 = (bMax - origin) * invD
                if (t1 > t2) {
                    val tmp = t1; t1 = t2; t2 = tmp
                }
                tMin = max(tMin, t1)
                tMax = min(tMax, t2)
                if (tMin > tMax) return null
            }
        }
        return tMin
    }
}

object Math3D {
    fun resolveMovementXZ(
        currentX: Float,
        currentZ: Float,
        deltaX: Float,
        deltaZ: Float,
        radius: Float,
        colliders: List<AABB>
    ): Pair<Float, Float> {
        val totalDist = sqrt(deltaX * deltaX + deltaZ * deltaZ)
        val steps = kotlin.math.ceil((totalDist / 0.10f).toDouble()).toInt().coerceAtLeast(1)
        val stepX = deltaX / steps
        val stepZ = deltaZ / steps

        var curX = currentX
        var curZ = currentZ

        for (i in 0 until steps) {
            var nextX = curX + stepX
            for (box in colliders) {
                if (box.intersectsCylinderXZ(nextX, curZ, radius, 0.05f, 1.75f)) {
                    nextX = curX
                    break
                }
            }
            curX = nextX

            var nextZ = curZ + stepZ
            for (box in colliders) {
                if (box.intersectsCylinderXZ(curX, nextZ, radius, 0.05f, 1.75f)) {
                    nextZ = curZ
                    break
                }
            }
            curZ = nextZ
        }
        return curX to curZ
    }

    fun yawPitchToForward(yawDeg: Float, pitchDeg: Float): FloatArray {
        val yawRad = Math.toRadians(yawDeg.toDouble())
        val pitchRad = Math.toRadians(pitchDeg.toDouble())
        val cosP = kotlin.math.cos(pitchRad).toFloat()
        val fx = (kotlin.math.sin(yawRad) * cosP).toFloat()
        val fy = kotlin.math.sin(pitchRad).toFloat()
        val fz = (-kotlin.math.cos(yawRad) * cosP).toFloat()
        val len = sqrt(fx * fx + fy * fy + fz * fz).coerceAtLeast(1e-5f)
        return floatArrayOf(fx / len, fy / len, fz / len)
    }
}
