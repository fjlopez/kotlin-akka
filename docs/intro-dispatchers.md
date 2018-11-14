---
layout: default
title: Kotkin + Akka: Dispatchers
---
# Dispatcher

The dispatcher defines the wiring of an actor within an actor system. 

The **default dispatcher** creates one mailbox per actor and is backed by a thread pool that can be shared across multiple actors.
It is recommended for non-blocking actors that perform short lived tasks. 

The **pinned dispatcher** creates one mailbox per actor backed by a single theraded pool that cannot be shared across actors .
It is recommended for long running, blocking actors.

The **calling thread dispatcher** creates one mailbox per actor per thread, i.e. the thread that sends a message to an actor is also used to deliver and process the message.
It creates a deterministic behaviour useful for testing.

The dispatcher behaviour can be defined programmatically or we can use a deployment configuration.
```
actor2-dispatcher {
  type = PinnedDispatcher
  executor = "thread-pool-executor"
}
```
The configuration can be loaded in the actor system and explictly set for an actor. 
```kotlin
import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef.noSender
import akka.actor.ActorSystem.create
import akka.actor.Props
import akka.japi.pf.ReceiveBuilder
import com.typesafe.config.ConfigFactory.parseResources

fun main() {
    class HelloActor : AbstractLoggingActor() {
        override fun createReceive() = ReceiveBuilder()
            .match(String::class.java) {
                log().info("Hello $it!")
            }.build()
    }

    val actorSystem = create("example3", parseResources("example3.conf"))
    val actorRef1 = actorSystem.actorOf(Props.create(HelloActor::class.java), "actor1")
    val actorRef2 =
        actorSystem.actorOf(
            Props.create(HelloActor::class.java).withDispatcher("actor2-dispatcher"), "actor2"
        )
    actorSystem.log().info("Sending 'Tom' and 'Mary'")
    actorRef1.tell("Tom", noSender())
    actorRef2.tell("Mary", noSender())
    Thread.sleep(1000)
    actorSystem.terminate()
}
```
Running the above code will startup the actor system and print out the following output:
```
[INFO] [...] [main] [akka.actor.ActorSystemImpl(example3)] Sending 'Tom' and 'Mary'
[INFO] [...] [example3-akka.actor.default-dispatcher-2] [akka://example3/user/actor1] Hello Tom!
[INFO] [...] [example3-actor2-dispatcher-5] [akka://example3/user/actor2] Hello Mary!
```

The source code is available [here](https://github.com/fjlopez/kotlin-akka/blob/master/src/main/kotlin/Example3.kt).