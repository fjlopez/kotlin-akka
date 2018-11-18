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

