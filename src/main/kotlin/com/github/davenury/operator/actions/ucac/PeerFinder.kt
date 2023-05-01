package com.github.davenury.operator.actions.ucac

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.github.davenury.operator.httpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

interface PeerFinder {
    fun findConsensusLeader(peerUrl: String): PeersetInformation?

    companion object {
        fun create(): PeerFinder =
            PeerFinderImplementation()
    }
}

data class PeerId(val value: String)
data class PeersetId(val value: String)

data class PeersetInformation(
    val currentConsensusLeader: PeerId?,
    val peersInPeerset: List<PeerId>
)
private data class PeersetInformationDto(
    val currentConsensusLeaderId: String?,
    val peersInPeerset: List<String>
) {
    fun toDomain() = PeersetInformation(
        this.currentConsensusLeaderId?.let { PeerId(it) },
        this.peersInPeerset.map { PeerId(it) }
    )
}

class PeerFinderImplementation : PeerFinder {
    override fun findConsensusLeader(peerUrl: String): PeersetInformation? =
        getLeaderId(peerUrl)

    private fun getLeaderId(peerUrl: String): PeersetInformation? =
        runBlocking {
            try {
                httpClient.get(peerUrl)
                    .body<PeersetInformationDto>()
                    .toDomain()
            } catch (e: Exception) {
                logger.error("Could not get leader id", e)
                null
            }
        }

    @JsonDeserialize
    private data class CurrentLeaderResponse(val currentLeaderPeerId: Int?)

    companion object {
        private val logger = LoggerFactory.getLogger("PeerFinder")
    }
}
