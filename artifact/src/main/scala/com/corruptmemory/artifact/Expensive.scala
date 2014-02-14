package com.corruptmemory.artifact

import akka.actor.{ ActorRef, Props, ActorSystem, Actor }
import akka.pattern.{ ask, pipe }
import scala.util.Random
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.util.Timeout

class Expensive extends Actor {
  def receive:Receive = {
    case x:Int =>
      Thread.sleep(Random.nextInt(4)*1000)
      sender ! x + 1
  }
}

class ExpensiveWebInterface {
  private val system = ActorSystem("expensive")
  private val expensive = system.actorOf(Props(classOf[Expensive]))
  import system.dispatcher

  implicit private val timeout = Timeout(30.seconds)

  def nextInt(x:Int):Future[Int] = {
    ask(expensive, x).mapTo[Int]
  }

  def shutdown():Unit = {
    system.shutdown()
  }

}