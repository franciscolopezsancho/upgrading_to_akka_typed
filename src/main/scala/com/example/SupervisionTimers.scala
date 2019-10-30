package com.example

import akka.actor.Timers
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors

object Counter {
  sealed trait Command
  case class Increment(nr: Int) extends Command
  case class GetCount(replyTo: ActorRef[Int]) extends Command

  def apply(): Behavior[Command] =
    Behaviors.supervise(counter(1)).onFailure(SupervisorStrategy.restart)
                                                  //SupervisorStrategy.restart.
                                                  //  withLimit(maxNrOfRetries = 10, withinTimeRange = 10.seconds))
  private def counter(count: Int): Behavior[Command] =
    Behaviors.receiveMessage[Command] {
      case Increment(nr: Int) =>
        counter(count + nr)
      case GetCount(replyTo) =>
        replyTo ! count
        Behaviors.same
    }
}


import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import com.example.NoJokeMan.{Present, Trick}
import scala.concurrent.duration._


object HelloWorld2 {

  def apply(): Behavior[String] =
    Behaviors.withTimers { timers =>
      Behaviors.receive { (context, message) =>
        context.log.info(s"the message is.... $message")
        //factory 2
        timers.startSingleTimer("xyz","I'm alive!!!", 1.seconds)

        Behavior.same
      }
    }
}
//Each timer has a key for cancelling,  ## timers.cancel("xyz")


object HelloWorldApp2 extends App {

  val system: ActorSystem[String] = ActorSystem(HelloWorld2(), "helloWorldMain")
  system ! "Hello"
  Thread.sleep(2000)
  system.terminate()

}




//Behaviors
//.supervise(behavior)
//.onFailure[IllegalStateException](


//#################
//Children can be kept alive on restart through
//Supervision.restart.withStopChildren(false)