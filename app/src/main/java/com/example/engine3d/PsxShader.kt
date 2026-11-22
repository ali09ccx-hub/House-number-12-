package com.example.engine3d

import android.opengl.GLES30

class PsxShader {
    var programId: Int = 0
        private set

    var uMvpLoc = -1
    var uModelLoc = -1
    var uTextureLoc = -1
    var uPsxSnapLoc = -1
    var uResolutionLoc = -1
    var uCamPosLoc = -1
    var uFogColorLoc = -1
    var uFogDensityLoc = -1
    var uAmbientColorLoc = -1
    var uLightCountLoc = -1
    var uLightPosLoc = -1
    var uLightColorLoc = -1
    var uLightRadiusLoc = -1
    var uFlashOnLoc = -1
    var uFlashDirLoc = -1
    var uEmissiveLoc = -1

    fun compile() {
        val vs = compileShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fs = compileShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vs)
        GLES30.glAttachShader(programId, fs)
        GLES30.glLinkProgram(programId)
        GLES30.glDeleteShader(vs)
        GLES30.glDeleteShader(fs)

        uMvpLoc = GLES30.glGetUniformLocation(programId, "uMvp")
        uModelLoc = GLES30.glGetUniformLocation(programId, "uModel")
        uTextureLoc = GLES30.glGetUniformLocation(programId, "uTexture")
        uPsxSnapLoc = GLES30.glGetUniformLocation(programId, "uPsxSnap")
        uResolutionLoc = GLES30.glGetUniformLocation(programId, "uResolution")
        uCamPosLoc = GLES30.glGetUniformLocation(programId, "uCamPos")
        uFogColorLoc = GLES30.glGetUniformLocation(programId, "uFogColor")
        uFogDensityLoc = GLES30.glGetUniformLocation(programId, "uFogDensity")
        uAmbientColorLoc = GLES30.glGetUniformLocation(programId, "uAmbientColor")
        uLightCountLoc = GLES30.glGetUniformLocation(programId, "uLightCount")
        uLightPosLoc = GLES30.glGetUniformLocation(programId, "uLightPos")
        uLightColorLoc = GLES30.glGetUniformLocation(programId, "uLightColor")
        uLightRadiusLoc = GLES30.glGetUniformLocation(programId, "uLightRadius")
        uFlashOnLoc = GLES30.glGetUniformLocation(programId, "uFlashOn")
        uFlashDirLoc = GLES30.glGetUniformLocation(programId, "uFlashDir")
        uEmissiveLoc = GLES30.glGetUniformLocation(programId, "uEmissive")
    }

    fun release() {
        if (programId != 0) GLES30.glDeleteProgram(programId)
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, src)
        GLES30.glCompileShader(shader)
        return shader
    }

    companion object {
        private const val VERTEX_SHADER = """#version 300 es
            layout(location = 0) in vec3 aPos;
            layout(location = 1) in vec3 aNormal;
            layout(location = 2) in vec2 aUv;
            layout(location = 3) in vec3 aColor;

            uniform mat4 uMvp;
            uniform mat4 uModel;
            uniform float uPsxSnap;
            uniform vec2 uResolution;

            out vec3 vWorldPos;
            out vec3 vNormal;
            out vec2 vUv;
            out vec3 vColor;

            void main() {
                vec4 clip = uMvp * vec4(aPos, 1.0);
                if (uPsxSnap > 0.5 && clip.w > 0.1) {
                    vec2 grid = uResolution * 0.35;
                    vec2 snapped = floor((clip.xy / clip.w) * grid + 0.5) / grid;
                    clip.xy = snapped * clip.w;
                }
                gl_Position = clip;
                vWorldPos = (uModel * vec4(aPos, 1.0)).xyz;
                vNormal = normalize(mat3(uModel) * aNormal);
                vUv = aUv;
                vColor = aColor;
            }
        """

        private const val FRAGMENT_SHADER = """#version 300 es
            precision mediump float;

            in vec3 vWorldPos;
            in vec3 vNormal;
            in vec2 vUv;
            in vec3 vColor;

            uniform sampler2D uTexture;
            uniform vec3 uCamPos;
            uniform vec3 uFogColor;
            uniform float uFogDensity;
            uniform vec3 uAmbientColor;
            uniform int uLightCount;
            uniform vec3 uLightPos[10];
            uniform vec3 uLightColor[10];
            uniform float uLightRadius[10];
            uniform float uFlashOn;
            uniform vec3 uFlashDir;
            uniform float uEmissive;

            out vec4 fragColor;

            void main() {
                vec4 tex = texture(uTexture, vUv);
                if (tex.a < 0.15) discard;

                vec3 albedo = tex.rgb * vColor;
                vec3 N = normalize(vNormal);

                // Cool moonlight hemisphere fill
                float hemi = clamp(N.y * 0.5 + 0.5, 0.25, 1.0);
                vec3 totalLight = uAmbientColor * hemi;

                // Dynamic point lights
                for (int i = 0; i < 10; i++) {
                    if (i >= uLightCount) break;
                    vec3 toLight = uLightPos[i] - vWorldPos;
                    float dist = length(toLight);
                    float rad = uLightRadius[i];
                    if (dist < rad) {
                        vec3 L = toLight / max(dist, 0.001);
                        float diff = max(dot(N, L), 0.12);
                        float atten = 1.0 - (dist / rad);
                        atten = atten * atten;
                        totalLight += uLightColor[i] * diff * atten;
                    }
                }

                // Player Flashlight cone
                if (uFlashOn > 0.5) {
                    vec3 toFrag = vWorldPos - uCamPos;
                    float dist = length(toFrag);
                    if (dist < 16.0) {
                        vec3 dir = toFrag / max(dist, 0.001);
                        float spot = dot(dir, normalize(uFlashDir));
                        float cone = smoothstep(0.84, 0.96, spot);
                        float atten = (1.0 - dist / 16.0);
                        float diff = max(dot(N, -dir), 0.15);
                        totalLight += vec3(1.0, 0.95, 0.84) * 1.45 * cone * atten * atten * diff;
                    }
                }

                vec3 lit = albedo * (totalLight + vec3(uEmissive));

                // Exponential night mist fog
                float camDist = length(vWorldPos - uCamPos);
                float fogFactor = clamp(exp(-pow(camDist * uFogDensity, 1.6)), 0.0, 1.0);
                vec3 finalRgb = mix(uFogColor, lit, fogFactor);

                // Subtle PSX color depth feeling
                fragColor = vec4(clamp(finalRgb, 0.0, 1.0), tex.a);
            }
        """
    }
}
