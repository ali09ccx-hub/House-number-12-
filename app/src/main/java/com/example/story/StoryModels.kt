package com.example.story

enum class StoryStage(
    val chapterTitle: String,
    val objectiveTitle: String,
    val vhsTime: String
) {
    TALK_TO_NEIGHBOR(
        chapterTitle = "الفصل الأول: وصول هادئ",
        objectiveTitle = "تحدّث مع جارك الأستاذ صالح عند السياج",
        vhsTime = "21:42:08"
    ),
    ENTER_HOUSE(
        chapterTitle = "الفصل الأول: عتبة الدار",
        objectiveTitle = "افتح الباب الأمامي وادخل المنزل رقم ١٢",
        vhsTime = "21:46:19"
    ),
    PICK_UP_MOP(
        chapterTitle = "الفصل الثاني: آثار الأقدام",
        objectiveTitle = "التقط الممسحة من زاوية المطبخ",
        vhsTime = "21:49:05"
    ),
    CLEAN_FLOOR(
        chapterTitle = "الفصل الثاني: ترتيب المنزل",
        objectiveTitle = "نظّف بقع الوحل عن الأرضية",
        vhsTime = "21:52:41"
    ),
    CHECK_PHONE_AT_HOME(
        chapterTitle = "الفصل الثاني: رسالة واردة",
        objectiveTitle = "افتح هاتفك واقرأ الرسائل الواردة",
        vhsTime = "21:58:12"
    ),
    GO_TO_STORE(
        chapterTitle = "الفصل الثالث: مشوار البقالة",
        objectiveTitle = "امشِ شرقاً على الرصيف نحو «بقالة الأمل»",
        vhsTime = "22:05:33"
    ),
    PICK_UP_SUPPLIES(
        chapterTitle = "الفصل الثالث: بقالة الأمل",
        objectiveTitle = "التقط سلة البطاريات والمؤونة من الرف",
        vhsTime = "22:11:47"
    ),
    PAY_CASHIER(
        chapterTitle = "الفصل الثالث: عند المحاسب",
        objectiveTitle = "توجّه إلى البائع كريم لدفع الحساب",
        vhsTime = "22:14:20"
    ),
    RETURN_HOME(
        chapterTitle = "الفصل الرابع: طريق العودة",
        objectiveTitle = "عُد إلى المنزل رقم ١٢ واغلق الباب خلفك",
        vhsTime = "22:26:04"
    ),
    PLACE_SUPPLIES(
        chapterTitle = "الفصل الرابع: مصباح يدوي",
        objectiveTitle = "ضع المشتريات على طاولة المطبخ لتركيب البطاريات",
        vhsTime = "22:31:50"
    ),
    CHECK_PEEPHOLE(
        chapterTitle = "الفصل الخامس: طرقاتٌ متأخرة",
        objectiveTitle = "انظر عبر العين السحرية للباب الأمامي وتحقق من الزائر",
        vhsTime = "22:39:11"
    ),
    RESTORE_BREAKER_AND_LOCK(
        chapterTitle = "الفصل الخامس: انقطاع التيار",
        objectiveTitle = "أشعل المصباح وتفقد القاطع الكهربائي ثم أقفل الباب الخلفي",
        vhsTime = "22:45:29"
    ),
    CALL_POLICE_AND_SURVIVE(
        chapterTitle = "الفصل الأخير: الدقائق الحاسمة",
        objectiveTitle = "اتصل بالنجدة (٩٩٩) عبر هاتفك وتحصّن بأمان حتى وصول الدورية",
        vhsTime = "22:51:03"
    ),
    FINISHED(
        chapterTitle = "نهاية الشريط",
        objectiveTitle = "اكتملت الواقعة",
        vhsTime = "23:08:44"
    )
}

enum class EndingType(
    val id: String,
    val title: String,
    val badge: String,
    val summary: String,
    val policeNote: String
) {
    VIGILANT_SURVIVAL(
        id = "ending_vigilant",
        title = "النهاية الأولى: نجاة واعية",
        badge = "النهاية الحقيقية",
        summary = "بفضل حذرك في البقالة وعدم كشفك أنك تقيم وحدك، ورفضك فتح الباب للزائر المريب رغم حيلته، وإقفالك جميع الأقفال قبل اتصالك السريع بالنجدة، وصلت دورية الشرطة في الوقت المناسب وألقت القبض على المنتحل في الزقاق الخلفي وبحوزته أدوات اقتحام ومفاتيح منسوخة.",
        policeNote = "ملف رقم ٢١٤-ب: تم توقيف المشتبه به (س. م) بتهمة انتحال صفة موظف صيانة ومحاولة السطو المتسلسل على المنازل الحديثة السكن."
    ),
    NARROW_ESCAPE(
        id = "ending_narrow",
        title = "النهاية الثانية: نجاة بأعجوبة",
        badge = "نهاية غامضة",
        summary = "تسببت بعض الهفوات في كشف وجودك بمفردك أو التأخر في تأمين المداخل، فاضطررت إلى الاحتماء في غرفة النوم والاتصال بالنجدة بينما حاول المتسلل فتح الممر الخلفي. لاذ بالفرار فور سماعه صفارات الدورية عن بُعد، تاركاً وراءه باباً مخدوشاً وذكراه الثقيلة.",
        policeNote = "ملف رقم ٢١٤-ج: فرّ المشتبه به قبل وصول الدورية بدقيقة واحدة. يُنصح بتغيير جميع الأقفال وتعزيز الإنارة الخارجية."
    ),
    FATAL_TRUST(
        id = "ending_fatal",
        title = "النهاية الثالثة: ثقة في غير محلها",
        badge = "النهاية المظلمة",
        summary = "انخدعت بهدوء نبرة «مفتش الصيانة» وفتحت له الباب دون التحقق من هويته أو الانتباه لتحذيرات البائع والأخبار. في اللحظة التي خطا فيها داخل الردهة وأغلق الباب خلفه، أدركت متأخراً أن أحداً لم يطلب فحصاً للأنابيب هذه الليلة.",
        policeNote = "تقرير الدوريات الصباحي: عُثر على باب المنزل رقم ١٢ مفتوحاً عند الفجر، والهاتف مرمياً قرب طاولة الطعام."
    )
}

data class DialogueChoice(
    val id: String,
    val text: String,
    val nextNodeId: String?,
    val effectTag: String? = null
)

data class DialogueNode(
    val id: String,
    val speaker: String,
    val speakerRole: String,
    val body: String,
    val choices: List<DialogueChoice>
)

data class SmsMessage(
    val id: String,
    val sender: String,
    val time: String,
    val body: String,
    val isAlert: Boolean = false
)
