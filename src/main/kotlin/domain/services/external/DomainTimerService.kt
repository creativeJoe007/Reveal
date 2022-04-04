package domain.services.external

interface DomainTimerService {
    fun setTimer(timeInMili: Long, metaData: Map<String, Any>): Boolean
}