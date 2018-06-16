package it.lamba.agents

import it.lamba.Utils.convertContent
import it.lamba.addReceivers
import it.lamba.behaviours.ModernContractNetInitiator
import it.lamba.data.JobsQueue
import it.lamba.data.MessageContent
import it.lamba.data.Job
import it.lamba.events.OnGuiClosedEvent
import it.lamba.gui.InitiatorGUI
import it.lamba.maxByField
import jade.core.AID
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.domain.FIPANames.InteractionProtocol.FIPA_CONTRACT_NET
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.collections.ArrayList

class InitiatorAgent: ModernAgent() {

    private val jobsQueue = JobsQueue()

    override fun onCreate(args: Array<String>) {
        EventBus.getDefault().register(this)
        Thread{
            javafx.application.Application.launch(InitiatorGUI::class.java)
        }.start()
        jobsQueue.setOnJobAddedListener {job ->
            searchAgents(converterAgentDescription(), { convertersAID ->
                if(convertersAID.isNotEmpty()){
                    addBehaviour(object : ModernContractNetInitiator(this, converterCfpMessage(convertersAID)){
                        override fun onProposeReceived(propose: ACLMessage) {

                        }

                        override fun onRefuseReceived(refuse: ACLMessage) {

                        }

                        override fun onFailureReceived(failure: ACLMessage) {

                        }

                        override fun onContractTimeExpired(responses: Array<ACLMessage>): Array<ACLMessage> {
                            val acceptances = ArrayList<ACLMessage>()
                            if (responses.isNotEmpty()) {
                                val highestBidMessage = responses
                                        .filter { it.performative == PROPOSE }
                                        .maxByField { convertContent(it.content).bid.toFloat() }!!
                                acceptances.add(highestBidMessage.createReply().apply {
                                    performative = ACCEPT_PROPOSAL
                                    content = convertContent(convertContent(highestBidMessage.content).apply { content = job })
                                })
                                for(message in responses){
                                    if (message != highestBidMessage)
                                        acceptances.add(message.createReply().apply {
                                            performative = REJECT_PROPOSAL
                                        })
                                }
                            }
                            return acceptances.toTypedArray()
                        }

                        override fun onActionPerformed(inform: ACLMessage) {
                            jobsQueue.readyForNext()
                        }

                    })
                }
            })
        }
    }

    override fun onMessageReceived(message: ACLMessage) {

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        jobsQueue.removeOnJobAddedListener()
    }

    @Subscribe fun onClosedGuiEvent(e: OnGuiClosedEvent) = shutDown()

    @Subscribe fun onConvertJobReceived(e: Job) = jobsQueue.addJob(e)

    private fun converterAgentDescription() = DFAgentDescription().apply {
        addServices(ServiceDescription().apply {
            type = "convert-video-file"
            name = "JADE-converter"
        })
    }

    private fun converterCfpMessage(convertersAID: Array<AID>) = ACLMessage(CFP).apply {
        addReceivers(convertersAID)
        protocol = FIPA_CONTRACT_NET
        replyByDate = Date(System.currentTimeMillis() + 10000)
        content = convertContent(MessageContent(UUID.randomUUID().toString(), "convert-job"))
    }
}
