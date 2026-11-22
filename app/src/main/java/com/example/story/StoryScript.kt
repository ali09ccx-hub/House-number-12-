package com.example.story

object StoryScript {

    val initialMessages = listOf(
        SmsMessage(
            id = "msg_1",
            sender = "طارق (أخوك)",
            time = "21:15",
            body = "حمداً لله على سلامة وصولك إلى المنزل الجديد! إذا احتجت إلى شيء فأنا في الحي المجاور الليلة، ولا تنسَ إقفال المزلاج قبل النوم."
        )
    )

    val homeFollowUpMessages = listOf(
        SmsMessage(
            id = "msg_2",
            sender = "طارق (أخوك)",
            time = "21:57",
            body = "تذكرتُ أمراً: مصباح الطوارئ في حقيبتك بلا بطاريات. اذهب إلى «بقالة الأمل» في آخر الشارع واشترِ بطاريات وماءً قبل أن يُغلق."
        ),
        SmsMessage(
            id = "msg_3",
            sender = "تنبيه الحي السكني",
            time = "21:58",
            body = "تنويه للسكان: لوحظ في الأيام الماضية شخص يدّعي التفتيش على عدّادات المياه ليلاً. يرجى عدم فتح الأبواب قبل التحقق من الهوية الرسمية.",
            isAlert = true
        )
    )

    val returnWarningMessage = SmsMessage(
        id = "msg_4",
        sender = "طارق (أخوك)",
        time = "22:25",
        body = "اتصلتُ بك ولم ترد، طمئني عندما تدخل البيت. الشارع يبدو هادئاً أكثر من المعتاد هذه الليلة."
    )

    fun getDialogueNode(nodeId: String): DialogueNode? = nodes[nodeId]

