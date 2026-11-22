package com.example.engine3d

object FurnitureAndNpcBuilder {

    fun buildStaticPropsAndFurniture(batch: MeshBatcher) {
        val fabric = batch.get(TexId.FABRIC)
        val wood = batch.get(TexId.WOOD_FLOOR)
        val darkWood = batch.get(TexId.WOOD_DOOR)
        val metal = batch.get(TexId.METAL_DARK)
        val plaster = batch.get(TexId.PLASTER_WALL)
        val crt = batch.get(TexId.CRT_SCREEN, 0.55f)
        val shelf = batch.get(TexId.STORE_SHELF)
        val foliage = batch.get(TexId.FOLIAGE)
        val carPaint = batch.get(TexId.CAR_PAINT)
        val glow = batch.get(TexId.CRT_SCREEN, 0.85f)

        // ======================== LIVING ROOM ========================
        // Area Rug (-3.0, 0.01, 0.55)
        fabric.addBox(-4.2f, 0.004f, -0.75f, -1.8f, 0.016f, 1.85f, 0.58f, 0.34f, 0.28f)

        // Plush 3-Seat Sofa at (-3.0, -1.30) facing +Z
        // Wooden legs
        for (lx in floatArrayOf(-4.02f, -1.98f)) {
            for (lz in floatArrayOf(-1.68f, -0.94f)) {
                darkWood.addCylinder(lx, 0f, lz, 0.035f, 0.025f, 0.14f, 6)
            }
        }
        // Seat frame + 3 plush cushions + backrest + armrests
        fabric.addBox(-4.10f, 0.14f, -1.72f, -1.90f, 0.36f, -0.90f, 0.36f, 0.42f, 0.48f)
        for (i in 0..2) {
            val cx0 = -4.02f + i * 0.68f
            fabric.addBox(cx0, 0.36f, -1.58f, cx0 + 0.65f, 0.48f, -0.92f, 0.42f, 0.48f, 0.56f)
        }
        fabric.addBox(-4.10f, 0.36f, -1.74f, -1.90f, 0.90f, -1.52f, 0.34f, 0.40f, 0.46f)
        fabric.addBox(-4.15f, 0.36f, -1.72f, -3.96f, 0.66f, -0.90f, 0.34f, 0.40f, 0.46f)
        fabric.addBox(-2.04f, 0.36f, -1.72f, -1.85f, 0.66f, -0.90f, 0.34f, 0.40f, 0.46f)

        // Coffee Table at (-3.0, 0.55) with Ceramic Mug & TV Remote
        for (lx in floatArrayOf(-3.55f, -2.45f)) {
            for (lz in floatArrayOf(0.22f, 0.88f)) {
                darkWood.addBox(lx - 0.03f, 0f, lz - 0.03f, lx + 0.03f, 0.42f, lz + 0.03f)
            }
        }
        wood.addBox(-3.62f, 0.42f, 0.16f, -2.38f, 0.47f, 0.94f, 0.92f, 0.86f, 0.78f)
        // Ceramic Mug
        plaster.addCylinder(-2.75f, 0.47f, 0.52f, 0.045f, 0.048f, 0.10f, 8, 0.92f, 0.88f, 0.80f)
        // Remote control
        metal.addBox(-3.28f, 0.47f, 0.48f, -3.10f, 0.50f, 0.68f, 0.22f, 0.24f, 0.28f)

        // TV Console + Retro CRT Television against front wall (-3.0, 2.52) facing -Z
        darkWood.addBox(-3.72f, 0f, 2.26f, -2.28f, 0.62f, 2.82f, 0.82f, 0.76f, 0.70f)
        metal.addBox(-3.36f, 0.62f, 2.34f, -2.64f, 1.18f, 2.78f, 0.28f, 0.30f, 0.34f)
        crt.addQuad(-2.71f, 0.70f, 2.33f, 0f, 1f, -3.29f, 0.70f, 2.33f, 1f, 1f, -3.29f, 1.11f, 2.33f, 1f, 0f, -2.71f, 1.11f, 2.33f, 0f, 0f)
        // Rabbit-ear TV antenna
        metal.addCylinder(-3.0f, 1.18f, 2.55f, 0.04f, 0.03f, 0.06f, 6)
        metal.addBox(-3.20f, 1.24f, 2.54f, -3.02f, 1.48f, 2.56f, 0.65f, 0.68f, 0.72f)
        metal.addBox(-2.98f, 1.24f, 2.54f, -2.80f, 1.48f, 2.56f, 0.65f, 0.68f, 0.72f)

        // Potted Indoor Plant in NW Living Corner (-4.48, 2.48)
        darkWood.addCylinder(-4.48f, 0f, 2.48f, 0.16f, 0.21f, 0.38f, 8, 0.72f, 0.44f, 0.30f)
        darkWood.addCylinder(-4.48f, 0.36f, 2.48f, 0.04f, 0.03f, 0.45f, 6)
        foliage.addSphere(-4.48f, 0.92f, 2.48f, 0.32f, 0.34f, 0.32f, 5, 8, 0.95f, 1.0f, 0.95f)

        // Bookshelf against West wall (-4.60, 0.55) with multi-colored books
        darkWood.addBox(-4.84f, 0f, -0.32f, -4.38f, 1.82f, 1.42f, 0.86f, 0.80f, 0.72f)
        for (shelfY in floatArrayOf(0.45f, 0.92f, 1.38f)) {
            wood.addBox(-4.78f, shelfY, -0.26f, -4.36f, shelfY + 0.04f, 1.36f)
            for (b in 0..6) {
                val bz = -0.18f + b * 0.21f
                val h = 0.24f + (b % 3) * 0.05f
                fabric.addBox(-4.72f, shelfY + 0.04f, bz, -4.40f, shelfY + 0.04f + h, bz + 0.14f,
                    0.35f + (b % 3) * 0.2f, 0.32f + ((b + 1) % 3) * 0.15f, 0.42f + ((b + 2) % 3) * 0.15f)
            }
        }

        // ======================== KITCHEN ========================
        // Back Kitchen Counter & West L-Counter
        plaster.addBox(-4.84f, 0f, -6.82f, -1.68f, 0.86f, -6.08f, 0.72f, 0.74f, 0.72f)
        metal.addBox(-4.86f, 0.86f, -6.84f, -1.66f, 0.92f, -6.05f, 0.82f, 0.84f, 0.86f)
        // Recessed Sink & Curved Faucet
        metal.addBox(-3.65f, 0.91f, -6.68f, -2.95f, 0.94f, -6.18f, 0.42f, 0.46f, 0.50f)
        metal.addCylinder(-3.30f, 0.92f, -6.72f, 0.025f, 0.02f, 0.24f, 6, 0.82f, 0.85f, 0.88f)
        metal.addBox(-3.32f, 1.14f, -6.72f, -3.28f, 1.18f, -6.54f, 0.82f, 0.85f, 0.88f)
        // Stove & 4 Burners + Oven Door
        metal.addBox(-2.60f, 0.02f, -6.80f, -1.78f, 0.92f, -6.04f, 0.28f, 0.30f, 0.34f)
        for (bx in floatArrayOf(-2.40f, -1.98f)) {
            for (bz in floatArrayOf(-6.60f, -6.24f)) {
                metal.addCylinder(bx, 0.92f, bz, 0.09f, 0.09f, 0.02f, 8, 0.18f, 0.18f, 0.20f)
            }
        }
        // Upper Wall Cabinets
        darkWood.addBox(-4.84f, 1.65f, -6.84f, -2.75f, 2.45f, -6.42f, 0.85f, 0.80f, 0.74f)
        // West Counter
        plaster.addBox(-4.84f, 0f, -6.05f, -4.08f, 0.86f, -3.58f, 0.72f, 0.74f, 0.72f)
        metal.addBox(-4.86f, 0.86f, -6.05f, -4.05f, 0.92f, -3.56f, 0.82f, 0.84f, 0.86f)
        // Refrigerator (-4.42, -2.82)
        plaster.addBox(-4.84f, 0f, -3.42f, -4.02f, 1.92f, -2.24f, 0.86f, 0.88f, 0.90f)
        metal.addBox(-4.02f, 1.32f, -3.40f, -3.99f, 1.35f, -2.26f, 0.45f, 0.48f, 0.52f)
        metal.addBox(-4.01f, 0.85f, -2.42f, -3.96f, 1.24f, -2.38f, 0.75f, 0.78f, 0.82f)
        metal.addBox(-4.01f, 1.46f, -2.42f, -3.96f, 1.78f, -2.38f, 0.75f, 0.78f, 0.82f)

        // Dining Table + 2 Sculpted 4-Legged Chairs at (-2.25, -4.35)
        darkWood.addCylinder(-2.25f, 0f, -4.35f, 0.08f, 0.07f, 0.74f, 8)
        darkWood.addCylinder(-2.25f, 0f, -4.35f, 0.34f, 0.28f, 0.05f, 8)
        wood.addCylinder(-2.25f, 0.74f, -4.35f, 0.56f, 0.56f, 0.05f, 12, 0.95f, 0.88f, 0.80f)
        buildChair(darkWood, -2.25f, -3.55f, facingNorth = true)
        buildChair(darkWood, -2.25f, -5.15f, facingNorth = false)

        // ======================== HALLWAY BREAKER & BEDROOM ========================
        // Electrical Circuit Breaker Box mounted on Hallway wall (X = 0.96, Y = 1.15..1.75, Z = -2.8)
        metal.addBox(0.90f, 1.15f, -3.05f, 1.04f, 1.75f, -2.55f, 0.52f, 0.56f, 0.60f)
        glow.addBox(0.89f, 1.62f, -2.66f, 0.91f, 1.67f, -2.61f, 0.95f, 0.28f, 0.24f)

        // Bedroom Bed (2.85..4.85, Z in [0.25..1.85])
        darkWood.addBox(4.74f, 0f, 0.26f, 4.86f, 0.98f, 1.84f, 0.85f, 0.78f, 0.72f) // Headboard
        darkWood.addBox(2.86f, 0.14f, 0.26f, 4.76f, 0.34f, 1.84f, 0.85f, 0.78f, 0.72f) // Frame
        plaster.addBox(2.90f, 0.34f, 0.30f, 4.72f, 0.54f, 1.80f, 0.95f, 0.94f, 0.92f) // Mattress
        fabric.addBox(2.88f, 0.36f, 0.28f, 4.18f, 0.57f, 1.82f, 0.34f, 0.44f, 0.56f) // Blanket quilt
        plaster.addBox(4.32f, 0.54f, 0.42f, 4.68f, 0.68f, 0.96f, 0.96f, 0.95f, 0.92f) // Pillow 1
        plaster.addBox(4.32f, 0.54f, 1.14f, 4.68f, 0.68f, 1.68f, 0.96f, 0.95f, 0.92f) // Pillow 2

        // Nightstand & Bedside Lamp
        darkWood.addBox(4.20f, 0f, -0.62f, 4.84f, 0.56f, -0.08f)
        metal.addCylinder(4.52f, 0.56f, -0.35f, 0.08f, 0.03f, 0.16f, 8)
        glow.addCylinder(4.52f, 0.70f, -0.35f, 0.14f, 0.08f, 0.16f, 8, 1.0f, 0.82f, 0.48f)

        // Study Desk & Chair (1.45..2.60, Z in [2.10..2.85])
        darkWood.addBox(1.48f, 0.68f, 2.15f, 2.58f, 0.74f, 2.84f)
        darkWood.addBox(1.52f, 0f, 2.20f, 1.60f, 0.68f, 2.80f)
        darkWood.addBox(2.46f, 0f, 2.20f, 2.54f, 0.68f, 2.80f)
        buildChair(darkWood, 2.02f, 1.78f, facingNorth = false)

        // Wardrobe Closet in SE bedroom corner (3.98..4.86, Z in [-6.25..-4.75])
        darkWood.addBox(3.98f, 0f, -6.25f, 4.86f, 2.08f, -4.75f, 0.82f, 0.76f, 0.70f)
        metal.addBox(3.96f, 0.95f, -5.58f, 3.99f, 1.15f, -5.54f, 0.85f, 0.72f, 0.40f)
        metal.addBox(3.96f, 0.95f, -5.44f, 3.99f, 1.15f, -5.40f, 0.85f, 0.72f, 0.40f)

        // ======================== STREET CAR & TREES ========================
        // Parked Sedan Car (-8.6..-4.4, Z in [11.2..13.2])
        carPaint.addBox(-8.55f, 0.34f, 11.30f, -4.45f, 0.92f, 13.10f, 0.88f, 0.92f, 0.98f)
        carPaint.addBox(-7.55f, 0.92f, 11.40f, -5.35f, 1.48f, 13.00f, 0.86f, 0.90f, 0.96f)
        // Windshield & Rear Window
        metal.addQuad(-5.35f, 0.94f, 12.96f, 0f, 1f, -5.35f, 0.94f, 11.44f, 1f, 1f, -5.65f, 1.44f, 11.44f, 1f, 0f, -5.65f, 1.44f, 12.96f, 0f, 0f, 0.25f, 0.32f, 0.42f)
        // 4 Wheels
        for (wx in floatArrayOf(-7.75f, -5.25f)) {
            for (wz in floatArrayOf(11.28f, 13.12f)) {
                metal.addSphere(wx, 0.32f, wz, 0.32f, 0.32f, 0.14f, 5, 8, 0.18f, 0.18f, 0.20f)
            }
        }
        // Taillights
        glow.addBox(-8.57f, 0.56f, 11.42f, -8.54f, 0.74f, 11.74f, 0.85f, 0.15f, 0.12f)
        glow.addBox(-8.57f, 0.56f, 12.66f, -8.54f, 0.74f, 12.98f, 0.85f, 0.15f, 0.12f)

        // 3D Trees
        for ((tx, tz) in listOf(-8.5f to 5.8f, 8.8f to 5.5f, 14.5f to 5.2f)) {
            darkWood.addCylinder(tx, 0f, tz, 0.24f, 0.14f, 2.2f, 7, 0.62f, 0.48f, 0.38f)
            foliage.addSphere(tx, 2.75f, tz, 1.25f, 1.05f, 1.25f, 5, 8)
            foliage.addSphere(tx - 0.55f, 2.35f, tz + 0.45f, 0.85f, 0.75f, 0.85f, 4, 7)
            foliage.addSphere(tx + 0.50f, 2.40f, tz - 0.40f, 0.80f, 0.75f, 0.80f, 4, 7)
        }

        // ======================== AL-AMAL CONVENIENCE STORE ========================
        // 2 Product Aisles (19.25..20.25 & 22.05..23.05, Z in [3.45..6.15])
        shelf.addBox(19.25f, 0f, 3.45f, 20.25f, 1.62f, 6.15f, 1f, 1f, 1f, 1f, 0.65f, 0.6f)
        shelf.addBox(22.05f, 0f, 3.45f, 23.05f, 1.62f, 6.15f, 1f, 1f, 1f, 1f, 0.65f, 0.6f)
        // Refrigerated Drink Cooler along back wall
        metal.addBox(20.05f, 0f, 0.72f, 23.75f, 2.12f, 1.42f, 0.35f, 0.40f, 0.46f)
        glow.addBox(20.22f, 0.25f, 1.41f, 23.58f, 1.92f, 1.44f, 0.68f, 0.86f, 0.98f)
        // Checkout Counter (24.3..25.1, Z in [2.4..5.2])
        darkWood.addBox(24.32f, 0f, 2.42f, 25.08f, 1.05f, 5.18f, 0.86f, 0.82f, 0.78f)
        // Cash Register & Retro Radio on Counter
        metal.addBox(24.50f, 1.05f, 3.40f, 24.92f, 1.32f, 3.85f, 0.55f, 0.58f, 0.62f)
        metal.addBox(24.55f, 1.05f, 4.35f, 24.82f, 1.24f, 4.72f, 0.32f, 0.28f, 0.24f)
    }

