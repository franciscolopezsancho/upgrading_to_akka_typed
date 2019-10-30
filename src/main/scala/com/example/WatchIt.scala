package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, ChildFailed, Terminated}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

//In order to be notified when another actor terminates (i.e. stops permanently, not temporary failure and restart),
object MistressControlProgram {

  def apply(): Behavior[Command] = {
    Behaviors
      .receive[Command] { (context, message) =>
      message match {
        case SpawnJob(name,duration) =>
          context.log.info("Spawning job with priority {}!", duration)
          val job = context.spawn(Jab(name),s"$name-$duration"  )
          job ! Jab.Start(duration)
          context.watch(job)
          Behaviors.same
      }
    }
      .receiveSignal {
        case (context, ChildFailed(ref)) =>
          context.log.info(s"My children job ${ref._1} stopped with Exception ${ref._2.getMessage}")
          Behaviors.stopped
        case (context, Terminated(ref)) =>
          context.log.info(s"My children job ${ref.path.name} stopped")
          Behaviors.same
          //missing this sample but we need Guardian for this

      }

  }

  sealed trait Command

  final case class SpawnJob(name: String, duration: Int) extends Command

}

object Jab {

  def apply(name: String): Behavior[Command] = {
    Behaviors
      .receive[Command] { (context, message) => {
      message match {
        case Start(n) if n < 5 => {
          context.log.info(s"my name is $name, and I've done my duty")
          Behaviors.stopped
        }
        case _ => {
          context.log.info(s"my name is $name, and I'm doing my duty")
          //for some reason an exception occurs
          throw new IllegalArgumentException("illegal")
          Behaviors.stopped
        }
      }
    }
    }



  }
  sealed trait Command

  case class Start(duration: Long) extends Command
}



object MistressControlProgramApp extends App {

  import MistressControlProgram._

  val system: ActorSystem[Command] = ActorSystem(MistressControlProgram(), "MSC")
  system ! SpawnJob("check",1)
  system ! SpawnJob("analysis",6)
  // gracefully stop the system
  Await.result(system.whenTerminated, 6.seconds)

}


