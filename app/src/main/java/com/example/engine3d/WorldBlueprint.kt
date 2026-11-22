package com.example.engine3d

enum class InteractableId {
    NEIGHBOR_SALEH,
    FRONT_DOOR,
    BACK_DOOR,
    MOP_TOOL,
    MUD_SPOT_1,
    MUD_SPOT_2,
    MUD_SPOT_3,
    STORE_DOOR,
    SUPPLIES_BASKET,
    CASHIER_KARIM,
    PORCH_FLYER,
    KITCHEN_TABLE_DROP,
    BREAKER_BOX,
    BEDROOM_CLOSET
}

data class InteractableTarget(
    val id: InteractableId,
    val bounds: AABB,
    val maxRange: Float = 2.8f
)

/**
 * Single source of truth for all House No. 12, Street, and Al-Amal Store coordinates.
 * Every furniture AABB and corridor is verified to be flush with floors/walls and non-overlapping.
 */
object WorldBlueprint {
    const val HOUSE_MIN_X = -5.0f
    const val HOUSE_MAX_X = 5.0f
    const val HOUSE_MIN_Z = -7.0f
    const val HOUSE_MAX_Z = 3.0f
    const val HOUSE_WALL_H = 3.0f

    const val STORE_MIN_X = 18.0f
    const val STORE_MAX_X = 26.5f
    const val STORE_MIN_Z = 0.5f
    const val STORE_MAX_Z = 8.5f

    const val WORLD_MIN_X = -13.5f
    const val WORLD_MAX_X = 28.0f
    const val WORLD_MIN_Z = -9.5f
    const val WORLD_MAX_Z = 20.5f

    // Player spawn outside House No. 12 facing North (-Z) toward the house & Mr. Saleh
    const val START_X = 0.0f
    const val START_Z = 9.6f
    const val START_YAW = 12f

    // Mud stain floor coordinates (clear walking corridor)
    val mudSpots = listOf(
        Triple(InteractableId.MUD_SPOT_1, 0.0f, 1.5f),
        Triple(InteractableId.MUD_SPOT_2, -0.35f, -1.0f),
        Triple(InteractableId.MUD_SPOT_3, -1.35f, -3.2f)
    )

