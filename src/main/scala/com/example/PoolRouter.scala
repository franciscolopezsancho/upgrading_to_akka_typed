package com.example


import akka.actor.typed.scaladsl.{Behaviors, Routers}
import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy}

object Worker {

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("Starting worker")

    Behaviors.receiveMessage {
      case DoLog(text) =>
        context.log.info(s"${context.self.path.name} Got message {}", text)
        Behaviors.same
    }
  }

  sealed trait Command

  case class DoLog(text: String) extends Command

}

object WorkerGuardian {
  def apply(): Behavior[Command] = Behaviors.receive {
    (context, message) =>
      message match {
        case Start =>
          //Supervision could be added here
          val pool = Routers.pool(poolSize = 4)(Worker())
          val router = context.spawn(pool, "worker-pool")
          (0 to 10).foreach { n =>
            router ! Worker.DoLog(s"msg $n")
          }
          Behaviors.same
      }

  }

  sealed trait Command

  case object Start extends Command

}

object WorkerApp extends App {

  val system = ActorSystem(WorkerGuardian(), "worker")
  system ! WorkerGuardian.Start
}




