package com.github.davenury.operator.actions.ucac

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.github.davenury.operator.httpClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

interface PeerFinder {
    fun findConsensusLeader(peersetId: PeersetId, namespace: String): PeerId?

    companion object {
        fun create(): PeerFinder =
            PeerFinderImplementation()
    }
}

data class PeerId(val value: Int)
data class PeersetId(val value: Int)

class PeerFinderImplementation : PeerFinder {
    override fun findConsensusLeader(peersetId: PeersetId, namespace: String): PeerId? =
        getLeaderId(peersetId, namespace)

    private fun getLeaderId(peersetId: PeersetId, namespace: String): PeerId? =
        runBlocking {
            try {
                httpClient.get("http://peer0-peerset${peersetId.value}-service.${namespace}.svc.cluster.local:8081/consensus/current-leader")
                    .body<CurrentLeaderResponse>()
                    .currentLeaderPeerId?.let {
                        PeerId(it)
                    }
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
