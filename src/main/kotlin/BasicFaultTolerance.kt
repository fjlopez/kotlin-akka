
import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.actor.ActorRef.noSender
import akka.actor.ActorSystem.create
import akka.actor.Props.create
import akka.japi.pf.ReceiveBuilder
import java.lang.Thread.sleep
import java.util.*

fun main() {

    class DieException(message: String) : Exception(message)

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

    val actorSystem = create("basic-fault-tolerance")
    val actorRef = actorSystem.actorOf(create(ParentActor::class.java), "parent")
    sleep(500)
    actorSystem.log().info("Sending 'Tom'")
    actorRef.tell("Tom", ActorRef.noSender())
    actorSystem.log().info("Sending DIE message to child2")
    actorSystem.actorSelection("akka://example2/user/parent/child2").tell("DIE", noSender())
    sleep(500)
    actorSystem.terminate()
}