    private fun buildChair(mb: MeshBuilder, cx: Float, cz: Float, facingNorth: Boolean) {
        for (dx in floatArrayOf(-0.18f, 0.18f)) {
            for (dz in floatArrayOf(-0.18f, 0.18f)) {
                mb.addBox(cx + dx - 0.025f, 0f, cz + dz - 0.025f, cx + dx + 0.025f, 0.44f, cz + dz + 0.025f)
            }
        }
        mb.addBox(cx - 0.21f, 0.44f, cz - 0.21f, cx + 0.21f, 0.49f, cz + 0.21f)
        val backZ = if (facingNorth) cz + 0.17f else cz - 0.21f
        mb.addBox(cx - 0.20f, 0.49f, backZ, cx + 0.20f, 0.92f, backZ + 0.04f)
    }

    fun buildDoorMesh(width: Float, height: Float): GpuMesh {
        val mb = MeshBuilder(512)
        // Hinged at local (0, 0, 0) extending along +X to width
        mb.addBox(0f, 0f, -0.04f, width, height, 0.04f, 1f, 1f, 1f, 1f / width, 1f / height, 1f)
        return GpuMesh(mb.toFloatBuffer(), mb.vertexCount, TexId.WOOD_DOOR)
    }

    fun buildMopMesh(): GpuMesh {
        val mb = MeshBuilder(512)
        // Leaning broom/mop centered locally at base (0,0,0)
        mb.addCylinder(0f, 0.14f, 0f, 0.022f, 0.022f, 1.32f, 6, 0.85f, 0.72f, 0.54f)
        mb.addCylinder(0f, 0f, 0f, 0.13f, 0.04f, 0.18f, 8, 0.92f, 0.90f, 0.85f)
        return GpuMesh(mb.toFloatBuffer(), mb.vertexCount, TexId.FABRIC)
    }

