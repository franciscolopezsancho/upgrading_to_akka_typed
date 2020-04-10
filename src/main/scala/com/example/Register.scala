package com.example

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.ActorSystem

object PingService {
  val PingServiceKey = ServiceKey[Ping]("pingService")

  final case class Ping(replyTo: ActorRef[Pong.type])
  final case object Pong

  def apply(): Behavior[Ping] = {
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.Register(PingServiceKey, context.self)
      Behaviors.receiveMessage {
        case Ping(replyTo) =>
          context.log.info("Pinged by {}", replyTo)
          replyTo ! Pong
          Behaviors.same
      }
    }
  }
}




