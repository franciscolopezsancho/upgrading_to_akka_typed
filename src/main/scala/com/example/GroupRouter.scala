package com.example

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{Behaviors, Routers}
import akka.actor.typed.{ActorSystem, Behavior}


object GRWorker {

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("Starting worker")

    Behaviors.receiveMessage {
      case DoLog(text) =>
        context.log.info("Got message {}", text)
        Behaviors.same
    }
  }

  sealed trait Command

  case class DoLog(text: String) extends Command

}
object GroupHolderRouter {
  def apply(): Behavior[Command] = Behaviors.receive {
    (context, message) =>
      message match {
        case Route =>

          val group = Routers.group(GRGuardian.serviceKey)
          val router = context.spawn(group, "worker-group")
          Thread.sleep(10)
          // note that since registration of workers goes through the receptionist there is no
          // guarantee the router has seen any workers yet if we hit it directly like this and
          // these messages may end up in dead letters - in a real application you would not use
          // a group router like this
          (0 to 10).foreach { n =>
            router ! GRWorker.DoLog(s"msg $n")
          }

          Behaviors.same
      }
  }
  sealed trait Command

  case object Route extends Command
}


object GRGuardian {
  //I NEED TO REGISTER THEM
  val serviceKey = ServiceKey[GRWorker.Command]("log-worker")

  import GroupHolderRouter._

  def apply(): Behavior[Command] = Behaviors.receive {
    (context, message) =>
      message match {
        case Start =>
          (0 to 3).foreach { n =>
            val worker = context.spawn(GRWorker(), s"worker-$n")
            context.system.receptionist ! Receptionist.Register(serviceKey, worker)
          }
          context.spawn(GroupHolderRouter(), "router") ! Route
          Behaviors.same
      }
  }

  sealed trait Command

  case object Start extends Command

}

object GRWorkerApp extends App {

  val system = ActorSystem(GRGuardian(), "worker")
  system ! GRGuardian.Start

}


