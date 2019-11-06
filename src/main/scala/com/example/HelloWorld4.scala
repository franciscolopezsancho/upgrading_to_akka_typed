package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.example.HelloWorld4.{HallowingMessage, Treat, Trick}

//STATE
object HelloWorld4 {




  def apply(patience: Int): Behavior[HallowingMessage] =
    Behaviors.receive { (context, message) =>
      message match {
        case Trick(joke) =>
          if (patience > 0) {
            context.log.info(s" $joke is not too funny to be honest")
            apply(patience - 1)
            //we don't need explicit behavior here, it's implicit
            //is not recursion!, we return a Behavior here. So it won't be executed til next message is received
          } else {
            context.log.info("Please leave me alone")
            Behavior.stopped
          }
        case Treat(content) =>
          context.log.info(s"thank you for the $content")
          //apply(patience + 1)
          Behavior.same
      }
    }


  //more neat relation between classes....
  sealed trait HallowingMessage

  case class Treat(content: String) extends HallowingMessage

  case class Trick(joke: String) extends HallowingMessage


}


object HelloWorld4App extends App {

  val system: ActorSystem[HallowingMessage] = ActorSystem(HelloWorld4(1), "helloWorldMain")
  system ! Trick("There is an English, an American and a French")
  system ! Treat("chocolate")
  system ! Trick("There is an English, an American and a French")
  //system ! Trick("There is an English, an American and a French")
  system ! Treat("Million Pounds")
  //bare in mind how the system takes the message before it stops. Stopping is the actor not the App
  Thread.sleep(100)
  system.terminate()


}