    // Static world colliders (walls + furniture + fences + street props + store shelves)
    val staticColliders: List<AABB> = buildList {
        // --- World Perimeter Boundaries ---
        add(AABB(WORLD_MIN_X - 1f, 0f, WORLD_MIN_Z - 1f, WORLD_MIN_X, 4f, WORLD_MAX_Z + 1f, "west_bound"))
        add(AABB(WORLD_MAX_X, 0f, WORLD_MIN_Z - 1f, WORLD_MAX_X + 1f, 4f, WORLD_MAX_Z + 1f, "east_bound"))
        add(AABB(WORLD_MIN_X - 1f, 0f, WORLD_MIN_Z - 1f, WORLD_MAX_X + 1f, 4f, WORLD_MIN_Z, "north_bound"))
        add(AABB(WORLD_MIN_X - 1f, 0f, 17.5f, WORLD_MAX_X + 1f, 4f, WORLD_MAX_Z + 1f, "south_road_bound"))

        // --- House No. 12 Exterior Walls ---
        // West wall (-5.0)
        add(AABB(-5.15f, 0f, -7.15f, -4.85f, HOUSE_WALL_H, 3.15f, "house_west_wall"))
        // East wall (+5.0)
        add(AABB(4.85f, 0f, -7.15f, 5.15f, HOUSE_WALL_H, 3.15f, "house_east_wall"))
        // Front wall (Z = +3.0) with doorway gap X in [-0.65, +0.65]
        add(AABB(-5.15f, 0f, 2.85f, -0.65f, HOUSE_WALL_H, 3.15f, "house_front_left"))
        add(AABB(0.65f, 0f, 2.85f, 5.15f, HOUSE_WALL_H, 3.15f, "house_front_right"))
        // Back wall (Z = -7.0) with back door gap X in [-0.60, +0.60]
        add(AABB(-5.15f, 0f, -7.15f, -0.60f, HOUSE_WALL_H, -6.85f, "house_back_left"))
        add(AABB(0.60f, 0f, -7.15f, 5.15f, HOUSE_WALL_H, -6.85f, "house_back_right"))

        // --- House Interior Partition Walls ---
        // Living/Kitchen partition at Z = -2.0 (X from -5.0 to -2.2; archway -2.2 to -0.8 is open)
        add(AABB(-5.0f, 0f, -2.12f, -2.2f, HOUSE_WALL_H, -1.88f, "kitchen_divider"))
        // Hallway/Bedroom divider at X = +1.1 (Z from -5.2 to +3.0, doorway gap Z in [-1.5, -0.2])
        add(AABB(0.98f, 0f, -0.2f, 1.22f, HOUSE_WALL_H, 3.0f, "bedroom_wall_front"))
        add(AABB(0.98f, 0f, -5.4f, 1.22f, HOUSE_WALL_H, -1.5f, "bedroom_wall_back"))

        // --- Living Room Furniture (X in [-4.85, -1.2], Z in [-1.85, +2.85]) ---
        // Sofa at (-3.0, -1.25) facing +Z toward TV
        add(AABB(-4.15f, 0f, -1.75f, -1.85f, 0.95f, -0.85f, "living_sofa"))
        // Coffee table at (-3.0, 0.55)
        add(AABB(-3.65f, 0f, 0.15f, -2.35f, 0.50f, 0.95f, "coffee_table"))
        // TV Stand + CRT TV against front wall (-3.0, 2.50)
        add(AABB(-3.75f, 0f, 2.22f, -2.25f, 1.25f, 2.84f, "tv_stand"))
        // Potted plant in NW living corner (-4.45, 2.45)
        add(AABB(-4.80f, 0f, 2.15f, -4.15f, 1.35f, 2.80f, "living_plant"))
        // Bookshelf flush against West wall (-4.60, 0.55)
        add(AABB(-4.85f, 0f, -0.35f, -4.35f, 1.85f, 1.45f, "bookshelf"))

        // --- Kitchen Furniture (X in [-4.85, -0.8], Z in [-6.85, -2.15]) ---
        // Back Kitchen Counter + Sink + Stove (Z = -6.45, X in [-4.85, -1.65])
        add(AABB(-4.85f, 0f, -6.85f, -1.65f, 0.95f, -6.05f, "kitchen_counter_back"))
        // West Kitchen Counter (X = -4.45, Z in [-6.05, -3.55])
        add(AABB(-4.85f, 0f, -6.05f, -4.05f, 0.95f, -3.55f, "kitchen_counter_west"))
        // Refrigerator (-4.45, -2.75)
        add(AABB(-4.85f, 0f, -3.45f, -4.00f, 1.95f, -2.20f, "refrigerator"))
        // Dining Table + Chairs (-2.25, -4.35)
        add(AABB(-2.85f, 0f, -4.95f, -1.65f, 0.85f, -3.75f, "dining_table"))

        // --- Bedroom Furniture (X in [1.25, 4.85], Z in [-6.85, +2.85]) ---
        // Bed flush against East wall (X = 3.85, Z = 1.05)
        add(AABB(2.85f, 0f, 0.25f, 4.85f, 0.95f, 1.85f, "bed"))
        // Nightstand (4.45, -0.35)
        add(AABB(4.15f, 0f, -0.65f, 4.85f, 0.65f, -0.05f, "nightstand"))
        // Study Desk against front-east corner (1.45..2.60, Z in [2.10..2.85])
        add(AABB(1.45f, 0f, 2.10f, 2.60f, 0.85f, 2.85f, "study_desk"))
        // Wardrobe Closet in SE bedroom corner (4.40, -5.50)
        add(AABB(3.95f, 0f, -6.30f, 4.85f, 2.10f, -4.70f, "bedroom_closet"))

        // --- Porch & Front Yard Fence ---
        // Porch columns
        add(AABB(-2.3f, 0f, 4.9f, -2.0f, 2.8f, 5.2f, "porch_col_l"))
        add(AABB(2.0f, 0f, 4.9f, 2.3f, 2.8f, 5.2f, "porch_col_r"))
        // Front Garden Fence along Z = 8.5 with gate gap X in [-1.3, +1.3]
        add(AABB(-6.5f, 0f, 8.35f, -1.3f, 1.2f, 8.65f, "fence_front_l"))
        add(AABB(1.3f, 0f, 8.35f, 6.5f, 1.2f, 8.65f, "fence_front_r"))
        // Side yard fences
        add(AABB(-6.6f, 0f, -8.5f, -6.3f, 1.2f, 8.65f, "fence_west"))
        add(AABB(6.3f, 0f, -8.5f, 6.6f, 1.2f, 8.65f, "fence_east"))
        // Backyard fence along Z = -9.2
        add(AABB(-6.5f, 0f, -9.35f, 6.5f, 1.4f, -9.05f, "fence_back"))

        // --- NPCs & Exterior Street Props ---
        // Neighbor Saleh standing near eastern garden fence
        add(AABB(2.85f, 0f, 7.45f, 3.55f, 1.85f, 8.15f, "npc_saleh"))
        // Parked Sedan Car on street (-6.5, 12.2)
        add(AABB(-8.6f, 0f, 11.2f, -4.4f, 1.6f, 13.2f, "parked_sedan"))
        // Streetlamp poles
        for (sx in floatArrayOf(-8.0f, 3.0f, 14.0f, 24.5f)) {
            add(AABB(sx - 0.18f, 0f, 10.35f, sx + 0.18f, 4.2f, 10.70f, "streetlamp_$sx"))
        }
        // Trees
        for ((tx, tz) in listOf(-8.5f to 5.8f, 8.8f to 5.5f, 14.5f to 5.2f)) {
            add(AABB(tx - 0.45f, 0f, tz - 0.45f, tx + 0.45f, 3.5f, tz + 0.45f, "tree_${tx}_$tz"))
        }
        // Neighboring house shell next door (X in [9.5, 16.5], Z in [-5.5, 2.5])
        add(AABB(9.5f, 0f, -5.5f, 16.5f, 4.0f, 2.5f, "neighbor_house"))

        // --- Al-Amal Convenience Store (X in [18.0, 26.5], Z in [0.5, 8.5]) ---
        // West wall
        add(AABB(17.85f, 0f, 0.35f, 18.15f, 3.2f, 8.65f, "store_west"))
        // East wall
        add(AABB(26.35f, 0f, 0.35f, 26.65f, 3.2f, 8.65f, "store_east"))
        // Back wall
        add(AABB(17.85f, 0f, 0.35f, 26.65f, 3.2f, 0.65f, "store_back"))
        // Storefront wall (Z = 8.5) with doorway gap X in [20.8, 22.2]
        add(AABB(17.85f, 0f, 8.35f, 20.8f, 3.2f, 8.65f, "store_front_l"))
        add(AABB(22.2f, 0f, 8.35f, 26.65f, 3.2f, 8.65f, "store_front_r"))
        // Store aisles & counter
        add(AABB(19.2f, 0f, 3.4f, 20.3f, 1.65f, 6.2f, "store_shelf_1"))
        add(AABB(22.0f, 0f, 3.4f, 23.1f, 1.65f, 6.2f, "store_shelf_2"))
        add(AABB(20.0f, 0f, 0.70f, 23.8f, 2.15f, 1.45f, "store_cooler"))
        add(AABB(24.3f, 0f, 2.4f, 25.1f, 1.15f, 5.2f, "store_counter"))
        // Cashier Karim behind counter
        add(AABB(25.25f, 0f, 3.45f, 25.95f, 1.85f, 4.15f, "npc_karim"))
    }

