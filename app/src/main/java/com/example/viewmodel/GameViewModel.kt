package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HorrorAudioEngine
import com.example.audio.SurfaceType
import com.example.data.GamePreferences
import com.example.data.GameSettings
import com.example.engine3d.InteractableId
import com.example.engine3d.WorldBlueprint
import com.example.engine3d.WorldRenderState
import com.example.story.DialogueChoice
import com.example.story.DialogueNode
import com.example.story.EndingType
import com.example.story.SmsMessage
import com.example.story.StoryScript
import com.example.story.StoryStage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    TITLE,
    PLAYING,
    ENDING
}

data class GameUiState(
    val screen: AppScreen = AppScreen.TITLE,
    val stage: StoryStage = StoryStage.TALK_TO_NEIGHBOR,
    val renderState: WorldRenderState = WorldRenderState(),
    val currentTarget: InteractableId? = null,
    val interactPrompt: String? = null,
    val activeDialogue: DialogueNode? = null,
    val toastSubtitle: String? = null,
    val isPhoneOpen: Boolean = false,
    val unreadMessages: Int = 1,
    val messages: List<SmsMessage> = StoryScript.initialMessages,
    val hasMop: Boolean = false,
    val cleanedCount: Int = 0,
    val hasSupplies: Boolean = false,
    val hasBatteries: Boolean = false,
    val frontDoorLocked: Boolean = false,
    val backDoorLocked: Boolean = false,
    val breakerRestored: Boolean = false,
    val policeCalled: Boolean = false,
    val policeEtaSeconds: Int = 0,
    val toldKarimAlone: Boolean = false,
    val tensionLevel: Float = 0.05f,
    val isPaused: Boolean = false,
    val currentEnding: EndingType? = null,
    val settings: GameSettings = GameSettings()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = GamePreferences(application.applicationContext)
    val audioEngine = HorrorAudioEngine()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var resetWorldCallback: (() -> Unit)? = null
    private var closetPoseCallback: ((Boolean) -> Unit)? = null

    init {
        viewModelScope.launch {
            prefs.settingsFlow.collect { settings ->
                audioEngine.masterVolume = settings.masterVolume
                _uiState.update { st ->
                    st.copy(
                        settings = settings,
                        renderState = st.renderState.copy(
                            psxJitterEnabled = settings.psxJitterEnabled,
                            lookSensitivity = settings.lookSensitivity
                        )
                    )
                }
            }
        }
    }

    fun attachSurfaceCallbacks(onReset: () -> Unit, onCloset: (Boolean) -> Unit) {
        resetWorldCallback = onReset
        closetPoseCallback = onCloset
    }

    fun startAudio() {
        audioEngine.start()
    }

    fun stopAudio() {
        audioEngine.stop()
    }

    fun startNewGame() {
        audioEngine.playUiClick()
        audioEngine.tensionLevel = 0.05f
        resetWorldCallback?.invoke()
        val s = _uiState.value.settings
        _uiState.value = GameUiState(
            screen = AppScreen.PLAYING,
            stage = StoryStage.TALK_TO_NEIGHBOR,
            renderState = WorldRenderState(
                stage = StoryStage.TALK_TO_NEIGHBOR,
                psxJitterEnabled = s.psxJitterEnabled,
                lookSensitivity = s.lookSensitivity
            ),
            messages = StoryScript.initialMessages,
            unreadMessages = 1,
            settings = s,
            toastSubtitle = "تحدّث أولاً مع جارك الأستاذ صالح الواقف قرب السياج."
        )
        scheduleClearSubtitle(4500L)
    }

    fun returnToTitle() {
        audioEngine.playUiClick()
        audioEngine.tensionLevel = 0.05f
        _uiState.update { it.copy(screen = AppScreen.TITLE, isPaused = false, isPhoneOpen = false) }
    }

    fun togglePause() {
        audioEngine.playUiClick()
        _uiState.update { st ->
            val paused = !st.isPaused
            st.copy(
                isPaused = paused,
                renderState = st.renderState.copy(inputFrozen = paused || st.activeDialogue != null || st.isPhoneOpen)
            )
        }
    }

    fun togglePhone() {
        audioEngine.playUiClick()
        _uiState.update { st ->
            val opening = !st.isPhoneOpen
            val nextStage = if (opening && st.stage == StoryStage.CHECK_PHONE_AT_HOME) {
                StoryStage.GO_TO_STORE
            } else {
                st.stage
            }
            val newSub = if (opening && st.stage == StoryStage.CHECK_PHONE_AT_HOME) {
                "اخرج من المنزل وسِر شرقاً على الرصيف نحو «بقالة الأمل»."
            } else st.toastSubtitle

            st.copy(
                isPhoneOpen = opening,
                unreadMessages = if (opening) 0 else st.unreadMessages,
                stage = nextStage,
                toastSubtitle = newSub,
                renderState = st.renderState.copy(
                    stage = nextStage,
                    inputFrozen = opening || st.isPaused || st.activeDialogue != null
                )
            )
        }
    }

    fun toggleFlashlight() {
        val st = _uiState.value
        if (!st.hasBatteries) {
            showSubtitle("بطاريات المصباح فارغة! تحتاج إلى شراء بطاريات جديدة من البقالة.")
            return
        }
        audioEngine.playUiClick()
        _uiState.update { cur ->
            cur.copy(renderState = cur.renderState.copy(flashlightOn = !cur.renderState.flashlightOn))
        }
    }

    fun onFootstep(surface: SurfaceType) {
        audioEngine.playFootstep(surface)
    }

    fun onIndoorsChanged(indoors: Boolean) {
        audioEngine.isIndoors = indoors
    }

    fun onTargetChanged(target: InteractableId?) {
        _uiState.update { st ->
            st.copy(
                currentTarget = target,
                interactPrompt = computePrompt(target, st)
            )
        }
    }

    fun onPlayerPositionChanged(x: Float, z: Float) {
        val st = _uiState.value
        if (st.screen != AppScreen.PLAYING) return

        // Entering the convenience store
        if (st.stage == StoryStage.GO_TO_STORE &&
            x in (WorldBlueprint.STORE_MIN_X + 0.3f)..(WorldBlueprint.STORE_MAX_X - 0.3f) &&
            z in (WorldBlueprint.STORE_MIN_Z + 0.3f)..(WorldBlueprint.STORE_MAX_Z - 0.4f)
        ) {
            _uiState.update { cur ->
                cur.copy(
                    stage = StoryStage.PICK_UP_SUPPLIES,
                    renderState = cur.renderState.copy(stage = StoryStage.PICK_UP_SUPPLIES),
                    toastSubtitle = "التقط سلة البطاريات والمؤونة من الرف الأيمن."
                )
            }
            scheduleClearSubtitle(4000L)
        }

        // Entering House No. 12 on return
        if (st.stage == StoryStage.RETURN_HOME &&
            x in (WorldBlueprint.HOUSE_MIN_X + 0.3f)..(WorldBlueprint.HOUSE_MAX_X - 0.3f) &&
            z in (WorldBlueprint.HOUSE_MIN_Z + 0.3f)..(WorldBlueprint.HOUSE_MAX_Z - 0.5f)
        ) {
            audioEngine.playDoorCreak()
            _uiState.update { cur ->
                cur.copy(
                    stage = StoryStage.PLACE_SUPPLIES,
                    frontDoorLocked = true,
                    renderState = cur.renderState.copy(
                        stage = StoryStage.PLACE_SUPPLIES,
                        frontDoorOpen = false
                    ),
                    toastSubtitle = "أغلقتُ الباب خلفي... ضع المشتريات على طاولة المطبخ."
                )
            }
            scheduleClearSubtitle(4500L)
        }
    }

    fun interactWithCurrentTarget() {
        val st = _uiState.value
        val target = st.currentTarget ?: return
        if (st.activeDialogue != null || st.isPhoneOpen || st.isPaused) return

        when (target) {
            InteractableId.NEIGHBOR_SALEH -> {
                if (st.stage == StoryStage.TALK_TO_NEIGHBOR) {
                    openDialogue("saleh_1")
                } else {
                    showSubtitle("الأستاذ صالح: تصبح على خير يا بني، لا تنسَ إقفال الباب جيداً.")
                }
            }

            InteractableId.FRONT_DOOR -> handleFrontDoor(st)
            InteractableId.BACK_DOOR -> handleBackDoor(st)
            InteractableId.STORE_DOOR -> handleStoreDoor(st)

            InteractableId.MOP_TOOL -> {
                if (st.stage == StoryStage.PICK_UP_MOP) {
                    audioEngine.playUiClick()
                    _uiState.update { cur ->
                        cur.copy(
                            hasMop = true,
                            stage = StoryStage.CLEAN_FLOOR,
                            renderState = cur.renderState.copy(stage = StoryStage.CLEAN_FLOOR, mopPickedUp = true),
                            toastSubtitle = "نظّف بقع الوحل الثلاث في الممر والمطبخ."
                        )
                    }
                    scheduleClearSubtitle(4000L)
                } else {
                    showSubtitle("ممسحة تنظيف عادية.")
                }
            }

            InteractableId.MUD_SPOT_1, InteractableId.MUD_SPOT_2, InteractableId.MUD_SPOT_3 -> {
                if (st.stage == StoryStage.CLEAN_FLOOR && st.hasMop) {
                    val idx = when (target) {
                        InteractableId.MUD_SPOT_1 -> 0
                        InteractableId.MUD_SPOT_2 -> 1
                        else -> 2
                    }
                    if (!st.renderState.mudCleaned[idx]) {
                        audioEngine.playMopScrub()
                        val updatedMud = st.renderState.mudCleaned.copyOf()
                        updatedMud[idx] = true
                        val count = updatedMud.count { it }
                        if (count >= 3) {
                            audioEngine.playPhoneChime()
                            val updatedMsgs = st.messages + StoryScript.homeFollowUpMessages
                            _uiState.update { cur ->
                                cur.copy(
                                    cleanedCount = 3,
                                    stage = StoryStage.CHECK_PHONE_AT_HOME,
                                    messages = updatedMsgs,
                                    unreadMessages = cur.unreadMessages + 2,
                                    renderState = cur.renderState.copy(
                                        stage = StoryStage.CHECK_PHONE_AT_HOME,
                                        mudCleaned = updatedMud
                                    ),
                                    toastSubtitle = "انتهيتَ من التنظيف! وصلتْ رسائل جديدة على هاتفك."
                                )
                            }
                            scheduleClearSubtitle(4500L)
                        } else {
                            _uiState.update { cur ->
                                cur.copy(
                                    cleanedCount = count,
                                    renderState = cur.renderState.copy(mudCleaned = updatedMud),
                                    toastSubtitle = "تم تنظيف البقعة ($count/3)."
                                )
                            }
                            scheduleClearSubtitle(2200L)
                        }
                    }
                } else if (st.stage.ordinal < StoryStage.CLEAN_FLOOR.ordinal) {
                    showSubtitle("آثار وحل على الأرضية... أحتاج إلى التقاط الممسحة من المطبخ أولاً.")
                }
            }

            InteractableId.SUPPLIES_BASKET -> {
                if (st.stage == StoryStage.PICK_UP_SUPPLIES) {
                    audioEngine.playUiClick()
                    _uiState.update { cur ->
                        cur.copy(
                            hasSupplies = true,
                            stage = StoryStage.PAY_CASHIER,
                            renderState = cur.renderState.copy(stage = StoryStage.PAY_CASHIER, suppliesPickedUp = true),
                            toastSubtitle = "أخذتَ البطاريات والماء. توجّه إلى البائع كريم لدفع الحساب."
                        )
                    }
                    scheduleClearSubtitle(4000L)
                }
            }

            InteractableId.CASHIER_KARIM -> {
                if (st.stage == StoryStage.PAY_CASHIER) {
                    openDialogue("karim_1")
                } else if (st.stage == StoryStage.PICK_UP_SUPPLIES) {
                    showSubtitle("كريم: خذ البطاريات والماء من الرف المقابل أولاً يا أخي.")
                } else {
                    showSubtitle("كريم: انتبه لنفسك في طريق العودة.")
                }
            }

            InteractableId.PORCH_FLYER -> {
                audioEngine.playUiClick()
                _uiState.update { cur ->
                    cur.copy(renderState = cur.renderState.copy(flyerPickedUp = true))
                }
                showSubtitle("ورقة مطوية عند العتبة: «تفتيش بلدي طارئ — المنزل ١٢»... غريب، لم يضعها أحد في الصباح.")
            }

            InteractableId.KITCHEN_TABLE_DROP -> {
                if (st.stage == StoryStage.PLACE_SUPPLIES) {
                    audioEngine.playUiClick()
                    _uiState.update { cur ->
                        cur.copy(
                            hasBatteries = true,
                            stage = StoryStage.CHECK_PEEPHOLE,
                            tensionLevel = 0.55f,
                            renderState = cur.renderState.copy(
                                stage = StoryStage.CHECK_PEEPHOLE,
                                suppliesPlaced = true,
                                strangerVisible = true
                            ),
                            toastSubtitle = "ركّبتَ البطاريات... فجأة، طرقٌ حاد على الباب الأمامي!"
                        )
                    }
                    audioEngine.tensionLevel = 0.55f
                    viewModelScope.launch {
                        delay(450L)
                        audioEngine.playDoorKnock()
                        delay(800L)
                        audioEngine.playTensionStinger()
                    }
                    scheduleClearSubtitle(5500L)
                }
            }

            InteractableId.BREAKER_BOX -> {
                if (st.stage == StoryStage.RESTORE_BREAKER_AND_LOCK && !st.breakerRestored) {
                    audioEngine.playBreakerClack()
                    _uiState.update { cur ->
                        val nextPower = false
                        val bothDone = cur.backDoorLocked
                        val nextStage = if (bothDone) StoryStage.CALL_POLICE_AND_SURVIVE else cur.stage
                        cur.copy(
                            breakerRestored = true,
                            stage = nextStage,
                            renderState = cur.renderState.copy(
                                powerOutage = nextPower,
                                stage = nextStage
                            ),
                            toastSubtitle = if (bothDone) {
                                "عاد التيار! اتصل بالنجدة ٩٩٩ فوراً عبر هاتفك!"
                            } else {
                                "عاد التيار! فجأة تسمع صوت مقبض الباب الخلفي يُحرَّك ببطء!"
                            }
                        )
                    }
                    if (!_uiState.value.backDoorLocked) {
                        viewModelScope.launch {
                            delay(550L)
                            audioEngine.playHandleRattle()
                        }
                    }
                    scheduleClearSubtitle(5000L)
                } else {
                    showSubtitle("لوحة قواطع الكهرباء تعمل بانتظام.")
                }
            }

            InteractableId.BEDROOM_CLOSET -> {
                if (st.stage == StoryStage.CALL_POLICE_AND_SURVIVE && st.policeCalled) {
                    audioEngine.playDoorCreak()
                    closetPoseCallback?.invoke(true)
                    _uiState.update { cur ->
                        cur.copy(renderState = cur.renderState.copy(hidingInCloset = true))
                    }
                    showSubtitle("اختبأتَ داخل الخزانة الخشبية بهدوء... صفارات الدورية تقترب في الشارع!")
                } else {
                    showSubtitle("خزانة الملابس الخشبية.")
                }
            }
        }
    }

    private fun handleFrontDoor(st: GameUiState) {
        when (st.stage) {
            StoryStage.TALK_TO_NEIGHBOR -> {
                showSubtitle("ينبغي أن أُحيّي جاري الأستاذ صالح عند السياج أولاً.")
            }

            StoryStage.ENTER_HOUSE -> {
                audioEngine.playDoorCreak()
                _uiState.update { cur ->
                    cur.copy(
                        stage = StoryStage.PICK_UP_MOP,
                        renderState = cur.renderState.copy(
                            stage = StoryStage.PICK_UP_MOP,
                            frontDoorOpen = true
                        ),
                        toastSubtitle = "ما هذه الآثار الطينية؟ التقط الممسحة من زاوية المطبخ."
                    )
                }
                scheduleClearSubtitle(4500L)
            }

            StoryStage.CHECK_PEEPHOLE -> {
                audioEngine.playHeartbeat()
                _uiState.update { cur ->
                    cur.copy(
                        renderState = cur.renderState.copy(peepholeActive = true, inputFrozen = true)
                    )
                }
                openDialogue("visitor_1")
            }

            StoryStage.RESTORE_BREAKER_AND_LOCK, StoryStage.CALL_POLICE_AND_SURVIVE -> {
                if (!st.frontDoorLocked) {
                    audioEngine.playDoorLock()
                    _uiState.update { cur ->
                        cur.copy(
                            frontDoorLocked = true,
                            renderState = cur.renderState.copy(frontDoorOpen = false),
                            toastSubtitle = "أحكمتَ إقفال مزلاج الباب الأمامي."
                        )
                    }
                } else {
                    showSubtitle("الباب الأمامي مقفل بإحكام بالمزلاج الحديدي.")
                }
            }

            else -> {
                if (st.stage.ordinal >= StoryStage.GO_TO_STORE.ordinal || st.stage.ordinal >= StoryStage.PICK_UP_MOP.ordinal) {
                    audioEngine.playDoorCreak()
                    _uiState.update { cur ->
                        cur.copy(renderState = cur.renderState.copy(frontDoorOpen = !cur.renderState.frontDoorOpen))
                    }
                }
            }
        }
    }

    private fun handleBackDoor(st: GameUiState) {
        if (st.stage == StoryStage.RESTORE_BREAKER_AND_LOCK) {
            if (!st.backDoorLocked) {
                audioEngine.playDoorLock()
                _uiState.update { cur ->
                    val bothDone = cur.breakerRestored
                    val nextStage = if (bothDone) StoryStage.CALL_POLICE_AND_SURVIVE else cur.stage
                    cur.copy(
                        backDoorLocked = true,
                        stage = nextStage,
                        renderState = cur.renderState.copy(
                            backDoorOpen = false,
                            stage = nextStage
                        ),
                        toastSubtitle = if (bothDone) {
                            "أقفلتَ الباب الخلفي بإحكام! افتح هاتفك واتصل بالنجدة ٩٩٩ الآن!"
                        } else {
                            "أقفلتَ مزلاج الباب الخلفي! توجّه إلى قاطع الكهرباء في الممر."
                        }
                    )
                }
                scheduleClearSubtitle(5000L)
            } else {
                showSubtitle("المزلاج الخلفي مقفل بإحكام.")
            }
        } else {
            showSubtitle("باب الحديقة الخلفية — مقفل.")
        }
    }

    private fun handleStoreDoor(st: GameUiState) {
        if (st.stage.ordinal < StoryStage.GO_TO_STORE.ordinal) {
            showSubtitle("البقالة ما زالت مفتوحة، لكن عليّ إنهاء أمور المنزل أولاً.")
            return
        }
        audioEngine.playDoorCreak()
        _uiState.update { cur ->
            cur.copy(renderState = cur.renderState.copy(storeDoorOpen = !cur.renderState.storeDoorOpen))
        }
    }

    private fun openDialogue(nodeId: String) {
        val node = StoryScript.getDialogueNode(nodeId) ?: return
        audioEngine.playUiClick()
        _uiState.update { cur ->
            cur.copy(
                activeDialogue = node,
                renderState = cur.renderState.copy(inputFrozen = true)
            )
        }
    }

    fun selectDialogueChoice(choice: DialogueChoice) {
        audioEngine.playUiClick()
        when (choice.effectTag) {
            "karim_alone" -> _uiState.update { it.copy(toldKarimAlone = true) }
            "karim_cautious" -> _uiState.update { it.copy(toldKarimAlone = false) }
            "finish_saleh" -> {
                _uiState.update { cur ->
                    cur.copy(
                        activeDialogue = null,
                        stage = StoryStage.ENTER_HOUSE,
                        renderState = cur.renderState.copy(
                            stage = StoryStage.ENTER_HOUSE,
                            inputFrozen = false
                        ),
                        toastSubtitle = "اقترب من باب المنزل رقم ١٢ وافتحه."
                    )
                }
                scheduleClearSubtitle(4000L)
                return
            }
            "finish_karim" -> {
                audioEngine.playCashRegister()
                audioEngine.playPhoneChime()
                _uiState.update { cur ->
                    cur.copy(
                        activeDialogue = null,
                        stage = StoryStage.RETURN_HOME,
                        tensionLevel = 0.25f,
                        messages = cur.messages + StoryScript.returnWarningMessage,
                        unreadMessages = cur.unreadMessages + 1,
                        renderState = cur.renderState.copy(
                            stage = StoryStage.RETURN_HOME,
                            porchLightOff = true,
                            inputFrozen = false
                        ),
                        toastSubtitle = "دفعتَ الحساب. عُد إلى المنزل رقم ١٢ قبل منتصف الليل."
                    )
                }
                audioEngine.tensionLevel = 0.25f
                scheduleClearSubtitle(4500L)
                return
            }
            "visitor_opened_door" -> {
                audioEngine.playDoorLock()
                audioEngine.playDoorCreak()
                audioEngine.playTensionStinger()
                _uiState.update { cur ->
                    cur.copy(
                        activeDialogue = null,
                        renderState = cur.renderState.copy(
                            peepholeActive = false,
                            frontDoorOpen = true,
                            inputFrozen = true
                        ),
                        toastSubtitle = "فتحتَ الباب... لحظة صمت ثقيلة تسبق خطوته إلى الداخل."
                    )
                }
                viewModelScope.launch {
                    delay(2200L)
                    triggerEnding(EndingType.FATAL_TRUST)
                }
                return
            }
            "finish_peephole_safe" -> {
                audioEngine.playDoorLock()
                audioEngine.playBreakerClack()
                audioEngine.tensionLevel = 0.85f
                _uiState.update { cur ->
                    cur.copy(
                        activeDialogue = null,
                        frontDoorLocked = true,
                        tensionLevel = 0.85f,
                        stage = StoryStage.RESTORE_BREAKER_AND_LOCK,
                        renderState = cur.renderState.copy(
                            peepholeActive = false,
                            strangerVisible = false,
                            powerOutage = true,
                            stage = StoryStage.RESTORE_BREAKER_AND_LOCK,
                            inputFrozen = false
                        ),
                        toastSubtitle = "انقطع التيار الكهربائي فجأة! أشعل مصباحك، تفقد القاطع وأقفل الباب الخلفي!"
                    )
                }
                scheduleClearSubtitle(6000L)
                return
            }
        }

        if (choice.nextNodeId != null) {
            openDialogue(choice.nextNodeId)
        } else {
            _uiState.update { cur ->
                cur.copy(
                    activeDialogue = null,
                    renderState = cur.renderState.copy(peepholeActive = false, inputFrozen = false)
                )
            }
        }
    }

    fun callEmergencyPolice() {
        val st = _uiState.value
        if (st.stage != StoryStage.CALL_POLICE_AND_SURVIVE || st.policeCalled) {
            if (st.stage.ordinal < StoryStage.CALL_POLICE_AND_SURVIVE.ordinal) {
                showSubtitle("لا يوجد طارئ يستدعي الاتصال بالنجدة في هذه اللحظة.")
            }
            return
        }

        audioEngine.playUiClick()
        audioEngine.tensionLevel = 0.95f
        _uiState.update { cur ->
            cur.copy(
                policeCalled = true,
                policeEtaSeconds = 7,
                isPhoneOpen = false,
                renderState = cur.renderState.copy(inputFrozen = false),
                toastSubtitle = "بلاغكِ مُسجّل لدى النجدة! الدورية في طريقها — ابقَ في أمان أو احتمِ بالخزانة!"
            )
        }

        viewModelScope.launch {
            delay(1200L)
            audioEngine.playHandleRattle()
            for (sec in 6 downTo 1) {
                _uiState.update { it.copy(policeEtaSeconds = sec) }
                if (sec == 4) audioEngine.playPoliceSiren()
                delay(1000L)
            }
            val finalState = _uiState.value
            val ending = if (!finalState.toldKarimAlone && finalState.frontDoorLocked && finalState.backDoorLocked) {
                EndingType.VIGILANT_SURVIVAL
            } else {
                EndingType.NARROW_ESCAPE
            }
            triggerEnding(ending)
        }
    }

    private fun triggerEnding(ending: EndingType) {
        audioEngine.tensionLevel = 0.05f
        viewModelScope.launch {
            prefs.recordEndingUnlocked(ending.id)
        }
        _uiState.update { cur ->
            cur.copy(
                screen = AppScreen.ENDING,
                stage = StoryStage.FINISHED,
                currentEnding = ending,
                isPhoneOpen = false,
                activeDialogue = null,
                renderState = cur.renderState.copy(inputFrozen = true, hidingInCloset = false)
            )
        }
    }

    fun updateSensitivity(v: Float) {
        viewModelScope.launch { prefs.setLookSensitivity(v) }
    }

    fun updatePsxJitter(enabled: Boolean) {
        viewModelScope.launch { prefs.setPsxJitter(enabled) }
    }

    fun updateCrtScanlines(enabled: Boolean) {
        viewModelScope.launch { prefs.setCrtScanlines(enabled) }
    }

    fun updateVolume(v: Float) {
        viewModelScope.launch { prefs.setMasterVolume(v) }
    }

    private fun showSubtitle(text: String) {
        _uiState.update { it.copy(toastSubtitle = text) }
        scheduleClearSubtitle(3600L)
    }

    private fun scheduleClearSubtitle(delayMs: Long) {
        viewModelScope.launch {
            val captured = _uiState.value.toastSubtitle
            delay(delayMs)
            _uiState.update { cur ->
                if (cur.toastSubtitle == captured) cur.copy(toastSubtitle = null) else cur
            }
        }
    }

    private fun computePrompt(target: InteractableId?, st: GameUiState): String? {
        return when (target) {
            InteractableId.NEIGHBOR_SALEH -> "تحدث مع الأستاذ صالح"
            InteractableId.FRONT_DOOR -> when (st.stage) {
                StoryStage.CHECK_PEEPHOLE -> "انظر عبر العين السحرية"
                StoryStage.ENTER_HOUSE -> "افتح الباب وادخل"
                else -> if (st.renderState.frontDoorOpen) "أغلق الباب الأمامي" else "افتح / أقفل الباب الأمامي"
            }
            InteractableId.BACK_DOOR -> if (st.stage == StoryStage.RESTORE_BREAKER_AND_LOCK && !st.backDoorLocked) {
                "أقفل مزلاج الباب الخلفي"
            } else "الباب الخلفي"
            InteractableId.MOP_TOOL -> "التقط الممسحة"
            InteractableId.MUD_SPOT_1, InteractableId.MUD_SPOT_2, InteractableId.MUD_SPOT_3 -> "نظّف بقعة الوحل"
            InteractableId.STORE_DOOR -> if (st.renderState.storeDoorOpen) "أغلق باب البقالة" else "افتح باب «بقالة الأمل»"
            InteractableId.SUPPLIES_BASKET -> "التقط البطاريات والمؤونة"
            InteractableId.CASHIER_KARIM -> "تحدث مع البائع كريم"
            InteractableId.PORCH_FLYER -> "اقرأ الورقة المطوية"
            InteractableId.KITCHEN_TABLE_DROP -> if (st.stage == StoryStage.PLACE_SUPPLIES) "ضع المشتريات وركّب البطاريات" else "طاولة الطعام"
            InteractableId.BREAKER_BOX -> "تفقد قاطع الكهرباء"
            InteractableId.BEDROOM_CLOSET -> if (st.policeCalled) "احتمِ داخل الخزانة" else "خزانة الملابس"
            null -> null
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
    }
}
