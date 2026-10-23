package com.raviteja.water_reminder.logic

import kotlin.random.Random

object QuotesProvider {

    private val motivationalQuotes = listOf(
        "Drink water like your energy depends on it — because it does.",
        "Your body works better when you are hydrated.",
        "Water is the cheapest health upgrade you can get.",
        "A glass of water now can save you from tiredness later.",
        "Hydration is self-care.",
        "Every sip of water is a step toward feeling better.",
        "Water fuels your brain, muscles, and mood.",
        "Staying hydrated helps you stay focused.",
        "Water is the foundation of good health.",
        "Your body is asking for water more often than you think."
    )

    private val healthFacts = listOf(
        "Around 60% of the human body is made of water.",
        "Drinking enough water can improve concentration.",
        "Water helps regulate body temperature.",
        "Staying hydrated supports digestion.",
        "Drinking water can reduce headaches caused by dehydration.",
        "Water helps transport nutrients throughout the body.",
        "Hydration can improve skin appearance.",
        "Drinking water helps maintain healthy joints.",
        "Water supports healthy kidney function.",
        "Even mild dehydration can make you feel tired."
    )

    private val fitnessQuotes = listOf(
        "Water helps muscles perform better.",
        "Hydrated people often have more energy.",
        "Drinking water before exercise improves performance.",
        "Water helps prevent cramps during workouts.",
        "Sweating means your body needs water back.",
        "Hydration supports faster recovery after exercise.",
        "Drinking water can help reduce fatigue.",
        "Your body loses water even when you are resting.",
        "More water often means better endurance.",
        "Hydration can help you stay active longer."
    )

    private val simpleQuotes = listOf(
        "Sip now, thank yourself later.",
        "Water first, everything else second.",
        "One more glass can make a difference.",
        "Stay cool, stay hydrated.",
        "Water is your body's favorite drink.",
        "Small sips lead to big benefits.",
        "Make water your daily habit.",
        "Keep calm and drink water.",
        "Hydration is a superpower.",
        "Your future self will thank you for this glass of water."
    )

    private val dailyReminderFacts = listOf(
        "Your body loses water every hour.",
        "Thirst is a sign you may already be dehydrated.",
        "Drinking water regularly is better than drinking a lot at once.",
        "Morning water can help wake up your body.",
        "Water can help reduce feelings of hunger.",
        "Drinking enough water can support better sleep.",
        "Water helps your body remove waste.",
        "Good hydration can improve mood.",
        "Carrying a water bottle makes it easier to drink more.",
        "A healthy day starts with enough water."
    )

    private val allDashboardQuotes = motivationalQuotes + healthFacts + fitnessQuotes + simpleQuotes + dailyReminderFacts

    fun getRandomQuote(): String {
        return allDashboardQuotes[Random.nextInt(allDashboardQuotes.size)]
    }

    private val lowIntakeMessages = listOf(
        "You have not had enough water today. Drink a glass now.",
        "Your body needs water. Take a short hydration break.",
        "You are falling behind on your water goal.",
        "A quick glass of water can help you feel better.",
        "You have missed a few water reminders today.",
        "Time to hydrate before you feel tired.",
        "Your body might be asking for water right now.",
        "Do not wait until you are thirsty.",
        "Hydration is important. Drink some water now.",
        "You are low on water intake today.",
        "A few sips now can make a big difference.",
        "Your energy may improve with water.",
        "You still have a lot of water left to drink today.",
        "Hydration check: drink one glass now.",
        "You deserve to feel refreshed. Drink water.",
        "Water reminder: your body needs fuel too.",
        "You have been busy, but do not forget water.",
        "Your water goal is waiting for you.",
        "Drink water now and keep yourself healthy.",
        "Even one glass is progress."
    )

    private val okIntakeMessages = listOf(
        "Nice job staying hydrated today.",
        "You are doing well. Keep drinking water regularly.",
        "Good progress so far. One more glass can help.",
        "You are halfway to your water goal.",
        "Keep the momentum going with another glass.",
        "Your hydration is looking better today.",
        "Great effort. Stay consistent.",
        "Keep sipping water throughout the day.",
        "You are on track. Stay hydrated.",
        "One more glass and you will be even closer to your goal.",
        "Good work. Your body thanks you.",
        "You are making progress with your hydration.",
        "Water break time. Keep your streak alive.",
        "You are doing better than yesterday.",
        "Keep going. You are close to your target."
    )

    private val greatIntakeMessages = listOf(
        "Amazing job. You are staying well hydrated today.",
        "You are crushing your water goal.",
        "Great work staying consistent with water.",
        "Your hydration habits are excellent today.",
        "You are doing an awesome job taking care of yourself.",
        "You are close to completing your water goal.",
        "Excellent progress. Keep it up.",
        "You are making hydration a healthy habit.",
        "Fantastic work. Your body will thank you.",
        "You are setting a great example with your water intake.",
        "Your hydration level is looking great today.",
        "You are doing an incredible job staying hydrated.",
        "Great consistency. Keep drinking water regularly.",
        "You are almost at 100% of your daily water goal.",
        "Excellent work. Stay hydrated and keep feeling great."
    )

    fun getNotificationMessage(status: HydrationAlgorithm.ReminderStatus): String {
        return when (status) {
            HydrationAlgorithm.ReminderStatus.CRITICAL -> lowIntakeMessages[Random.nextInt(lowIntakeMessages.size)]
            HydrationAlgorithm.ReminderStatus.NORMAL -> okIntakeMessages[Random.nextInt(okIntakeMessages.size)]
            HydrationAlgorithm.ReminderStatus.PASSIVE -> greatIntakeMessages[Random.nextInt(greatIntakeMessages.size)]
        }
    }
}
