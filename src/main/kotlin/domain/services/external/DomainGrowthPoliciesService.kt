package domain.services.external

import domain.models.Company
import domain.models.Suggestion
import java.util.*

interface DomainGrowthPoliciesService {
    fun getSequenceForSuggestionEmails(): List<Pair<Int, Long>>
    fun generateEmailDetails(companyId: Int, emailType: Int, suggestedCompanies: List<Suggestion>):
            Triple<String, String, String>
}