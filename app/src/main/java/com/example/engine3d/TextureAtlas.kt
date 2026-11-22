package com.example.engine3d

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.opengl.GLES30
import android.opengl.GLUtils
import kotlin.random.Random

enum class TexId {
    WOOD_FLOOR,
    KITCHEN_TILE,
    PLASTER_WALL,
    EXTERIOR_BRICK,
    ASPHALT,
    SIDEWALK,
    GRASS,
    WOOD_DOOR,
    FABRIC,
    STORE_SHELF,
    STORE_SIGN,
    MUD_STAIN,
    CRT_SCREEN,
    FOLIAGE,
    METAL_DARK,
    CAR_PAINT
}

class TextureAtlas {
    private val textures = IntArray(TexId.entries.size)

    fun initAll() {
        GLES30.glGenTextures(textures.size, textures, 0)
        TexId.entries.forEach { id ->
            val bmp = buildTextureBitmap(id)
            uploadTexture(textures[id.ordinal], bmp, pixelated = true)
            bmp.recycle()
        }
    }

    fun getHandle(id: TexId): Int = textures[id.ordinal]

    fun release() {
        GLES30.glDeleteTextures(textures.size, textures, 0)
    }

    private fun uploadTexture(handle: Int, bitmap: Bitmap, pixelated: Boolean) {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, handle)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            if (pixelated) GLES30.GL_NEAREST_MIPMAP_LINEAR else GLES30.GL_LINEAR_MIPMAP_LINEAR
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MAG_FILTER,
            if (pixelated) GLES30.GL_NEAREST else GLES30.GL_LINEAR
        )
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
    }

    private fun buildTextureBitmap(id: TexId): Bitmap {
        val size = if (id == TexId.STORE_SIGN) 256 else 128
        val height = if (id == TexId.STORE_SIGN) 64 else 128
        val bmp = Bitmap.createBitmap(size, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val rng = Random(id.ordinal * 97 + 13)

        when (id) {
            TexId.WOOD_FLOOR -> {
                c.drawColor(Color.rgb(92, 62, 38))
                val plankH = 32
                for (row in 0 until 4) {
                    val shade = rng.nextInt(-10, 12)
                    p.color = Color.rgb(92 + shade, 62 + shade, 38 + shade / 2)
                    c.drawRect(0f, row * plankH.toFloat(), 128f, (row + 1) * plankH.toFloat(), p)
                    // Wood grain lines
                    p.color = Color.argb(40, 42, 24, 12)
                    for (g in 0..5) {
                        val gy = row * plankH + 4f + g * 4.5f
                        c.drawLine(0f, gy, 128f, gy + rng.nextFloat() * 2f - 1f, p)
                    }
                    // Plank seam
                    p.color = Color.rgb(48, 30, 18)
                    p.strokeWidth = 2f
                    c.drawLine(0f, (row + 1) * plankH.toFloat(), 128f, (row + 1) * plankH.toFloat(), p)
                    val jointX = ((row * 53 + 28) % 128).toFloat()
                    c.drawLine(jointX, row * plankH.toFloat(), jointX, (row + 1) * plankH.toFloat(), p)
                }
            }

            TexId.KITCHEN_TILE -> {
                val tile = 32
                for (y in 0 until 4) {
                    for (x in 0 until 4) {
                        val even = (x + y) % 2 == 0
                        p.color = if (even) Color.rgb(176, 174, 164) else Color.rgb(118, 126, 130)
                        c.drawRect(x * tile.toFloat(), y * tile.toFloat(), (x + 1) * tile.toFloat(), (y + 1) * tile.toFloat(), p)
                    }
                }
                p.color = Color.rgb(65, 68, 70)
                p.strokeWidth = 2f
                for (i in 0..4) {
                    c.drawLine(i * 32f, 0f, i * 32f, 128f, p)
                    c.drawLine(0f, i * 32f, 128f, i * 32f, p)
                }
            }

            TexId.PLASTER_WALL -> {
                c.drawColor(Color.rgb(172, 165, 150))
                addNoise(c, p, rng, 14, 128, 128)
                // Subtle baseboard molding strip at bottom edge
                p.color = Color.rgb(78, 56, 40)
                c.drawRect(0f, 114f, 128f, 128f, p)
            }

            TexId.EXTERIOR_BRICK -> {
                c.drawColor(Color.rgb(98, 92, 86))
                val bh = 16f
                val bw = 32f
                for (row in 0 until 8) {
                    val offset = if (row % 2 == 1) bw * 0.5f else 0f
                    for (col in -1..4) {
                        val shade = rng.nextInt(-14, 15)
                        p.color = Color.rgb(
                            (126 + shade).coerceIn(60, 170),
                            (78 + shade / 2).coerceIn(50, 120),
                            (64 + shade / 2).coerceIn(40, 100)
                        )
                        c.drawRect(col * bw + offset + 2f, row * bh + 2f, (col + 1) * bw + offset - 1f, (row + 1) * bh - 1f, p)
                    }
                }
            }

            TexId.ASPHALT -> {
                c.drawColor(Color.rgb(38, 42, 48))
                addNoise(c, p, rng, 18, 128, 128)
                // Wet sheen puddle patch
                p.color = Color.argb(36, 120, 140, 165)
                c.drawOval(RectF(20f, 30f, 98f, 88f), p)
            }

            TexId.SIDEWALK -> {
                c.drawColor(Color.rgb(112, 115, 118))
                addNoise(c, p, rng, 10, 128, 128)
                p.color = Color.rgb(70, 72, 76)
                p.strokeWidth = 2.5f
                c.drawRect(1f, 1f, 127f, 127f, p.apply { style = Paint.Style.STROKE })
                p.style = Paint.Style.FILL
                c.drawLine(64f, 0f, 64f, 128f, p)
            }

            TexId.GRASS -> {
                c.drawColor(Color.rgb(34, 52, 32))
                for (i in 0 until 320) {
                    p.color = Color.rgb(26 + rng.nextInt(26), 44 + rng.nextInt(32), 22 + rng.nextInt(18))
                    val gx = rng.nextFloat() * 128f
                    val gy = rng.nextFloat() * 128f
                    c.drawRect(gx, gy, gx + 2f, gy + 5f, p)
                }
            }

            TexId.WOOD_DOOR -> {
                c.drawColor(Color.rgb(76, 46, 28))
                // Raised panels
                p.color = Color.rgb(58, 34, 20)
                c.drawRect(18f, 12f, 110f, 56f, p)
                c.drawRect(18f, 68f, 110f, 116f, p)
                p.color = Color.rgb(92, 58, 36)
                c.drawRect(24f, 18f, 104f, 50f, p)
                c.drawRect(24f, 74f, 104f, 110f, p)
                // Brass peephole & handle
                p.color = Color.rgb(212, 175, 76)
                c.drawCircle(64f, 40f, 5f, p)
                p.color = Color.rgb(24, 24, 28)
                c.drawCircle(64f, 40f, 2.5f, p)
                p.color = Color.rgb(212, 175, 76)
                c.drawCircle(104f, 64f, 6f, p)
            }

            TexId.FABRIC -> {
                c.drawColor(Color.rgb(92, 98, 108))
                p.color = Color.argb(35, 255, 255, 255)
                for (i in 0 until 128 step 4) {
                    c.drawLine(0f, i.toFloat(), 128f, i.toFloat(), p)
                    c.drawLine(i.toFloat(), 0f, i.toFloat(), 128f, p)
                }
            }

            TexId.STORE_SHELF -> {
                c.drawColor(Color.rgb(42, 46, 54))
                val colors = intArrayOf(
                    Color.rgb(192, 58, 48),
                    Color.rgb(52, 138, 188),
                    Color.rgb(220, 168, 48),
                    Color.rgb(66, 152, 88),
                    Color.rgb(208, 208, 200)
                )
                for (shelf in 0 until 4) {
                    val sy = shelf * 32f
                    p.color = Color.rgb(170, 174, 180)
                    c.drawRect(0f, sy + 27f, 128f, sy + 32f, p)
                    for (item in 0 until 8) {
                        p.color = colors[(shelf * 3 + item) % colors.size]
                        val ix = item * 16f + 2f
                        c.drawRect(ix, sy + 6f, ix + 11f, sy + 27f, p)
                        p.color = Color.argb(110, 255, 255, 255)
                        c.drawRect(ix + 1f, sy + 11f, ix + 10f, sy + 16f, p)
                    }
                }
            }

            TexId.STORE_SIGN -> {
                c.drawColor(Color.rgb(22, 34, 48))
                p.color = Color.rgb(235, 178, 72)
                p.style = Paint.Style.STROKE
                p.strokeWidth = 4f
                c.drawRect(4f, 4f, 252f, 60f, p)
                p.style = Paint.Style.FILL
                p.textAlign = Paint.Align.CENTER
                p.textSize = 30f
                p.typeface = Typeface.DEFAULT_BOLD
                c.drawText("بقالة الأمل", 128f, 42f, p)
            }

            TexId.MUD_STAIN -> {
                c.drawColor(Color.TRANSPARENT)
                p.color = Color.rgb(52, 36, 22)
                c.drawOval(RectF(14f, 22f, 114f, 102f), p)
                p.color = Color.rgb(38, 26, 16)
                c.drawOval(RectF(26f, 34f, 76f, 84f), p)
                c.drawOval(RectF(68f, 40f, 106f, 92f), p)
            }

            TexId.CRT_SCREEN -> {
                c.drawColor(Color.rgb(28, 38, 42))
                for (y in 0 until 128 step 2) {
                    val v = rng.nextInt(32, 95)
                    p.color = Color.rgb(v, v + 12, v + 18)
                    c.drawLine(0f, y.toFloat(), 128f, y.toFloat(), p)
                }
            }

            TexId.FOLIAGE -> {
                c.drawColor(Color.rgb(24, 48, 26))
                for (i in 0 until 140) {
                    p.color = Color.rgb(18 + rng.nextInt(28), 38 + rng.nextInt(42), 18 + rng.nextInt(24))
                    val x = rng.nextFloat() * 128f
                    val y = rng.nextFloat() * 128f
                    c.drawCircle(x, y, 4f + rng.nextFloat() * 6f, p)
                }
            }

            TexId.METAL_DARK -> {
                c.drawColor(Color.rgb(54, 58, 66))
                addNoise(c, p, rng, 12, 128, 128)
            }

            TexId.CAR_PAINT -> {
                c.drawColor(Color.rgb(44, 58, 76))
                p.color = Color.argb(40, 210, 225, 245)
                c.drawRect(0f, 14f, 128f, 24f, p)
            }
        }
        return bmp
    }

    private fun addNoise(c: Canvas, p: Paint, rng: Random, amp: Int, w: Int, h: Int) {
        for (i in 0 until 450) {
            val v = rng.nextInt(255)
            p.color = Color.argb(amp, v, v, v)
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            c.drawRect(x, y, x + 2f, y + 2f, p)
        }
    }
}
