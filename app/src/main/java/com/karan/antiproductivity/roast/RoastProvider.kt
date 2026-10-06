package com.karan.antiproductivity.roast

object RoastProvider {
    private val roasts = listOf(
        "You opened your phone to procrastinate. Impressive.",
        "Your future self is disappointed.",
        "That app isn't going anywhere.",
        "You could have finished the assignment by now.",
        "Denied. Go touch grass.",
        "15 minutes of entertainment must be earned.",
        "You couldn't type 8 characters. Tragic.",
        "Maybe YouTube wasn't meant to be opened right now.",
        "Error 404: Motivation not found. Try typing better.",
        "Back to work. The doomscrolling can wait."
    )

    fun getRandomRoast(): String {
        return roasts.random()
    }
}
