package domain.services.external

import domain.models.Company

interface DomainCompaniesService {
    fun getCompaniesByCountryAndIndustry (country: String, industry: String): List<Company>
}