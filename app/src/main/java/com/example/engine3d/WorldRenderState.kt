package com.example.engine3d

import com.example.story.StoryStage

data class WorldRenderState(
    val stage: StoryStage = StoryStage.TALK_TO_NEIGHBOR,
    val frontDoorOpen: Boolean = false,
    val backDoorOpen: Boolean = false,
    val storeDoorOpen: Boolean = false,
    val mopPickedUp: Boolean = false,
    val mudCleaned: BooleanArray = BooleanArray(3),
    val suppliesPickedUp: Boolean = false,
    val suppliesPlaced: Boolean = false,
    val flyerPickedUp: Boolean = false,
    val strangerVisible: Boolean = false,
    val powerOutage: Boolean = false,
    val porchLightOff: Boolean = false,
    val flashlightOn: Boolean = false,
    val peepholeActive: Boolean = false,
    val hidingInCloset: Boolean = false,
    val psxJitterEnabled: Boolean = true,
    val lookSensitivity: Float = 1.0f,
    val inputFrozen: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WorldRenderState) return false
        return stage == other.stage &&
            frontDoorOpen == other.frontDoorOpen &&
            backDoorOpen == other.backDoorOpen &&
            storeDoorOpen == other.storeDoorOpen &&
            mopPickedUp == other.mopPickedUp &&
            mudCleaned.contentEquals(other.mudCleaned) &&
            suppliesPickedUp == other.suppliesPickedUp &&
            suppliesPlaced == other.suppliesPlaced &&
            flyerPickedUp == other.flyerPickedUp &&
            strangerVisible == other.strangerVisible &&
            powerOutage == other.powerOutage &&
            porchLightOff == other.porchLightOff &&
            flashlightOn == other.flashlightOn &&
            peepholeActive == other.peepholeActive &&
            hidingInCloset == other.hidingInCloset &&
            psxJitterEnabled == other.psxJitterEnabled &&
            lookSensitivity == other.lookSensitivity &&
            inputFrozen == other.inputFrozen
    }

    override fun hashCode(): Int = stage.hashCode()
}