    fun buildMudSpotMesh(): GpuMesh {
        val mb = MeshBuilder(64)
        mb.addQuad(-0.42f, 0.014f, 0.42f, 0f, 1f, 0.42f, 0.014f, 0.42f, 1f, 1f, 0.42f, 0.014f, -0.42f, 1f, 0f, -0.42f, 0.014f, -0.42f, 0f, 0f)
        return GpuMesh(mb.toFloatBuffer(), mb.vertexCount, TexId.MUD_STAIN)
    }

    fun buildSuppliesBasketMesh(): GpuMesh {
        val mb = MeshBuilder(256)
        mb.addBox(-0.18f, 0f, -0.14f, 0.18f, 0.16f, 0.14f, 0.25f, 0.55f, 0.78f)
        mb.addBox(-0.12f, 0.16f, -0.08f, -0.02f, 0.28f, 0.06f, 0.92f, 0.75f, 0.22f) // Battery pack
        mb.addCylinder(0.08f, 0.16f, 0f, 0.045f, 0.04f, 0.20f, 6, 0.72f, 0.88f, 0.98f) // Water bottle
        return GpuMesh(mb.toFloatBuffer(), mb.vertexCount, TexId.PLASTER_WALL)
    }

    fun buildFlyerMesh(): GpuMesh {
        val mb = MeshBuilder(64)
        mb.addBox(-0.14f, 0.005f, -0.18f, 0.14f, 0.015f, 0.18f, 0.95f, 0.93f, 0.88f)
        return GpuMesh(mb.toFloatBuffer(), mb.vertexCount, TexId.PLASTER_WALL)
    }

