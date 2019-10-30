package com.example

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{Behaviors, Routers}
import akka.actor.typed.{ActorSystem, Behavior}


object GroupRouterWorker {

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

object GroupRouterGuardian {
  //I NEED TO REGISTER THEM

  def apply(): Behavior[Command] = Behaviors.receive {
    (context, message) =>
      message match {
        case Start =>
          val serviceKey = ServiceKey[GroupRouterWorker.Command]("log-worker")

          // this would likely happen elsewhere - if we create it locally we
          // can just as well use a pool
          (0 to 3).foreach { n =>
            val worker = context.spawn(GroupRouterWorker(), s"worker-$n")
            context.system.receptionist ! Receptionist.Register(serviceKey, worker)
          }

          val group = Routers.group(serviceKey)
          val router = context.spawn(group, "worker-group")
          Thread.sleep(10)
          // note that since registration of workers goes through the receptionist there is no
          // guarantee the router has seen any workers yet if we hit it directly like this and
          // these messages may end up in dead letters - in a real application you would not use
          // a group router like this
          (0 to 10).foreach { n =>
            router ! GroupRouterWorker.DoLog(s"msg $n")

          }

          Behaviors.same
      }
  }

  sealed trait Command

  case object Start extends Command

}

object GroupRouterWorkerApp extends App {

  val system = ActorSystem(GroupRouterGuardian(), "worker")
  system ! GroupRouterGuardian.Start
}


