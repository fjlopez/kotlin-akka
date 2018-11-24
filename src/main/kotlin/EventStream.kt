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