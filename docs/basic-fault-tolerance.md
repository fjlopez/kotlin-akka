---
layout: default
title: Kotkin + Akka: Basic tault tolerance
---
# Basic fault tolerance
An important concept to understand is that Actors are hierarchical, 
which means that any Actor created is a child of another Actor. 
This hierarchy gives parent Actors the ability to _supervise_ child Actor in the event of an exception, 
which is a key feature of Akka [Fault Tolerance](https://en.wikipedia.org/wiki/Fault_tolerance).

Let’s demonstrate this feature. 
First we create a `ChildActor` that will either throw an `Exception` or simply log the message:
```kotlin
class ChildActor(val name: String) : AbstractLoggingActor() {
    override fun preStart() {
        log().info("$name is initialized")
    }

    override fun createReceive() = log().info("$name has a new receiver").let {
        ReceiveBuilder()
            .match(String::class.java) { message ->
                if ("DIE" == message) throw DieException("Someone killed $name!")
                log().info("Hello $message!")
            }
            .build()
    }

    override fun preRestart(reason: Throwable?, message: Optional<Any>?) {
        log().info("before restarting $name ensure ...")
        super.preRestart(reason, message)
        log().info("... and then restart $name")
    }

    override fun postRestart(reason: Throwable?) {
        log().info("but after restarting $name ensure ...")
        super.postRestart(reason)
        log().info("... and then $name is ready")
    }

    override fun postStop() {
        log().info("$name is stopped")
    }

}
```
Next we create our `ParentActor` which creates 3 `ChildActor`. 
If any child throws a non-Akka `Exception`, the parent actor will restart the child:
```kotlin
class ParentActor : AbstractLoggingActor() {

    override fun preStart() {
        super.preStart()
        (1..3).forEach { context.actorOf(create(ChildActor::class.java, "child$it"), "child$it") }
    }

    override fun createReceive() =
        ReceiveBuilder()
            .match(String::class.java) { context.children.forEach { child -> child.tell(it, self()) } }
            .build()
}
```
We now create our actor system and send a normal message and a kill message so we can test the :
```kotlin
val actorSystem = create("example2")
val actorRef = actorSystem.actorOf(create(ParentActor::class.java), "parent")
sleep(500)
actorSystem.log().info("Sending 'Tom'")
actorRef.tell("Tom", ActorRef.noSender())
actorSystem.log().info("Sending DIE message to child2")
actorSystem.actorSelection("akka://example2/user/parent/child2").tell("DIE", noSender())
sleep(500)
actorSystem.terminate()
```
Notice how, when `child2` throws an `Exception` (e.g. `DieException`), 
it is restarted by the default supervision strategy of the parent.
The default supervision strategy is equivalent to the following `when` expression.
`stop()` and `restart()` are `SupervisorStrategy` objects:
 ```kotlin
when (it) {
 is ActorInitializationException -> stop()
 is ActorKilledException -> stop()
 is DeathPactException -> stop()
 else -> restart()
}
```` 
There are a few callbacks that are called here:
- `preRestart()` by default it disposes of all children and then calls `postStop()`.
- `postRestart()` by default it calls `preStart()`.

The source code is available [here](../src/main/kotlin/Example2.kt).