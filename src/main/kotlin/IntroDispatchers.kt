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

    val actorSystem = create("intro-dispatchers", parseResources("intro-dispatchers.conf"))
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