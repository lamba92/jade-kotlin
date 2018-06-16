package it.lamba.data

import java.util.concurrent.LinkedBlockingQueue

data class MessageContent(var id: String, var description: String, var bid: Int = 0, var content: Job? = null)

data class Job(val filePath: String, val videoCodec: String, val audioCodec: String){
    val creationTime = System.currentTimeMillis()
}

class JobsQueue {
    val queue = LinkedBlockingQueue<Job>()
    private var listening: Boolean = false
    private var readyForNext = false

    private lateinit var jobAddedListener: (job: Job) -> Unit

    fun setOnJobAddedListener(onJobAdded: (job: Job) -> Unit){
        jobAddedListener = onJobAdded
        listening = true
        readyForNext = true
        processQueue()
    }

    fun readyForNext(){
        readyForNext = true
        processQueue()
    }

    fun removeOnJobAddedListener() {
        listening = false
        jobAddedListener = {}
    }

    fun addJob(job: Job){
        queue.add(job)
        processQueue()
    }
    @Synchronized
    private fun processQueue(){
        if(queue.isNotEmpty() && ::jobAddedListener.isInitialized){
            readyForNext = false
            jobAddedListener(queue.poll())
        }
    }

}
