package com.example.engine3d

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.audio.SurfaceType
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GameGLSurfaceView(
    context: Context,
    private val onTargetChanged: (InteractableId?) -> Unit,
    private val onFootstep: (SurfaceType) -> Unit,
    private val onIndoorsChanged: (Boolean) -> Unit,
    private val onPlayerPositionChanged: (Float, Float) -> Unit
) : GLSurfaceView(context) {

    private val renderer = PsxWorldRenderer()

    init {
        setEGLContextClientVersion(3)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun updateRenderState(state: WorldRenderState) {
        renderer.renderState = state
    }

    fun setMoveInput(x: Float, y: Float) {
        renderer.moveX = x.coerceIn(-1f, 1f)
        renderer.moveY = y.coerceIn(-1f, 1f)
    }

    fun addLookDelta(dx: Float, dy: Float) {
        renderer.addLookDelta(dx, dy)
    }

    fun resetPlayerToStart() {
        renderer.resetPlayer()
    }

    fun teleportToBedroomHiding(hide: Boolean) {
        renderer.setHidingPose(hide)
    }

    private inner class PsxWorldRenderer : Renderer {
        @Volatile
        var renderState = WorldRenderState()

        @Volatile
        var moveX = 0f

        @Volatile
        var moveY = 0f

        private var playerX = WorldBlueprint.START_X
        private var playerZ = WorldBlueprint.START_Z
        private var yaw = WorldBlueprint.START_YAW
        private var pitch = -2f
        private var walkBobPhase = 0f
        private var stepAccum = 0f
        private var lastIndoors = false
        private var timeSec = 0f
        private var lastFrameNs = System.nanoTime()

        private var frontDoorAngle = 0f
        private var backDoorAngle = 0f
        private var storeDoorAngle = 0f

        private var widthPx = 1280
        private var heightPx = 720

        private val shader = PsxShader()
        private val textures = TextureAtlas()

        private var staticMeshes = emptyList<GpuMesh>()
        private var frontDoorMesh: GpuMesh? = null
        private var backDoorMesh: GpuMesh? = null
        private var storeDoorMesh: GpuMesh? = null
        private var mopMesh: GpuMesh? = null
        private var mudMesh: GpuMesh? = null
        private var suppliesMesh: GpuMesh? = null
        private var flyerMesh: GpuMesh? = null
        private var salehMesh: GpuMesh? = null
        private var karimMesh: GpuMesh? = null
        private var strangerMesh: GpuMesh? = null

        private val projMatrix = FloatArray(16)
        private val viewMatrix = FloatArray(16)
        private val vpMatrix = FloatArray(16)
        private val modelMatrix = FloatArray(16)
        private val mvpMatrix = FloatArray(16)

        private var lastReportedTarget: InteractableId? = null
        private var frameCounter = 0

        fun addLookDelta(dx: Float, dy: Float) {
            val st = renderState
            if (st.inputFrozen || st.peepholeActive) return
            val sens = 0.17f * st.lookSensitivity
            yaw = (yaw + dx * sens) % 360f
            pitch = (pitch - dy * sens).coerceIn(-68f, 68f)
        }

        fun resetPlayer() {
            playerX = WorldBlueprint.START_X
            playerZ = WorldBlueprint.START_Z
            yaw = WorldBlueprint.START_YAW
            pitch = -2f
            frontDoorAngle = 0f
            backDoorAngle = 0f
            storeDoorAngle = 0f
        }

        fun setHidingPose(hide: Boolean) {
            if (hide) {
                playerX = 3.55f
                playerZ = -5.45f
                yaw = -90f
                pitch = 0f
            }
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES30.glClearColor(0.03f, 0.04f, 0.06f, 1f)
            GLES30.glEnable(GLES30.GL_DEPTH_TEST)
            GLES30.glEnable(GLES30.GL_CULL_FACE)
            GLES30.glCullFace(GLES30.GL_BACK)

            shader.compile()
            textures.initAll()

            val batch = MeshBatcher()
            ArchitectureBuilder.buildWorldArchitecture(batch)
            FurnitureAndNpcBuilder.buildStaticPropsAndFurniture(batch)
            staticMeshes = batch.buildAll()

            frontDoorMesh = FurnitureAndNpcBuilder.buildDoorMesh(1.30f, 2.25f)
            backDoorMesh = FurnitureAndNpcBuilder.buildDoorMesh(1.20f, 2.25f)
            storeDoorMesh = FurnitureAndNpcBuilder.buildDoorMesh(1.40f, 2.30f)
            mopMesh = FurnitureAndNpcBuilder.buildMopMesh()
            mudMesh = FurnitureAndNpcBuilder.buildMudSpotMesh()
            suppliesMesh = FurnitureAndNpcBuilder.buildSuppliesBasketMesh()
            flyerMesh = FurnitureAndNpcBuilder.buildFlyerMesh()

            salehMesh = FurnitureAndNpcBuilder.buildNpcMesh(
                coatR = 0.44f, coatG = 0.36f, coatB = 0.28f,
                pantsR = 0.24f, pantsG = 0.26f, pantsB = 0.30f,
                skinR = 0.82f, skinG = 0.66f, skinB = 0.54f,
                hasCap = false, hasLantern = true, hasClipboard = false
            )
            karimMesh = FurnitureAndNpcBuilder.buildNpcMesh(
                coatR = 0.22f, coatG = 0.44f, coatB = 0.56f,
                pantsR = 0.18f, pantsG = 0.20f, pantsB = 0.24f,
                skinR = 0.80f, skinG = 0.64f, skinB = 0.52f,
                hasCap = true, hasLantern = false, hasClipboard = false
            )
            strangerMesh = FurnitureAndNpcBuilder.buildNpcMesh(
                coatR = 0.14f, coatG = 0.15f, coatB = 0.18f,
                pantsR = 0.11f, pantsG = 0.12f, pantsB = 0.14f,
                skinR = 0.72f, skinG = 0.60f, skinB = 0.52f,
                hasCap = true, hasLantern = false, hasClipboard = true
            )
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            widthPx = width.coerceAtLeast(1)
            heightPx = height.coerceAtLeast(1)
            GLES30.glViewport(0, 0, widthPx, heightPx)
        }

        override fun onDrawFrame(gl: GL10?) {
            val now = System.nanoTime()
            val dt = ((now - lastFrameNs) / 1_000_000_000.0).toFloat().coerceIn(0.001f, 0.05f)
            lastFrameNs = now
            timeSec += dt

            val st = renderState
            updatePhysics(dt, st)

            val aspect = widthPx.toFloat() / heightPx.toFloat()
            val fov = if (st.peepholeActive) 40f else 66f
            Matrix.perspectiveM(projMatrix, 0, fov, aspect, 0.08f, 65f)

            val camX: Float
            val camY: Float
            val camZ: Float
            val lookYaw: Float
            val lookPitch: Float

            if (st.peepholeActive) {
                camX = 0.0f
                camY = 1.56f
                camZ = 3.16f
                lookYaw = 180f
                lookPitch = -5f
            } else {
                camX = playerX
                camY = 1.60f + sin(walkBobPhase) * 0.032f
                camZ = playerZ
                lookYaw = yaw
                lookPitch = pitch
            }

            val fwd = Math3D.yawPitchToForward(lookYaw, lookPitch)
            Matrix.setLookAtM(
                viewMatrix, 0,
                camX, camY, camZ,
                camX + fwd[0], camY + fwd[1], camZ + fwd[2],
                0f, 1f, 0f
            )
            Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

            // Raycast interactable target
            if (frameCounter % 3 == 0) {
                val hit = findBestTarget(camX, camY, camZ, fwd, st)
                if (hit != lastReportedTarget) {
                    lastReportedTarget = hit
                    onTargetChanged(hit)
                }
                onPlayerPositionChanged(playerX, playerZ)
            }
            frameCounter++

            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
            GLES30.glUseProgram(shader.programId)

            setupUniforms(st, camX, camY, camZ, fwd)

            // Draw Static Batched World
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            for (mesh in staticMeshes) {
                mesh.draw(shader, textures, modelMatrix, mvpMatrix)
            }

            // Draw Dynamic Doors
            drawDoor(frontDoorMesh, -0.65f, 0f, 3.00f, frontDoorAngle)
            drawDoor(backDoorMesh, -0.60f, 0f, -7.00f, backDoorAngle)
            drawDoor(storeDoorMesh, 20.8f, 0f, 8.50f, storeDoorAngle)

            // Draw Mop (in kitchen corner or hidden if picked up)
            if (!st.mopPickedUp) {
                drawAt(mopMesh, -1.25f, 0f, -6.45f, 0f)
            }

            // Draw 3 Mud Spots
            WorldBlueprint.mudSpots.forEachIndexed { idx, (_, mx, mz) ->
                if (!st.mudCleaned[idx]) {
                    drawAt(mudMesh, mx, 0f, mz, idx * 45f)
                }
            }

            // Draw Supplies Basket (in store shelf until picked, or on dining table once placed)
            if (!st.suppliesPickedUp) {
                drawAt(suppliesMesh, 20.25f, 0.92f, 4.80f, 15f)
            } else if (st.suppliesPlaced) {
                drawAt(suppliesMesh, -2.25f, 0.79f, -4.35f, -20f)
            }

            // Draw Porch Flyer (appears when returning home)
            if (st.stage.ordinal >= com.example.story.StoryStage.RETURN_HOME.ordinal && !st.flyerPickedUp) {
                drawAt(flyerMesh, -0.15f, 0.16f, 3.70f, 18f)
            }

            // Draw NPCs
            // Mr. Saleh near eastern garden fence, subtly turns toward player
            val salehYaw = Math.toDegrees(atan2((playerX - 3.2f).toDouble(), (playerZ - 7.8f).toDouble())).toFloat()
            drawAt(salehMesh, 3.2f, 0f, 7.8f, salehYaw)

            // Karim behind store counter facing West (-X)
            drawAt(karimMesh, 25.60f, 0f, 3.80f, -90f)

            // Stranger outside porch door when visible (peephole / knocking chapter)
            if (st.strangerVisible) {
                val sway = sin(timeSec * 1.4f) * 2.0f
                drawAt(strangerMesh, 0.05f, 0.16f, 4.38f, 180f + sway)
            }
        }

        private fun updatePhysics(dt: Float, st: WorldRenderState) {
            // Smooth door hinges
            frontDoorAngle += ((if (st.frontDoorOpen) -86f else 0f) - frontDoorAngle) * (dt * 8f).coerceAtMost(1f)
            backDoorAngle += ((if (st.backDoorOpen) 86f else 0f) - backDoorAngle) * (dt * 8f).coerceAtMost(1f)
            storeDoorAngle += ((if (st.storeDoorOpen) -86f else 0f) - storeDoorAngle) * (dt * 8f).coerceAtMost(1f)

            if (st.inputFrozen || st.peepholeActive || st.hidingInCloset) {
                walkBobPhase = 0f
                return
            }

            val mag = sqrt(moveX * moveX + moveY * moveY)
            if (mag > 0.08f) {
                val yawRad = Math.toRadians(yaw.toDouble())
                val fwdX = sin(yawRad).toFloat()
                val fwdZ = -cos(yawRad).toFloat()
                val rightX = cos(yawRad).toFloat()
                val rightZ = sin(yawRad).toFloat()

                val speed = 3.15f * mag.coerceAtMost(1f)
                // moveY > 0 means push stick UP (forward)
                val vx = (fwdX * moveY + rightX * moveX) * speed * dt
                val vz = (fwdZ * moveY + rightZ * moveX) * speed * dt

                val dynamicColliders = ArrayList<AABB>(WorldBlueprint.staticColliders.size + 4)
                dynamicColliders.addAll(WorldBlueprint.staticColliders)
                if (!st.frontDoorOpen) dynamicColliders.add(AABB(-0.65f, 0f, 2.84f, 0.65f, 2.3f, 3.14f))
                if (!st.backDoorOpen) dynamicColliders.add(AABB(-0.60f, 0f, -7.14f, 0.60f, 2.3f, -6.84f))
                if (!st.storeDoorOpen) dynamicColliders.add(AABB(20.8f, 0f, 8.36f, 22.2f, 2.3f, 8.64f))

                val (nx, nz) = Math3D.resolveMovementXZ(playerX, playerZ, vx, vz, 0.30f, dynamicColliders)
                val moved = sqrt((nx - playerX) * (nx - playerX) + (nz - playerZ) * (nz - playerZ))
                playerX = nx
                playerZ = nz

                if (moved > 0.002f) {
                    walkBobPhase += dt * (7.5f + mag * 3.5f)
                    stepAccum += moved
                    if (stepAccum >= 1.35f) {
                        stepAccum = 0f
                        onFootstep(detectSurface(playerX, playerZ))
                    }
                }
            }

            val inHouse = playerX in WorldBlueprint.HOUSE_MIN_X..WorldBlueprint.HOUSE_MAX_X &&
                playerZ in WorldBlueprint.HOUSE_MIN_Z..WorldBlueprint.HOUSE_MAX_Z
            val inStore = playerX in WorldBlueprint.STORE_MIN_X..WorldBlueprint.STORE_MAX_X &&
                playerZ in WorldBlueprint.STORE_MIN_Z..WorldBlueprint.STORE_MAX_Z
            val indoors = inHouse || inStore
            if (indoors != lastIndoors) {
                lastIndoors = indoors
                onIndoorsChanged(indoors)
            }
        }

        private fun detectSurface(x: Float, z: Float): SurfaceType {
            val inKitchen = x in -5.0f..-0.8f && z in -7.0f..-2.0f
            val inStore = x in WorldBlueprint.STORE_MIN_X..WorldBlueprint.STORE_MAX_X &&
                z in WorldBlueprint.STORE_MIN_Z..WorldBlueprint.STORE_MAX_Z
            if (inKitchen || inStore) return SurfaceType.TILE
            val inHouse = x in WorldBlueprint.HOUSE_MIN_X..WorldBlueprint.HOUSE_MAX_X &&
                z in WorldBlueprint.HOUSE_MIN_Z..WorldBlueprint.HOUSE_MAX_Z
            return if (inHouse) SurfaceType.WOOD else SurfaceType.ASPHALT
        }

        private fun findBestTarget(
            ox: Float, oy: Float, oz: Float,
            fwd: FloatArray,
            st: WorldRenderState
        ): InteractableId? {
            if (st.peepholeActive || st.hidingInCloset) return null
            var bestId: InteractableId? = null
            var bestDist = Float.MAX_VALUE
            for (target in WorldBlueprint.interactableTargets) {
                if (target.id == InteractableId.MOP_TOOL && st.mopPickedUp) continue
                if (target.id == InteractableId.MUD_SPOT_1 && st.mudCleaned[0]) continue
                if (target.id == InteractableId.MUD_SPOT_2 && st.mudCleaned[1]) continue
                if (target.id == InteractableId.MUD_SPOT_3 && st.mudCleaned[2]) continue
                if (target.id == InteractableId.SUPPLIES_BASKET && st.suppliesPickedUp) continue
                if (target.id == InteractableId.PORCH_FLYER &&
                    (st.flyerPickedUp || st.stage.ordinal < com.example.story.StoryStage.RETURN_HOME.ordinal)
                ) continue

                val hitDist = target.bounds.intersectRay(ox, oy, oz, fwd[0], fwd[1], fwd[2], target.maxRange)
                if (hitDist != null && hitDist < bestDist) {
                    bestDist = hitDist
                    bestId = target.id
                }
            }
            return bestId
        }

        private fun setupUniforms(st: WorldRenderState, camX: Float, camY: Float, camZ: Float, fwd: FloatArray) {
            GLES30.glUniform1f(shader.uPsxSnapLoc, if (st.psxJitterEnabled) 1f else 0f)
            GLES30.glUniform2f(shader.uResolutionLoc, widthPx.toFloat(), heightPx.toFloat())
            GLES30.glUniform3f(shader.uCamPosLoc, camX, camY, camZ)
            GLES30.glUniform3f(shader.uFogColorLoc, 0.04f, 0.055f, 0.085f)
            GLES30.glUniform1f(shader.uFogDensityLoc, if (st.powerOutage) 0.082f else 0.048f)
            GLES30.glUniform3f(shader.uAmbientColorLoc, 0.16f, 0.19f, 0.28f)

            val flicker = if (st.stage.ordinal >= com.example.story.StoryStage.RETURN_HOME.ordinal) {
                if ((sin(timeSec * 19.0) + cos(timeSec * 31.0)) > 0.4) 0.25f else 0.05f
            } else 1.0f

            val houseOn = !st.powerOutage
            val porchOn = !st.porchLightOff

            val lightPos = floatArrayOf(
                // 0: Porch warm lamp
                0.9f, 2.2f, 3.8f,
                // 1: Living room warm ceiling
                -2.6f, 2.75f, 0.5f,
                // 2: Kitchen warm ceiling
                -2.6f, 2.75f, -4.6f,
                // 3: Bedroom nightstand lamp
                4.2f, 1.4f, -0.35f,
                // 4: Streetlamp West
                -8.0f, 3.7f, 11.1f,
                // 5: Streetlamp House #12 (flickers on return!)
                3.0f, 3.7f, 11.1f,
                // 6: Streetlamp Mid
                14.0f, 3.7f, 11.1f,
                // 7: Streetlamp Store
                24.5f, 3.7f, 11.1f,
                // 8: Al-Amal Store fluorescent 1
                21.2f, 2.85f, 4.2f,
                // 9: Al-Amal Store fluorescent 2
                24.4f, 2.85f, 4.2f
            )

            val porchStr = if (porchOn) 1.0f else 0.0f
            val houseStr = if (houseOn) 1.0f else 0.0f

            val lightColor = floatArrayOf(
                1.05f * porchStr, 0.78f * porchStr, 0.42f * porchStr,
                0.95f * houseStr, 0.76f * houseStr, 0.48f * houseStr,
                0.98f * houseStr, 0.85f * houseStr, 0.65f * houseStr,
                0.90f * houseStr, 0.68f * houseStr, 0.36f * houseStr,
                0.95f, 0.72f, 0.40f,
                0.95f * flicker, 0.72f * flicker, 0.40f * flicker,
                0.95f, 0.72f, 0.40f,
                0.95f, 0.76f, 0.46f,
                0.92f, 0.98f, 1.05f,
                0.92f, 0.98f, 1.05f
            )

            val lightRadius = floatArrayOf(
                8.5f, 7.2f, 7.2f, 6.0f,
                11.0f, 11.0f, 11.0f, 11.0f,
                9.5f, 8.5f
            )

            GLES30.glUniform1i(shader.uLightCountLoc, 10)
            GLES30.glUniform3fv(shader.uLightPosLoc, 10, lightPos, 0)
            GLES30.glUniform3fv(shader.uLightColorLoc, 10, lightColor, 0)
            GLES30.glUniform1fv(shader.uLightRadiusLoc, 10, lightRadius, 0)

            GLES30.glUniform1f(shader.uFlashOnLoc, if (st.flashlightOn) 1f else 0f)
            GLES30.glUniform3f(shader.uFlashDirLoc, fwd[0], fwd[1], fwd[2])
        }

        private fun drawDoor(mesh: GpuMesh?, hx: Float, hy: Float, hz: Float, angleDeg: Float) {
            mesh ?: return
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, hx, hy, hz)
            Matrix.rotateM(modelMatrix, 0, angleDeg, 0f, 1f, 0f)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mesh.draw(shader, textures, modelMatrix, mvpMatrix)
        }

        private fun drawAt(mesh: GpuMesh?, x: Float, y: Float, z: Float, rotYDeg: Float) {
            mesh ?: return
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, x, y, z)
            if (rotYDeg != 0f) Matrix.rotateM(modelMatrix, 0, rotYDeg, 0f, 1f, 0f)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mesh.draw(shader, textures, modelMatrix, mvpMatrix)
        }
    }
}
