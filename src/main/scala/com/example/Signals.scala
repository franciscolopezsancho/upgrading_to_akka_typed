package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, PostStop}
import akka.util.Timeout
import com.example.MasterControlProgram.Cleaned

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}


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
            import scala.concurrent.duration.{SECONDS => secs}
            implicit val timeout: Timeout = scala.concurrent.duration.FiniteDuration(3,secs)
            val cleanser = context.spawn(Job("cleanser"),"cleanser")
            Behaviors.stopped {
              () =>
                context.log.info("starting to clean")
                context.ask(cleanser)(Job.Clean) {
                  //this will send the message to self
                  case Success(_) => Cleaned
                  case Failure(ex) => Cleaned
                }
            }
          case Cleaned =>
            context.log.info("All has been cleaned")
            Behaviors.same
        }
    }
      //Notice it's missing Behavior as is a method that adds functionality to that one
      .receiveSignal {
        case (context, PostStop) =>
          context.log.info("Master Control Program stopped")
          Behaviors.same
      }
  }

  // Predefined cleanup operation

  sealed trait Command

  final case class SpawnJob(name: String) extends Command

  final case object GracefulShutdown extends Command

  final case object Cleaned extends Command

}

object Job {

  def apply(name: String): Behavior[Command] = {
    Behaviors.receive[Command] {
      case (context,message) => message match {
        case Clean(replyTo: ActorRef[MasterControlProgram.Command]) =>
          context.log.info("cleaning")
          replyTo ! Cleaned
          Behaviors.stopped
      }

    }
    Behaviors.receiveSignal[Command] {
      case (context, PostStop) =>
        context.log.info("Worker {} stopped", name)
        Behaviors.same
    }
  }

  sealed trait Command
  final case class Clean(actorRef: ActorRef[MasterControlProgram.Command]) extends Command
  final case class Finished(message: String) extends Command

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
  Await.result(system.whenTerminated, 10.seconds)

}



























