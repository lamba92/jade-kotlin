package it.lamba.agents

import it.lamba.Utils.convertContent
import it.lamba.behaviours.ModernContractNetResponder
import it.lamba.data.Job
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.domain.FIPAException
import jade.domain.FIPANames.InteractionProtocol.FIPA_CONTRACT_NET
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.*
import jade.lang.acl.MessageTemplate
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFmpegUtils
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.FFprobe
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class ConverterAgent: ModernAgent() {

    private val ffmpeg = FFmpeg(this::class.java.classLoader.getResource("ffmpeg-4.0-win64-static/bin/ffmpeg.exe").path.removeRange(0,1))
    var ffprobe = FFprobe(this::class.java.classLoader.getResource("ffmpeg-4.0-win64-static/bin/ffprobe.exe").path.removeRange(0,1))
    private var working = false
    private var computationPower = 0

    override fun onMessageReceived(message: ACLMessage) {

    }

    override fun onCreate(args: Array<String>) {
        val cpIndex = args.indexOfFirst{ it == "-cp" || it == "--computationPower" }
        if(cpIndex >= 0) computationPower = args[cpIndex + 1].toInt()
        else shutDown("computationPower not set. Use \"-cp <power>\" or \"--computationPower <power>\" as argument")
        try {
            DFService.register(this, DFAgentDescription().apply {
                addServices(ServiceDescription().apply {
                    type = "convert-video-file"
                    name = "JADE-converter"
                })
            })
        } catch (e: FIPAException){
            log(e.toString(), STD_ERR)
        }

        val template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(CFP))

        addBehaviour(object : ModernContractNetResponder(this, template){

            override fun onContractReceived(cfp: ACLMessage): ACLMessage {
                lateinit var reply: ACLMessage
                if(!working){
                    reply = cfp.createReply().apply {
                        performative = PROPOSE
                        content = convertContent(convertContent(cfp.content).apply { bid = computationPower })
                    }
                } else reply = cfp.createReply().apply {
                    performative = REFUSE
                    content = cfp.content
                }
                return reply
            }

            override fun onContractProposalAccepted(cfp: ACLMessage, propose: ACLMessage, accept: ACLMessage): ACLMessage {
                synchronized(working, {working = true})
                val job = convertContent(accept.content).content as Job
                executeJob(job)
                return accept.createReply().apply {
                    performative = INFORM
                }
            }

            override fun onContractProposalRejected(cfp: ACLMessage, propose: ACLMessage, reject: ACLMessage) {

            }

        })
    }

    private fun executeJob(job: Job) {
        val builder = FFmpegBuilder()
                .setInput(job.filePath)
                .overrideOutputFiles(true)
                .addOutput(job.filePath + "-converted.mp4")
                .setFormat("mp4")
                .apply {
                    when(job.videoCodec){
                        "h.264" -> setVideoCodec("libx264")
                        "h.256" -> setVideoCodec("libxh265")
                    }
                    when(job.audioCodec){
                        "aac" -> setAudioCodec("aac")
                    }
                }
                .setConstantRateFactor(30.0)
                .done()
        val executor = FFmpegExecutor(ffmpeg, ffprobe)
        val input = ffprobe.probe(job.filePath)
        val durationNs = input.getFormat().duration * TimeUnit.SECONDS.toNanos(1)
        executor.createJob(builder, {
            val percentage = it.out_time_ns / durationNs
            log("[${(percentage * 100).roundToInt()}%] status: ${it.status} | frame: ${it.frame} " +
                    "| time: ${FFmpegUtils.toTimecode(it.out_time_ns, TimeUnit.NANOSECONDS)}ms" +
                    "| fps: ${it.fps} | speed: ${it.speed}x")
        }).run()
    }

    override fun onDestroy() {

    }
}