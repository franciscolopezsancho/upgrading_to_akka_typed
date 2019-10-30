package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.example.NonTuringMachine.{Present, Trick}

//why FSM and not infinite state machine? any implementation of that
object NonTuringMachine {

  //what options do we have
    //
  def apply(): Behavior[Command] =
    goodMood(Uninitialized)

  def goodMood(some: Command): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case Trick(joke) =>
          context.log.info(s"don't find it funny. Take your '$joke' joke back")
          badMood(message)
        case Present(content) =>
          context.log.info(s"thank you for the $content")
          Behavior.same
        case Uninitialized =>
          Behaviors.same
      }

    }

  def badMood(some: Command): Behavior[Command] = {
    Behaviors.receive { (context, message) =>
      message match {
        case Trick(joke) =>
          context.log.info(s"don't find it funny. Take your '$joke' joke back")
          Behavior.stopped
        case Present(content) =>
          context.log.info(s"thank you for the $content")
          Behavior.same
      }
    }
  }

  sealed trait Command

  case object Uninitialized extends Command
  //if not final what would happen? some other message you don't handle!
  final case class Present(content: String) extends Command
  final case class Trick(joke: String) extends Command

}

object NonTuringMachineApp extends App {

  //be aware of the signature here
  val system: ActorSystem[NonTuringMachine.Command] = ActorSystem(NonTuringMachine(), "helloWorldMain")
  system ! Trick("There is an English, an American and a French")
  system ! Present("chocolate")
  system ! Trick("There is an English, an American and a French")
  system ! Present("Million Pounds")

}
