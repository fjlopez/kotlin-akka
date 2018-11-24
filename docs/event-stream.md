---
layout: default
title: Kotkin + Akka: Event Stream
---
# Event Stream

Akka provides an event bus for actors belonging to the same actor system. 
Actors subscribe to channels on the actor system event stream .
Next, the event is placed in the actor’s mailbox if the subscription type matches the message.

The subscription channels are simple message types.
Any message published to the event bus which is the type, or the subtype, of the channel is forwarded to the actor ref.
Actors can unsubscribe from the subscription channels. 

```kotlin
import akka.actor.AbstractLoggingActor
import akka.actor.ActorSystem.create
import akka.actor.Props.create
import akka.japi.pf.ReceiveBuilder

fun main() {

    open class Event
    class Event1 : Event()
    class Event2 : Event()

    class LoggingActor : AbstractLoggingActor() {
        override fun createReceive() =
            ReceiveBuilder()
                .match(Event1::class.java) { log().info("Received Event 1") }
                .match(Event2::class.java) { log().info("Received Event 2") }
                .build()
    }

    fun createActor() = create(LoggingActor::class.java)

    with(create("event-stream")) {
        val loggerRef1 = actorOf(createActor(), "Logger1")
        val loggerRef2 = actorOf(createActor(), "Logger2")
        val loggerRef3 = actorOf(createActor(), "Logger3")

        with(eventStream()) {
            subscribe(loggerRef1, Event::class.java)
            subscribe(loggerRef2, Event1::class.java)
            subscribe(loggerRef3, Event2::class.java)
            publish(Event1())
            publish(Event2())
            unsubscribe(loggerRef2, Event1::class.java)
            unsubscribe(loggerRef3, Event2::class.java)
            publish(Event2())
        }
        Thread.sleep(500)
        terminate()
    }
}
```
In the above example, `loggerRef1` will receive messages of type `Event`, `Event1` and `Event2`.
`loggerRef2` and `loggerRef3` will receive only messages of type `Event1` and `Event2` respectivelly.
Later both `loggerRef2` and `loggerRef3` are unsubscribed.

Actors can access to the event stream by `context.system.eventStream()`.