package com.example

import akka.actor.ActorLogging
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

class Coexistance {

}
//The magic lines.... more the second
import akka.{ actor => untyped }
import akka.actor.typed.scaladsl.adapter._

object UntypedWatchingTypedSpec {
  object Untyped {
    def props() = untyped.Props(new Untyped)
  }

  //#untyped-watch
  class Untyped extends untyped.Actor with ActorLogging {
    //an untyped ActorSystem. This can be converted to a typed ActorSystem
    //context has an implicit system
    val second: ActorRef[Typed.Command] =
      context.spawn(Typed.behavior, "second")

    context.watch(second)

    // self can be used as the `replyTo` parameter here because
    // there is an implicit conversion from akka.actor.ActorRef to
    // akka.actor.typed.ActorRef
    second ! Typed.Ping(self)

    override def receive = {
      case Typed.Pong =>
        log.info(s"$self got Pong from ${sender()}")
        // context.stop is an implicit extension method
        context.stop(second)
      case untyped.Terminated(ref) =>
        log.info(s"$self observed termination of $ref")
        context.stop(self)
    }
  }
  //#untyped-watch

  //#typed
  object Typed {
    sealed trait Command
    final case class Ping(replyTo: ActorRef[Pong.type]) extends Command
    case object Pong

    val behavior: Behavior[Command] =
      Behaviors.receive { (context, message) =>
        message match {
          case Ping(replyTo) =>
            context.log.info(s"${context.self} got Ping from $replyTo")
            // replyTo is an untyped actor that has been converted for coexistence
            replyTo ! Pong
            Behaviors.same
        }
      }
  }
  //#typed
}

//"Typed supervising untyped" should {
//    "default to restart