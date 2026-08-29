package cit.edu.primera.feature.dashboard.domain

data class WeeklyInfo(
    val milestones: List<String>,
    val symptoms: List<String>,
    val articles: List<ArticleInfo>
)

data class ArticleInfo(
    val title: String,
    val description: String,
    val imageUrl: String? = null
)

object BabyDevelopmentData {
    val weeklyContent = mapOf(
        1 to WeeklyInfo(
            milestones = listOf("Conception usually occurs about 2 weeks after your last period."),
            symptoms = listOf("Nausea", "Fatigue", "Frequent urination"),
            articles = listOf(
                ArticleInfo("Understanding Conception", "How it all begins.", null),
                ArticleInfo("Early Signs", "What to look out for.", null)
            )
        ),
        2 to WeeklyInfo(
            milestones = listOf("The sperm meets the egg.", "Fertilization occurs."),
            symptoms = listOf("Slight spotting", "Mild cramping"),
            articles = listOf(
                ArticleInfo("The Miracle of Fertilization", "How life begins.", null)
            )
        ),
        3 to WeeklyInfo(
            milestones = listOf("The fertilized egg is traveling to your uterus.", "Implantation might occur."),
            symptoms = listOf("Implantation bleeding", "Breast tenderness"),
            articles = listOf(
                ArticleInfo("Implantation Explained", "The first step of pregnancy.", null)
            )
        ),
        4 to WeeklyInfo(
            milestones = listOf("The blastocyst is now officially an embryo.", "Neural tube starts to develop."),
            symptoms = listOf("Missed period", "Breast tenderness", "Mild cramping"),
            articles = listOf(
                ArticleInfo("Your First Appointment", "What to expect when you're expecting.", null)
            )
        ),
        6 to WeeklyInfo(
            milestones = listOf("The neural tube along your baby's back is closing.", "The heart is pumping blood."),
            symptoms = listOf("Morning sickness", "Frequent urination", "Fatigue"),
            articles = listOf(
                ArticleInfo("The First Ultrasound", "Seeing your baby for the first time.", null)
            )
        ),
        8 to WeeklyInfo(
            milestones = listOf("Baby's heart is beating clearly.", "Tiny fingers and toes are forming."),
            symptoms = listOf("Morning sickness is common now.", "Heightened sense of smell."),
            articles = listOf(
                ArticleInfo("Managing Morning Sickness", "Tips to feel better.", null)
            )
        ),
        10 to WeeklyInfo(
            milestones = listOf("Your baby is no longer an embryo; they're now a fetus.", "Vital organs are beginning to function."),
            symptoms = listOf("Visible veins", "Round ligament pain"),
            articles = listOf(
                ArticleInfo("The Fetal Stage", "A major milestone.", null)
            )
        ),
        12 to WeeklyInfo(
            milestones = listOf("Baby's face is looking more human.", "Fingerprints are forming."),
            symptoms = listOf("Morning sickness might start to fade.", "Increased energy."),
            articles = listOf(
                ArticleInfo("The First Trimester Wall", "Navigating the end of trimester 1.", null),
                ArticleInfo("Nutrition for Two", "Eating healthy for your baby.", null)
            )
        ),
        14 to WeeklyInfo(
            milestones = listOf("Baby is making sucking motions.", "Lanugo (fine hair) starts to grow."),
            symptoms = listOf("Increased appetite", "Easier breathing"),
            articles = listOf(
                ArticleInfo("Welcome to the Second Trimester", "The 'honeymoon' phase.", null)
            )
        ),
        16 to WeeklyInfo(
            milestones = listOf("Baby's eyes can now perceive light.", "Coordinated movements start."),
            symptoms = listOf("Skin changes (the 'glow')", "Round ligament pain."),
            articles = listOf(
                ArticleInfo("Exercise During Pregnancy", "Staying active safely.", null)
            )
        ),
        18 to WeeklyInfo(
            milestones = listOf("Baby might start to hear sounds from outside.", "They're about the size of a bell pepper."),
            symptoms = listOf("Dizziness", "Swollen feet"),
            articles = listOf(
                ArticleInfo("Connecting with Baby", "Talking and singing to your bump.", null)
            )
        ),
        20 to WeeklyInfo(
            milestones = listOf("The halfway point!", "Baby's sex might be visible via ultrasound."),
            symptoms = listOf("Increased appetite", "Backaches."),
            articles = listOf(
                ArticleInfo("The Anatomy Scan", "Checking in on baby's development.", null)
            )
        ),
        22 to WeeklyInfo(
            milestones = listOf("Baby's sense of touch is developing.", "They have distinct eyebrows and eyelashes."),
            symptoms = listOf("Itchy skin", "Stretch marks"),
            articles = listOf(
                ArticleInfo("Skincare During Pregnancy", "Dealing with stretch marks.", null)
            )
        ),
        24 to WeeklyInfo(
            milestones = listOf("Baby's inner ear is developed.", "Lungs are forming branches."),
            symptoms = listOf("Swollen ankles", "Leg cramps."),
            articles = listOf(
                ArticleInfo("Glucose Screening", "Testing for gestational diabetes.", null)
            )
        ),
        26 to WeeklyInfo(
            milestones = listOf("Baby is inhaling and exhaling amniotic fluid.", "Eyes are beginning to open."),
            symptoms = listOf("Braxton Hicks contractions", "Slightly blurred vision"),
            articles = listOf(
                ArticleInfo("Understanding Braxton Hicks", "Practice for the big day.", null)
            )
        ),
        28 to WeeklyInfo(
            milestones = listOf("Baby can open and close their eyes.", "Lungs are capable of breathing air."),
            symptoms = listOf("Backaches", "Leg cramps", "Shortness of breath"),
            articles = listOf(
                ArticleInfo("Counting Kicks", "Monitoring your baby's movements.", null),
                ArticleInfo("Preparing for Birth", "What to pack in your hospital bag.", null)
            )
        ),
        30 to WeeklyInfo(
            milestones = listOf("Baby's brain is developing rapidly.", "They're putting on weight."),
            symptoms = listOf("Heartburn", "Shortness of breath"),
            articles = listOf(
                ArticleInfo("Sleeping Positions", "Getting comfortable in the third trimester.", null)
            )
        ),
        32 to WeeklyInfo(
            milestones = listOf("Baby is practicing breathing.", "Most organs are fully developed."),
            symptoms = listOf("Braxton Hicks contractions", "Heartburn."),
            articles = listOf(
                ArticleInfo("Third Trimester Comfort", "Getting through the final stretch.", null)
            )
        ),
        34 to WeeklyInfo(
            milestones = listOf("Baby's central nervous system is maturing.", "Their lungs are continuing to develop."),
            symptoms = listOf("Pelvic pressure", "Blurred vision"),
            articles = listOf(
                ArticleInfo("Perineal Massage", "Preparing for delivery.", null)
            )
        ),
        36 to WeeklyInfo(
            milestones = listOf("Baby is gaining about half a pound a week.", "Lungs are nearly mature."),
            symptoms = listOf("Pelvic pressure", "Difficulty sleeping."),
            articles = listOf(
                ArticleInfo("Signs of Labor", "When to call your doctor.", null)
            )
        ),
        38 to WeeklyInfo(
            milestones = listOf("Baby is full term!", "They have a firm grasp."),
            symptoms = listOf("Increased vaginal discharge", "Diarrhea"),
            articles = listOf(
                ArticleInfo("The Waiting Game", "Coping with the final days.", null)
            )
        ),
        40 to WeeklyInfo(
            milestones = listOf("Baby is ready to meet the world!", "Average weight is 6-9 pounds."),
            symptoms = listOf("Strong contractions", "Burst of energy (nesting)."),
            articles = listOf(
                ArticleInfo("Meeting Your Baby", "The first hour after birth.", null)
            )
        )
    )

    fun getInfoForWeek(week: Int): WeeklyInfo {
        val availableWeeks = weeklyContent.keys.sorted()
        val closestWeek = availableWeeks.lastOrNull { it <= week } ?: availableWeeks.first()
        return weeklyContent[closestWeek]!!
    }
}
