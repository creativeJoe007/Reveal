package domain.repository

import domain.models.Company
import domain.models.Suggestion
import java.util.*

interface DomainCompanyRepository {
    fun findById(id: Int): Optional<Company>
    fun attachSuggestionToCompany(company: Company, suggestion: List<Company>): Boolean
    fun updateRecord(updatedCompany: Company): Company // updatedCompany must contain ID of company to identify what data to change
}