    val interactableTargets: List<InteractableTarget> = listOf(
        InteractableTarget(InteractableId.NEIGHBOR_SALEH, AABB(2.65f, 0.0f, 7.25f, 3.75f, 1.95f, 8.35f), 3.4f),
        InteractableTarget(InteractableId.FRONT_DOOR, AABB(-0.65f, 0.0f, 2.75f, 0.65f, 2.30f, 3.25f), 2.7f),
        InteractableTarget(InteractableId.BACK_DOOR, AABB(-0.60f, 0.0f, -7.25f, 0.60f, 2.30f, -6.75f), 2.7f),
        InteractableTarget(InteractableId.MOP_TOOL, AABB(-1.55f, 0.0f, -6.80f, -0.95f, 1.55f, -6.10f), 2.7f),
        InteractableTarget(InteractableId.MUD_SPOT_1, AABB(-0.45f, 0.0f, 1.05f, 0.45f, 0.45f, 1.95f), 2.5f),
        InteractableTarget(InteractableId.MUD_SPOT_2, AABB(-0.80f, 0.0f, -1.45f, 0.10f, 0.45f, -0.55f), 2.5f),
        InteractableTarget(InteractableId.MUD_SPOT_3, AABB(-1.80f, 0.0f, -3.65f, -0.90f, 0.45f, -2.75f), 2.5f),
        InteractableTarget(InteractableId.STORE_DOOR, AABB(20.8f, 0.0f, 8.25f, 22.2f, 2.35f, 8.75f), 2.8f),
        InteractableTarget(InteractableId.SUPPLIES_BASKET, AABB(19.9f, 0.55f, 4.35f, 20.6f, 1.45f, 5.25f), 2.7f),
        InteractableTarget(InteractableId.CASHIER_KARIM, AABB(24.3f, 0.5f, 3.1f, 26.1f, 1.95f, 4.5f), 3.3f),
        InteractableTarget(InteractableId.PORCH_FLYER, AABB(-0.55f, 0.0f, 3.35f, 0.25f, 0.45f, 4.05f), 2.6f),
        InteractableTarget(InteractableId.KITCHEN_TABLE_DROP, AABB(-2.85f, 0.5f, -4.95f, -1.65f, 1.15f, -3.75f), 2.9f),
        InteractableTarget(InteractableId.BREAKER_BOX, AABB(0.75f, 1.05f, -3.25f, 1.15f, 1.85f, -2.35f), 2.5f),
        InteractableTarget(InteractableId.BEDROOM_CLOSET, AABB(3.80f, 0.0f, -6.30f, 4.85f, 2.10f, -4.70f), 2.7f)
    )
}
