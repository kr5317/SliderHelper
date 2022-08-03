package com.kr5317.verify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlin.coroutines.CoroutineContext

@OptIn(ObsoleteCoroutinesApi::class)
object EventBus {
    private var channel = BroadcastChannel<Any>(102400)
    private val coroutineScope = ChannelScope()

    fun send(event: Any) = coroutineScope.launch {
        channel.send(event)
    }

    inline fun <reified T : Any> receive(noinline block: CoroutineScope.(T) -> Unit): Job {
        return receiveAny {
            if (it is T) {
                block(it)
            }
        }
    }

    fun receiveAny(
        block: suspend CoroutineScope.(Any) -> Unit
    ): Job {
        return coroutineScope.launch {
            for (bus in channel.openSubscription()) {
                block(bus)
            }
        }
    }
}

class ChannelScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()
}

@Composable
inline fun <reified T : Any> EventBusReceive(noinline block: CoroutineScope.(T) -> Unit) {
    DisposableEffect(Unit, effect = {
        val job = EventBus.receive<T> { block(it) }
        onDispose {
            job.cancel()
        }
    })
}