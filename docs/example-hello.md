---
layout: default
title: Kotkin + Akka: Hello Kotlin!
---
# Hello Kotlin!

Let’s create our first ‘Hello Kotlin’ actor:
```kotlin
import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef.noSender
import akka.actor.ActorSystem.create
import akka.actor.Props.create
import akka.japi.pf.ReceiveBuilder

fun main(args: Array<String>) {

    class HelloActor : AbstractLoggingActor() {
        override fun createReceive() = ReceiveBuilder()
            .match(String::class.java) {
                log().info("Hello $it!")
            }.build()
    }

    val actorSystem = create("example1")
    val actorRef = actorSystem.actorOf(
        create(HelloActor::class.java),
        "hello1"
    )
    actorSystem.log().info("Sending 'Tom'")
    actorRef.tell("Tom", noSender())
    Thread.sleep(1000)
    actorSystem.terminate()
}
```
What's going on?
1. `HelloActor` is an actor class whose base class mixes logging into the actor class.
1. `HelloActor::createReceive` method is configured to create a receiver able to handle any message of type `String` and then log a hello message.
1. `ActorSystem.create` creates the actor system. 
It is a heavyweight structure that will allocate *Threads*, so we should create one per logical application. 
1. `Props.create(...)` creates a *Props*, an immutable configuration object using in creating an actor given a class.
1. `actorRef = actorSystem.actorOf(...)` creates a new actor from the metadata contained in *Props*.
The actor will be an instance of the class `HelloActor` and its name will be `hello1`.
1. `actorRef.tell("Tom", noSender())` sends a message to the actor.
Method `tell` requires a sender reference. 
As there is no sender, we pass `noSender()` as sender reference.
1. `terminate()` shuts downs the actor system gracefully.

Running the above code will startup the actor system and print out the following output:
```
[INFO] [...] [main] [akka.actor.ActorSystemImpl(example1)] Sending 'Tom'
[INFO] [...] [example1-akka.actor.default-dispatcher-2] [akka://example1/user/hello1] Hello Tom!
```
Note that:
1. The log messages are form separate threads.
One of the threads are an Akka dispatcher.
1. The actor has an unique address based on the name of the actor system (`example1`) and the actor name (`hello1`).

The source code is available [here](https://github.com/fjlopez/kotlin-akka/blob/master/src/main/kotlin/Example1.kt).