    private val nodes: Map<String, DialogueNode> = mapOf(
        // === 1. الحوار مع الجار الأستاذ صالح ===
        "saleh_1" to DialogueNode(
            id = "saleh_1",
            speaker = "الأستاذ صالح",
            speakerRole = "جارك في المنزل المجاور",
            body = "مساء الخير يا بني. أهلاً بك في حي الروابي، أراك قد وصلتَ أخيراً إلى المنزل رقم ١٢.",
            choices = listOf(
                DialogueChoice(
                    id = "s1_polite",
                    text = "مساء النور يا عم صالح، سرّني التعرف إليك في يومي الأول.",
                    nextNodeId = "saleh_2_polite",
                    effectTag = "polite_saleh"
                ),
                DialogueChoice(
                    id = "s1_brief",
                    text = "مساء الخير، نعم وصلتُ للتو وأحتاج إلى ترتيب الأغراض سريعاً.",
                    nextNodeId = "saleh_2_brief"
                )
            )
        ),
        "saleh_2_polite" to DialogueNode(
            id = "saleh_2_polite",
            speaker = "الأستاذ صالح",
            speakerRole = "جارك في المنزل المجاور",
            body = "بارك الله فيك. على فكرة، مرّ رجل أمس يسأل إن كان المنزل قد سُكن بعد، وظننته من عمال النظافة أو التوصيل. بالمناسبة، عمال النقل تركوا بعض آثار الوحل في مدخل بيتك، تجد الممسحة في زاوية المطبخ.",
            choices = listOf(
                DialogueChoice(
                    id = "s2_ask",
                    text = "هل أخبرك لماذا كان يسأل عن المنزل؟",
                    nextNodeId = "saleh_3",
                    effectTag = "asked_saleh_detail"
                ),
                DialogueChoice(
                    id = "s2_thanks",
                    text = "شكراً لك على التنبيه، سأدخل لتنظيف الأرضية الآن.",
                    nextNodeId = null,
                    effectTag = "finish_saleh"
                )
            )
        ),
        "saleh_2_brief" to DialogueNode(
            id = "saleh_2_brief",
            speaker = "الأستاذ صالح",
            speakerRole = "جارك في المنزل المجاور",
            body = "كما تشاء، لا أريد أن أعطلك. فقط انتبه لأرضية المدخل فقد ترك عمال الأثاث وحلاً عند الباب، والممسحة في ركن المطبخ. ولا تتأخر إن احتجت شراء شيء فبقالة الأمل تغلق عند الحادية عشرة.",
            choices = listOf(
                DialogueChoice(
                    id = "s2b_ok",
                    text = "مفهوم، تصبح على خير.",
                    nextNodeId = null,
                    effectTag = "finish_saleh"
                )
            )
        ),
        "saleh_3" to DialogueNode(
            id = "saleh_3",
            speaker = "الأستاذ صالح",
            speakerRole = "جارك في المنزل المجاور",
            body = "قال إنه يتأكد من صيانة العدادات للمستأجرين الجدد، لكنه لم يكن يرتدي زياً رسمياً. ربما موظف متعاقد. ادخل واسترح الآن ولا تنسَ تنظيف الأرضية قبل أن يجف الوحل.",
            choices = listOf(
                DialogueChoice(
                    id = "s3_end",
                    text = "حسناً يا عم صالح، أشكرك على حرصك.",
                    nextNodeId = null,
                    effectTag = "finish_saleh"
                )
            )
        ),

        // === 2. الحوار مع البائع كريم في بقالة الأمل ===
        "karim_1" to DialogueNode(
            id = "karim_1",
            speaker = "كريم",
            speakerRole = "بائع «بقالة الأمل»",
            body = "أهلاً بك يا أخي! يبدو أنك الساكن الجديد في المنزل رقم ١٢، أليس كذلك؟ هذا المذياع يذيع منذ ساعة أخباراً مزعجة عن سرقات في الأحياء المجاورة.",
            choices = listOf(
                DialogueChoice(
                    id = "k1_cautious",
                    text = "نعم انتقلتُ مع أخي اليوم، هو ينتظرني في المنزل الآن. تفضل حساب البطاريات والماء.",
                    nextNodeId = "karim_2_cautious",
                    effectTag = "karim_cautious"
                ),
                DialogueChoice(
                    id = "k1_alone",
                    text = "نعم أنا هناك بمفردي هذه الليلة. ما قصة هذه الأخبار؟",
                    nextNodeId = "karim_2_alone",
                    effectTag = "karim_alone"
                )
            )
        ),
        "karim_2_cautious" to DialogueNode(
            id = "karim_2_cautious",
            speaker = "كريم",
            speakerRole = "بائع «بقالة الأمل»",
            body = "جيد أنكما معاً. المذيع حذّر من شخص يطرق الأبواب ليلاً بزعم فحص تسرب المياه أو الكهرباء. خذ الباقي، وعُد مباشرة فالشارع يزداد ضباباً.",
            choices = listOf(
                DialogueChoice(
                    id = "k2c_end",
                    text = "شكراً لك يا كريم، سنحرص على ذلك.",
                    nextNodeId = null,
                    effectTag = "finish_karim"
                )
            )
        ),
        "karim_2_alone" to DialogueNode(
            id = "karim_2_alone",
            speaker = "كريم",
            speakerRole = "بائع «بقالة الأمل»",
            body = "(يخفض صوته بنبرة قلقة، بينما يلتفت نحو زبون غادر لتوه عند الباب): يُقال إن شخصاً ينتحل صفة مفتش صيانة ليعرف إن كان البيت خالياً. لا تخبر أحداً أنك وحدك، وأقفل الباب الخلفي جيداً فور عودتك!",
            choices = listOf(
                DialogueChoice(
                    id = "k2a_end",
                    text = "فهمتُ... سأعود إلى المنزل فوراً.",
                    nextNodeId = null,
                    effectTag = "finish_karim"
                )
            )
        ),

        // === 3. الحوار عند الباب الأمامي عبر العين السحرية ===
        "visitor_1" to DialogueNode(
            id = "visitor_1",
            speaker = "رجل غريب في الشرفة",
            speakerRole = "خارج الباب الأمامي",
            body = "مساء الخير... المعذرة على الإزعاج في هذا الوقت. أنا من مكتب الطوارئ البلدية، تلقينا بلاغاً عن تسرب مياه يغمر مطبخ المنزل رقم ١٢. هل تفتح الباب لأفحص الأنابيب سريعاً؟",
            choices = listOf(
                DialogueChoice(
                    id = "v1_question",
                    text = "من قدّم البلاغ؟ وما رقم هويتك الوظيفية؟ لن أفتح الباب في العاشرة والنصف ليلاً!",
                    nextNodeId = "visitor_2_suspicious",
                    effectTag = "visitor_interrogated"
                ),
                DialogueChoice(
                    id = "v1_stall",
                    text = "المطبخ جاف تماماً ولا يوجد أي تسرب، أخي نائم في الداخل الآن، عُد غداً صباحاً.",
                    nextNodeId = "visitor_2_stall",
                    effectTag = "visitor_refused"
                ),
                DialogueChoice(
                    id = "v1_open",
                    text = "(افتح المزلاج) تفضل بالدخول إذا كان الأمر طارئاً...",
                    nextNodeId = null,
                    effectTag = "visitor_opened_door"
                )
            )
        ),
        "visitor_2_suspicious" to DialogueNode(
            id = "visitor_2_suspicious",
            speaker = "الرجل الغريب",
            speakerRole = "خارج الباب الأمامي",
            body = "(يصمت لثانية ثم يبتسم ببرود دون أن يرفع بطاقة): الأمر لا يحتمل التأجيل... أطفئتُ المحبس الخارجي للتو وقد ينقطع تيار بيتك أيضاً إن لم أتحقق من غرفة العداد.",
            choices = listOf(
                DialogueChoice(
                    id = "v2s_refuse",
                    text = "ابقَ خارجاً! سأتصل بالبلدية وبالشرطة للتحقق من أمرك الآن.",
                    nextNodeId = null,
                    effectTag = "finish_peephole_safe"
                ),
                DialogueChoice(
                    id = "v2s_open",
                    text = "حسناً، سأفتح الباب لترى العداد سريعاً...",
                    nextNodeId = null,
                    effectTag = "visitor_opened_door"
                )
            )
        ),
        "visitor_2_stall" to DialogueNode(
            id = "visitor_2_stall",
            speaker = "الرجل الغريب",
            speakerRole = "خارج الباب الأمامي",
            body = "(ينظر ببطء نحو نافذة غرفة المعيشة المظلمة): أخوك؟ الغريب أنني لم أرَ سوى ظل شخص واحد يتحرك داخل المطبخ قبل دقائق... على كل حال، تأكد من أنبوب الحديقة الخلفية.",
            choices = listOf(
                DialogueChoice(
                    id = "v2st_refuse",
                    text = "(ابتعد عن الباب وأحكم إقفال المزلاج)",
                    nextNodeId = null,
                    effectTag = "finish_peephole_safe"
                )
            )
        )
    )
}