    /**
     * Articulated composite PSX Human NPC (head, hair/cap, eyes, torso, coat/apron, arms, hands, legs, shoes, prop).
     */
    fun buildNpcMesh(
        coatR: Float, coatG: Float, coatB: Float,
        pantsR: Float, pantsG: Float, pantsB: Float,
        skinR: Float, skinG: Float, skinB: Float,
        hasCap: Boolean,
        hasLantern: Boolean,
        hasClipboard: Boolean
    ): GpuMesh {
        val mb = MeshBuilder(2048)
        // Shoes
        mb.addBox(-0.15f, 0f, -0.08f, -0.03f, 0.09f, 0.14f, 0.16f, 0.14f, 0.14f)
        mb.addBox(0.03f, 0f, -0.08f, 0.15f, 0.09f, 0.14f, 0.16f, 0.14f, 0.14f)
        // Legs
        mb.addCylinder(-0.09f, 0.09f, 0f, 0.068f, 0.075f, 0.74f, 7, pantsR, pantsG, pantsB)
        mb.addCylinder(0.09f, 0.09f, 0f, 0.068f, 0.075f, 0.74f, 7, pantsR, pantsG, pantsB)
        // Torso / Jacket
        mb.addBox(-0.22f, 0.82f, -0.13f, 0.22f, 1.44f, 0.13f, coatR, coatG, coatB)
        // Collar
        mb.addCylinder(0f, 1.43f, 0f, 0.10f, 0.09f, 0.07f, 7, coatR * 0.8f, coatG * 0.8f, coatB * 0.8f)
        // Arms
        mb.addCylinder(-0.27f, 0.88f, 0f, 0.055f, 0.06f, 0.52f, 6, coatR, coatG, coatB)
        mb.addCylinder(0.27f, 0.88f, 0f, 0.055f, 0.06f, 0.52f, 6, coatR, coatG, coatB)
        // Hands
        mb.addSphere(-0.27f, 0.85f, 0f, 0.05f, 0.055f, 0.05f, 4, 6, skinR, skinG, skinB)
        mb.addSphere(0.27f, 0.85f, 0f, 0.05f, 0.055f, 0.05f, 4, 6, skinR, skinG, skinB)
        // Neck & Sculpted Head
        mb.addCylinder(0f, 1.48f, 0f, 0.055f, 0.055f, 0.09f, 6, skinR, skinG, skinB)
        mb.addSphere(0f, 1.66f, 0f, 0.115f, 0.135f, 0.12f, 5, 8, skinR, skinG, skinB)
        // Facial features (nose + brow/eyes facing +Z)
        mb.addBox(-0.02f, 1.62f, 0.10f, 0.02f, 1.68f, 0.14f, skinR * 0.9f, skinG * 0.88f, skinB * 0.85f)
        mb.addBox(-0.07f, 1.68f, 0.105f, -0.025f, 1.71f, 0.125f, 0.12f, 0.12f, 0.14f)
        mb.addBox(0.025f, 1.68f, 0.105f, 0.07f, 1.71f, 0.125f, 0.12f, 0.12f, 0.14f)
        // Hair or Cap
        if (hasCap) {
            mb.addSphere(0f, 1.73f, -0.01f, 0.125f, 0.08f, 0.13f, 4, 8, 0.16f, 0.18f, 0.22f)
            mb.addBox(-0.11f, 1.71f, 0.06f, 0.11f, 1.74f, 0.21f, 0.14f, 0.16f, 0.20f)
        } else {
            mb.addSphere(0f, 1.73f, -0.02f, 0.122f, 0.085f, 0.125f, 4, 8, 0.35f, 0.34f, 0.36f)
        }
        if (hasLantern) {
            mb.addBox(0.24f, 0.64f, 0.04f, 0.36f, 0.82f, 0.16f, 0.95f, 0.80f, 0.40f)
        }
        if (hasClipboard) {
            mb.addBox(-0.34f, 0.86f, 0.08f, -0.14f, 1.12f, 0.14f, 0.56f, 0.40f, 0.24f)
        }
        return GpuMesh(mb.toFloatBuffer(), mb.vertexCount, TexId.PLASTER_WALL)
    }
}
