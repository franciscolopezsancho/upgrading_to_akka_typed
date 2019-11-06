package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}

object Counter {

  def apply(): Behavior[Command] =
    //wrapping behaviors, another flavour
    Behaviors.supervise(counter(1)).onFailure(SupervisorStrategy.restart)
  //SupervisorStrategy.restart.withLimit(maxNrOfRetries = 10, withinTimeRange = 10.seconds))

  private def counter(count: Int): Behavior[Command] =
    Behaviors.supervise(
      Behaviors.receive[Command] { (context, message) =>
        message match  {
          case Increment(nr: Int) =>
            context.log.info(s"$count and counting")
            throw new IllegalStateException("just testing")
            counter(count + nr)
          case GetCount(replyTo) =>
            context.log.info(s"$count and counting")
            replyTo ! count
            Behaviors.same
        }
    }).onFailure(SupervisorStrategy.restart)

  sealed trait Command

  case class Increment(nr: Int) extends Command

  case class GetCount(replyTo: ActorRef[Int]) extends Command

}

object CounterApp2 extends App {

  import com.example.Counter._

  val system: ActorSystem[Command] = ActorSystem(Counter(), "helloWorldMain")
  system ! Increment(0)
  system ! Increment(0)
  Thread.sleep(2000)
  system.terminate()

}


import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

import scala.concurrent.duration._


object HelloWorld2Guardian {

  def apply(): Behavior[String] =
    Behaviors.withTimers { timers =>
      Behaviors.receive { (context, message) =>
        context.log.info(s"the message is.... $message")
        //factory 2
        timers.startSingleTimer("xyz", "I'm alive!!!", 1.seconds)

        Behavior.same
      }
    }
}

//Each timer has a key for cancelling,  ## timers.cancel("xyz")


object HelloWorldApp2 extends App {

  val system: ActorSystem[String] = ActorSystem(HelloWorld2Guardian(), "helloWorldMain")
  system ! "Hello"
  Thread.sleep(2000)
  system.terminate()

}





//################# KEEP CHILDREN ALIVE
//Children can be kept alive on restart through
//Supervision.restart.withStopChildren(false)

//################# SYSTEM SCHEDULER
///context.system.scheduler....