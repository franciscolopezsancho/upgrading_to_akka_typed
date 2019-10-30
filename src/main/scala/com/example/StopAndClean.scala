package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, Logger, PostStop}

import scala.concurrent.Await
import scala.concurrent.duration._


object MasterControlProgram {

  def apply(): Behavior[Command] = {
    Behaviors.receive[Command] {
      (context, message) =>
        message match {
          case SpawnJob(jobName) =>
            context.log.info("Spawning job {}!", jobName)
            context.spawn(Job(jobName), name = jobName)
            Behaviors.same
          case GracefulShutdown =>
            context.log.info("Initiating graceful shutdown...")
            // perform graceful stop, executing cleanup before final system termination
            // behavior executing cleanup is passed as a parameter to Actor.stopped
            Behaviors.stopped {
              () =>
                cleanup(context.system.log)
            }
        }
    }
      //what's is receive signal??
      //Notice it's missing Behavior as is a method that adds functionality to that one
      .receiveSignal {
      case (context, PostStop) =>
        context.log.info("Master Control Program stopped")
        Behaviors.same
    }
  }

  // Predefined cleanup operation
  def cleanup(log: Logger): Unit = log.info("Cleaning up!")

  sealed trait Command

  final case class SpawnJob(name: String) extends Command

  final case object GracefulShutdown extends Command

}

object Job {

  def apply(name: String): Behavior[Command] = {
    Behaviors.receiveSignal[Command] {
      case (context, PostStop) =>
        context.log.info("Worker {} stopped", name)
        Behaviors.same
    }
  }

  sealed trait Command

}
















object MasterControlProgramApp extends App {

  import MasterControlProgram._

  val system: ActorSystem[MasterControlProgram.Command] = ActorSystem(MasterControlProgram(), "MCP")
  system ! SpawnJob("a")
  system ! SpawnJob("b")
  Thread.sleep(100)
  // gracefully stop the system
  system ! GracefulShutdown
  Thread.sleep(100)
  Await.result(system.whenTerminated, 3.seconds)

}



























