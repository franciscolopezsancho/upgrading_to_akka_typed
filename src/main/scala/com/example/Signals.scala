package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, PostStop}
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, PostStop}
import org.slf4j.Logger

object MasterControlProgram {
    sealed trait Command
    final case class SpawnJob(name: String) extends Command
    final case object GracefulShutdown extends Command
    final case class Cleaned(actor: ActorRef[Job.Command]) extends Command

    // Predefined cleanup operation

    def apply(jobs: Seq[ActorRef[Job.Command]]): Behavior[Command] = {
      Behaviors
        .receive[Command] { (context, message) =>
          message match {
            case SpawnJob(jobName) =>
              context.log.info("Spawning job {}!", jobName)
              val job = context.spawn(Job(jobName), name = jobName)
              // job ! Job.Shutdown(context.self)
              apply(job +: jobs)
            case GracefulShutdown =>
              context.log.info("Initiating graceful shutdown...")
              // perform graceful stop, executing cleanup before final system termination
              // behavior executing cleanup is passed as a parameter to Actor.stopped
              jobs.map(_ ! Job.Shutdown(context.self))
              Behaviors.same
            case Cleaned(job) =>
              val runningJobs = jobs.filterNot(_.path == job.path)
              if (runningJobs.isEmpty)
                Behaviors.stopped
              else
                apply(runningJobs)
          }
        }
        
    }
  }
  //#master-actor

  //#worker-actor

  object Job {
    sealed trait Command
    case class Shutdown(replyTo: ActorRef[MasterControlProgram.Command]) extends Command

    def cleanup(log: Logger): Unit =
      log.info("Cleaning up!")

    def apply(name: String): Behavior[Command] = {
      Behaviors
        .receive[Command] { (context, message) =>
          message match {
            case Shutdown(replyTo: ActorRef[MasterControlProgram.Command]) =>
              Behaviors.stopped { () =>
                cleanup(context.system.log)
                replyTo ! MasterControlProgram.Cleaned(context.self)
              }
          }
        }
        .receiveSignal {
          case (context, PostStop) =>
            context.log.info("Worker {} stopped", name)
            Behaviors.same
        }
    }
  }

object MasterControlProgramApp extends App {

  import MasterControlProgram._

  val system: ActorSystem[MasterControlProgram.Command] =
    ActorSystem(MasterControlProgram(Seq.empty), "MCP")

  system ! SpawnJob("a")
  system ! SpawnJob("b")
  Thread.sleep(100)
  // gracefully stop the system

  system ! GracefulShutdown
  Thread.sleep(100)
 Await.result(system.whenTerminated, 10.seconds)
  system.terminate()

}
