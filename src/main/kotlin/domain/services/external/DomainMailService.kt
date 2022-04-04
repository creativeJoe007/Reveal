package domain.services.external

interface DomainMailService {
    fun sendMail(title: String, content: String, recipient: String)
}