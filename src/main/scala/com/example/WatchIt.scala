package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, ChildFailed, Terminated}

import scala.concurrent.Await
import scala.concurrent.duration._

//In order to be notified when another actor terminates (i.e. stops permanently, not temporary failure and restart),
object MistressControlProgram {

  def apply(): Behavior[Command] = {
    Behaviors
      .receive[Command] { (context, message) =>
        message match {
          case SpawnJob(name, duration) =>
            context.log.info(s"Spawning a worker of type [$name] with duration $duration")
            val worker = context.spawn(SpecialisedWorker(name), s"$name-$duration")
            worker ! SpecialisedWorker.Task(duration)
            context.watch(worker)
            Behaviors.same
        }
      }
      .receiveSignal {
        //order matters!!
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


object SpecialisedWorker {
  def apply(name: String): Behavior[Command] = {
    Behaviors
      .receive[Command] { (context, message) => {
        message match {
          case Task(n) if n < 5 => {
            context.log.info(s"my name is $name, and I've done my duty")
            Behaviors.stopped
          }
          case _ => {
            context.log.info(s"my name is $name, and I'm doing my duty")
            //for some reason an exception occurs
            throw new IllegalStateException("I got too tired")
            Behaviors.stopped
          }
        }
      }
      }


  }

  sealed trait Command

  case class Task(duration: Int) extends Command

}


object MistressControlProgramApp extends App {

  import MistressControlProgram._

  val system: ActorSystem[Command] = ActorSystem(MistressControlProgram(), "MistressControlProgram")
  system ! SpawnJob("check", 1)
  system ! SpawnJob("analysis", 6)

  // gracefully stop the system
  Await.result(system.whenTerminated, 6.seconds)

}


