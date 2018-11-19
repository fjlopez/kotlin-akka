import akka.actor.AbstractLoggingActor
import akka.actor.ActorSystem.create
import akka.actor.ExtendedActorSystem
import akka.actor.Props.create
import akka.japi.pf.ReceiveBuilder
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory.parseResources

fun main(args: Array<String>) {
    class ScheduledActor : AbstractLoggingActor() {
        override fun preStart() {
            super.preStart()
            val actorSystem = context.system as ExtendedActorSystem
            val scheduler = QuartzSchedulerExtension(actorSystem)
            scheduler.schedule("HelloKotlin", self, "hello kotlin")
        }

        override fun createReceive(): Receive {
            return ReceiveBuilder()
                .match(String::class.java) { log().info(it) }
                .build()
        }
    }

    val actorSystem = create("extension-quartz-scheduler", parseResources("extension-quartz-scheduler.conf"))
    actorSystem.actorOf(create(ScheduledActor::class.java), "scheduled-actor")
    Thread.sleep(5000)
    actorSystem.terminate()
}