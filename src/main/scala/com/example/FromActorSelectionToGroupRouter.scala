//package com.example
//
//import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy}
//import akka.actor.typed.receptionist.ServiceKey
//import akka.actor.typed.scaladsl.{Behaviors, Routers}
//
//class FromActorSelectionToGroupRouter {
//
//}
//
//object Worker {
//
//  //??behavior?? why not apply?
//  val behavior: Behavior[Command] = Behaviors.setup { ctx =>
//    ctx.log.info("Starting worker")
//
//    Behaviors.receiveMessage {
//      case DoLog(text) =>
//        ctx.log.info("Got message {}", text)
//        Behaviors.same
//    }
//  }
//  val serviceKey = ServiceKey[Worker.Command]("log-worker")
//
//  sealed trait Command
//
//
//  // #pool
//
//  case class DoLog(text: String) extends Command
//
//
//
//}
//
//object WorkerApp extends App {
//
//  val system: ActorSystem[String] = ActorSystem(Worker(), "helloWorldMain")
//  system ! "Hello"
//
//
//}
//
//
//object Tester {
//
//  sealed trait Command
//  case object SpawnActor
//
//
//  def apply():Behavior[Command] = {
//   Behaviors.receive{ (context, message) =>
//     val pool = Routers.pool (poolSize = 4) (
//       // make sure the workers are restarted if they fail
//       Behaviors.supervise (Worker () ).onFailure[Exception] (SupervisorStrategy.restart) )
//     val router = context.spawn (pool, "worker-pool")
//     (0 to 10).foreach {
//
//       n =>
//         router ! Worker.DoLog (s"msg $n")
//   }
//     Behaviors.same
//  }
//}
//
//
//
//
//}
//
