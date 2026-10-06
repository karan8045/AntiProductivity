package com.karan.antiproductivity.challenge

import kotlin.random.Random

object RandomStringChallenge {
    private val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789abcdefghijkmnopqrstuvwxyz"

    fun generateTargetString(length: Int = 8): String {
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    fun validate(input: String, target: String): Boolean {
        return input == target
    }
}
