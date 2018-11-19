---
layout: default
title: Kotkin + Akka: Extensions
---
# Extensions
Akka provides a mechanism for adding new features named 
[Akka Extensions](https://doc.akka.io/docs/akka/2.5/extending-akka.html).
scheduler that will put a message in the target Actor’s mailbox at a specified point in the future. 
Akka allows for scheduling a one-off task or a repeating task using `context.system.scheduler()`.

## Quartz Scheduler Extension

[Stephen Hopper](https://github.com/enragedginger)'s [Quartz Extension](https://github.com/enragedginger/akka-quartz-scheduler) 
is a popular extension that provides scheduling in Akka 2.5.x. based on [Quartz Scheduler 2.3.x](https://github.com/quartz-scheduler/quartz).
The Akka Scheduler is designed to setup [once off](https://fjlopez.github.io/kotlin-akka/intro-scheduling.html#once-off-task) 
or [repeating](https://fjlopez.github.io/kotlin-akka/intro-scheduling.html#repeating-task) events that happen based on durations from the current moment.
This extension uses quartz jobs to schedule messages in Akka.

The usage of this component first requires including the dependency into the project.
Then, it is necessary to create and access to the extension:
```kotlin
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

val actorSystem = context.system as ExtendedActorSystem
val scheduler = QuartzSchedulerExtension(actorSystem)
```
Next, we can schedule a job:
```kotlin
scheduler.schedule(
    "HelloKotlin",  // name of quartz job configured in akka config
    self,           // target actor
    "hello kotlin"  // message to send to target actor
)
```
An optional start date can be used for postponed start of a job. Defaults to now.

The configuration of this extensions is done inside the Akka configuration file in an `akka.quartz` block:
```
akka.quartz {
  schedules {
    HelloKotlin {
      description = "A Hello Kotlin Task!"
      expression = "*/1 * * ? * *"
    }
  }
}
```
In the above configuration, 
a job named `HelloKotlin` 
is scheduled to be fired every second forever.

The source code is available [here](https://github.com/fjlopez/kotlin-akka/blob/master/src/main/kotlin/ExtensionQuartzScheduler.kt).
