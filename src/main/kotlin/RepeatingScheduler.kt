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
