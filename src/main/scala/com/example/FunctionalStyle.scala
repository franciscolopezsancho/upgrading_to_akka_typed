//package com.example
//
//import akka.actor.typed.scaladsl.Behaviors
//import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
//import com.example.HelloWorldBot.BotGreet
//
//class FunctionalStyle {
//
//}
//
//
//object HelloWorld {
//
//  def apply(): Behavior[Greet] = Behaviors.receive { (context, greetings) =>
//    context.log.info("Did you say? '{}'", greetings.message)
//    greetings.replyTo ! Greet(s"${greetings.message} back at you!", context.self)
//    Behaviors.stopped
//  }
//
//  //create diff messages
//  final case class Greet(message: String, replyTo: ActorRef[Greet])
//
//}
//
//object HelloWorldGuardian {
//
//  def apply(): Behavior[Start] =
//    Behaviors.setup { context =>
//      Behaviors.receiveMessage { message =>
//        val person1 = context.spawn(HelloWorld(), message.person1)
//        val person2 = context.spawn(HelloWorld(), message.person2)
//        person1 ! HelloWorld.Greet(message.person1, person2)
//        Behaviors.same
//      }
//    }
//
//  final case class Start(person1: String, person2: String)
//
//}
//
//
//object FunctionlAkkaQuickstart1 extends App {
//
//  import HelloWorldGuardian.Start
//
//  // Create the 'helloAkka' actor system
//  val system: ActorSystem[Start] = ActorSystem(HelloWorldGuardian(), "helloWorldMain")
//
//  system ! Start("john", "janet")
//
//
//}
//
//object HelloWorldBot {
//
//  def apply(max: Int): Behavior[BotGreet] = {
//    bot(0, max)
//  }
//
//  private def bot(greetingCounter: Int, max: Int): Behavior[BotGreet] =
//    Behaviors.receive { (context, greetings) =>
//      val n = greetingCounter + 1
//      context.log.info("Did you say? '{}'", greetings.message) //#fiddle_code
//      if (n == max) {
//        Behaviors.stopped
//      } else {
//        greetings.replyTo ! BotGreet(s"${greetings.message} back at you!", context.self)
//        bot(n, max)
//      }
//    }
//
//  final case class BotGreet(message: String, replyTo: ActorRef[BotGreet])
//
//}
//
//object HelloWorldGuardianBot {
//
//  def apply(): Behavior[StartBot] =
//    Behaviors.setup { context =>
//      val jane = context.spawn(HelloWorldBot(max = 2), "Jane")
//      val john = context.spawn(HelloWorldBot(max = 2), "John")
//      Behaviors.receiveMessage { message =>
//        jane ! BotGreet("How you doing!!", john)
//        Behaviors.same
//      }
//    }
//
//  //has to be a class???
//  case class StartBot()
//
//}
//
//object FunctionlAkkaQuickstart2 extends App {
//
//  import HelloWorldGuardianBot.StartBot
//
//  val system: ActorSystem[StartBot] = ActorSystem(HelloWorldGuardianBot(), "helloWorldMain")
//  system ! HelloWorldGuardianBot.StartBot()
//
//
//}
//
//
//
