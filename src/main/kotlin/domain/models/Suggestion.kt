package domain.models

import java.util.*

data class Suggestion(
    val Id: Int,
    val company: Company,
    var status: String? = null
)
