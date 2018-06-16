package it.lamba

import com.google.gson.GsonBuilder
import it.lamba.data.MessageContent
import jade.core.AID
import jade.lang.acl.ACLMessage
import jade.proto.ContractNetInitiator
import java.util.*
import kotlin.collections.ArrayList

object Utils {
    val gson = GsonBuilder().setPrettyPrinting().create()
    fun convertContent(content: String) = gson.fromJson(content, MessageContent::class.java)!!
    fun convertContent(content: MessageContent) = gson.toJson(content)!!
}

fun <E> Collection<E>.maxByField(function: (item: E) -> Float): E? {
    var max: E? = null
    for(item in this){
        if(max == null) max = item
        else if(function(item) >= function(max)) max = item
    }
    return max
}

inline fun <reified E> Array<E>.allBut(item: E): Array<E> {
    val list = ArrayList<E>()
    forEach { if(item != it) list.add(it)}
    return list.toTypedArray()
}

fun <E> Array<E>.maxByField(function: (item: E) -> Float): E? {
    var max: E? = null
    for(item in this){
        if(max == null) max = item
        else if(function(item) >= function(max)) max = item
    }
    return max
}

fun <E> ArrayList<E>.sortedAdd(newItem: E, compareBy: (item: E) -> Float): Int {
    if(this.isEmpty()){
        this.add(0, newItem)
        return 0
    }
    else for(i in 0 until this.size + 1 step 1){
        if(compareBy(this[i]) <= compareBy(newItem)) {
            this.add(i, newItem)
            return i
        }
    }
    return -1
}

fun ACLMessage.addReceivers(convertersAID: Array<AID>) {
    for(aid in convertersAID) addReceiver(aid)
}