package com.example

import com.example.engine3d.Math3D
import com.example.engine3d.WorldBlueprint
import com.example.story.StoryScript
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryAndWorldTest {

    @Test
    fun `start position and key waypoints are collision free`() {
        val waypoints = listOf(
            "start" to (WorldBlueprint.START_X to WorldBlueprint.START_Z),
            "front_porch" to (0.0f to 4.2f),
            "living_foyer" to (0.0f to 1.2f),
            "kitchen_center" to (-2.2f to -2.6f),
            "hallway_breaker" to (0.0f to -2.8f),
            "bedroom_center" to (2.1f to -0.85f),
            "store_aisle" to (21.2f to 4.8f)
        )
        for ((name, pos) in waypoints) {
            val (px, pz) = pos
            val blocked = WorldBlueprint.staticColliders.any {
                it.intersectsCylinderXZ(px, pz, 0.28f, 0.05f, 1.75f)
            }
            assertFalse("Waypoint '$name' ($px, $pz) should not collide with static geometry", blocked)
        }
    }

    @Test
    fun `sliding collision prevents walking through exterior wall`() {
        // Attempt to push player through West House wall (X = -5.0)
        val (nx, _) = Math3D.resolveMovementXZ(
            currentX = -4.4f,
            currentZ = 0.5f,
            deltaX = -1.2f,
            deltaZ = 0.0f,
            radius = 0.30f,
            colliders = WorldBlueprint.staticColliders
        )
        assertTrue("Player should be stopped before West wall", nx >= -4.85f)
    }

    @Test
    fun `all dialogue branches terminate or link to valid nodes`() {
        val roots = listOf("saleh_1", "karim_1", "visitor_1")
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque(roots)

        while (queue.isNotEmpty()) {
            val id = queue.removeFirst()
            if (!visited.add(id)) continue
            val node = StoryScript.getDialogueNode(id)
            assertNotNull("Dialogue node $id must exist", node)
            for (choice in node!!.choices) {
                choice.nextNodeId?.let { nextId ->
                    queue.add(nextId)
                }
            }
        }
        assertTrue(visited.size >= 8)
    }
}
