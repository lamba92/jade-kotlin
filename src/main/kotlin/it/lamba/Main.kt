package it.lamba

import it.lamba.agents.ConverterAgent
import it.lamba.agents.InitiatorAgent
import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.wrapper.AgentController

@Suppress("UNUSED_VARIABLE")
fun main(args: Array<String>){
    val myRuntime = Runtime.instance()

    // prepare the settings for the platform that we're going to connect to
    val myProfile = ProfileImpl()
    myProfile.setParameter(Profile.MAIN_PORT, "1099")

    // create the agent container
    val mainContainer = myRuntime.createMainContainer(myProfile)
    val rmaController = mainContainer.addRmaAgent()
    val initiatorController = mainContainer.createNewAgent(InitiatorAgent::class, "Initiator").run()
    val converterControllers = ArrayList<AgentController>().apply {
        for(i in 1 until 4 step 1){
            add(mainContainer.createNewAgent(ConverterAgent::class, "converter#$i", arrayOf("-cp", i.toString())).run())
        }
    }
}