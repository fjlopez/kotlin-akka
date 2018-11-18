---
layout: default
title: Kotkin + Akka: Scheduling
---
# Scheduling
Akka provides a scheduler that will put a message in the target Actor’s mailbox at a specified point in the future. 
Akka allows for scheduling a one-off task or a repeating task using `context.system.scheduler()`.

## Once Off Task
You can schedule a **Once Off Task** using the method `scheduleOnce` of the scheduler.
This method requires 
a finite delay, 
a target reference, 
a message to put in its mailbox,
an execution context (e.g. the dispatcher of the actor system), and
a sender reference or `Actor.noSender`.
The following example with dispatch the once off message to the target after the immediate message.
```kotlin
import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef.noSender
import akka.actor.ActorSystem.create
import akka.actor.Props.create
import akka.japi.pf.ReceiveBuilder
import java.time.Duration.ofMillis

fun main() {
    data class ScheduledMessage(val message: String)
    class ScheduledActor : AbstractLoggingActor() {
        override fun createReceive(): Receive {
            return ReceiveBuilder()
                .match(ScheduledMessage::class.java) {
                    with(context.system) {
                        scheduler().scheduleOnce(ofMillis(500), self, it.message, dispatcher(), self)
                    }
                }
                .match(String::class.java) { log().info(it) }
                .build()
        }
    }

    val actorSystem = create("once-off-scheduler")
    val repeatingScheduler = actorSystem.actorOf(create(ScheduledActor::class.java), "scheduled-actor")
    repeatingScheduler.tell(ScheduledMessage("once off message"), noSender())
    repeatingScheduler.tell("immediate message", noSender())
    Thread.sleep(1000)
    actorSystem.terminate()
}
```
The source code is available [here](https://github.com/fjlopez/kotlin-akka/blob/master/src/main/kotlin/OnceOffScheduler.kt).
## Repeating Task
You can schedule a **Repeating Task** that periodically sends a message to a target Actor in fixed time intervals using the method `schedule` of the scheduler.
This method requires
an initial delay,
an interval,
a target reference,
a message to put in its mailbox,
an execution context (e.g. the dispatcher of the actor system), and
a sender reference or `Actor.noSender`.
The call returns `akka.actor.Cancellable` which allows to cancel the repeating task.
The following example with dispatch the message each 100ms until its scheduling is explicitly cancelled.
```kotlin
import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef.noSender
import akka.actor.ActorSystem
import akka.actor.Cancellable
import akka.actor.Props.create
import akka.japi.pf.ReceiveBuilder
import java.time.Duration.ofMillis

fun main() {

    data class ScheduledMessage(val message: String)

    class ScheduledActor : AbstractLoggingActor() {

        private lateinit var cancellable: Cancellable

        override fun postStop() {
            super.postStop()
            log().info("Cancelling the repeating task")
            cancellable.cancel()
        }

        override fun createReceive(): Receive {
            return ReceiveBuilder()
                .match(ScheduledMessage::class.java) {
                    cancellable = with(context.system) {
                        scheduler().schedule(ofMillis(0), ofMillis(100), self, it.message, dispatcher(), self)
                    }
                }
                .match(String::class.java) { log().info(it) }
                .build()
        }
    }

    val actorSystem = ActorSystem.create("repeating-scheduler")
    val repeatingScheduler = actorSystem.actorOf(create(ScheduledActor::class.java), "scheduled-actor")
    repeatingScheduler.tell(ScheduledMessage("repeated task"), noSender())
    Thread.sleep(500)
    actorSystem.stop(repeatingScheduler)
    Thread.sleep(1000)
    actorSystem.terminate()
}
```
The source code is available [here](https://github.com/fjlopez/kotlin-akka/blob/master/src/main/kotlin/RepeatingScheduler.kt).
