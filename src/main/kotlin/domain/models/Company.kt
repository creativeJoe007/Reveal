package domain.models

import java.util.*

data class Company (
    val id: Int,
    val title: String,
    var country: String,
    val industry: String,
    var email: String,
    var suggestedCompanies: List<Suggestion> = listOf()
)