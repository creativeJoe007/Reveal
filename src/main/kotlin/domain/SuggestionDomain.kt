package domain

import domain.models.Company
import domain.repository.DomainCompanyRepository
import domain.services.external.DomainCompaniesService
import domain.services.external.DomainGrowthPoliciesService
import domain.services.external.DomainMailService
import domain.services.external.DomainTimerService

const val ACCEPTED = "ACCEPTED"
const val REJECTED = "REJECTED"

class SuggestionDomain {
    // list service variables here
    private lateinit var companiesService: DomainCompaniesService
    private lateinit var growthPoliciesService: DomainGrowthPoliciesService
    private lateinit var timerService: DomainTimerService
    private lateinit var mailService: DomainMailService

    // list repositories
    private lateinit var companyRepository: DomainCompanyRepository

    fun setServices(
        _companiesService: DomainCompaniesService,
        _growthPoliciesService: DomainGrowthPoliciesService,
        _timerService: DomainTimerService,
        _mailService: DomainMailService
    ) {
        companiesService = _companiesService
        growthPoliciesService = _growthPoliciesService
        timerService = _timerService
        mailService= _mailService
    }
    fun setDataRepositories(_companyRepository: DomainCompanyRepository) {
        companyRepository = _companyRepository
    }
    fun handleCompanyCreated(createdCompany: Company): Company {
        // this function is called when a company is created,
        if (this::companiesService.isInitialized) {
            val suggestedCompaniesByCountryAndIndustry = companiesService
                .getCompaniesByCountryAndIndustry(
                    createdCompany.country,
                    createdCompany.industry
                )

            // Remove this new created company from the suggested data if included.
            val filteredSuggestedCompanies = suggestedCompaniesByCountryAndIndustry
                .filter { it.id != createdCompany.id }

            // Attach our filtered suggestion to the company records, by default status is null,
            // meaning neither accepted nor rejected
            companyRepository
                .attachSuggestionToCompany(
                    createdCompany,
                    filteredSuggestedCompanies
                )

            this.activateSuggestionTimer(createdCompany)
            return createdCompany
        } else throw Exception("CompaniesService not implemented")
    }
    fun timerCountDownTrigger(metaData: Map<String, Any>) {
        // This is triggered by the timer service
        // This means a timer is complete now we check if the company hasn't taken any action yet
        // metadata is passed, so we can get email type as well as get details of the company
        val companyId = metaData["companyId"] as Int
        val emailType = metaData["emailType"] as Int

        val selectedCompany = companyRepository.findById(companyId)
        if (selectedCompany.isEmpty)
            throw Exception("Oops, seems no company with ID ${metaData["companyId"]} exist in our DB")
        else {
            // now we check which suggestion hasn't been accepted nor rejected
            val unAnsweredSuggestions = selectedCompany.get()
                .suggestedCompanies
                    .filter { it.status == null }

            val emailDetails =
                growthPoliciesService.generateEmailDetails(companyId, emailType, unAnsweredSuggestions)

            // Now send mail
            mailService.sendMail(
                emailDetails.first, // email title
                emailDetails.second, // email content
                emailDetails.third // recipient
            )

        }
    }
    fun handleUserActionWebConsole(companyId: Int, suggestionId: Int, selectedStatus: String) {
        val selectedCompany = companyRepository.findById(companyId)
        val selectedSuggestion = selectedCompany.get()
            .suggestedCompanies
            .find { it.Id == suggestionId }

        if (
            selectedSuggestion != null &&
            (selectedStatus == ACCEPTED || selectedStatus == REJECTED)
        ) {
            selectedSuggestion.status =  selectedStatus
            companyRepository.updateRecord(selectedCompany.get())
        }
    }
    private fun activateSuggestionTimer(company: Company) {
        growthPoliciesService
            .getSequenceForSuggestionEmails()
            .forEach{
                timerService.setTimer(it.second, mapOf(
                    "emailType" to it.first,
                    "companyId" to company.id
                ))
            }
    }
}