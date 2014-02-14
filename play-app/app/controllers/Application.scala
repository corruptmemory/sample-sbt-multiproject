package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.{Try, Success, Failure}
import java.util.concurrent.TimeoutException
import com.corruptmemory.artifact._
import scala.util.Random

object ExpensiveThing extends ExpensiveWebInterface

trait RequestTimeout { this:Controller =>
  import play.api.libs.concurrent.Promise
  val actionTimeout:FiniteDuration = Play.current.configuration.getMilliseconds("saks.action.timeout").map(_.milliseconds).getOrElse(2.seconds)

  def timeout(alternateBody: => SimpleResult = RequestTimeout("Request timeout"),
              maxTime:FiniteDuration = actionTimeout)(body: => SimpleResult):Future[SimpleResult] =
       timeoutFuture(alternateBody,maxTime)(Future(body))

  def timeoutFuture(alternateBody: => SimpleResult = RequestTimeout("Request timeout"),
                    maxTime:FiniteDuration = actionTimeout)(body: => Future[SimpleResult]):Future[SimpleResult] = {
    try {
      val f = body
      Await.ready(f, maxTime).value.get match {
        case Success(x:SimpleResult) => Future.successful(x)
        case Failure(t) => Future.failed(t)
      }
    } catch {
      case t:TimeoutException => Future.successful(alternateBody)
    }
  }

  // Support for a DSL that times-out everything
  class TimeoutActionDSL[A](bodyParser: BodyParser[A],
                            alternateBody: => SimpleResult,
                            maxTime:FiniteDuration) {
    def apply(block: => Future[SimpleResult]): Action[A] = Action.async(bodyParser)(_ => timeoutFuture(alternateBody,maxTime)(block))
    def apply(block: Request[A] => Future[SimpleResult]): Action[A] = Action.async(bodyParser)(r => timeoutFuture(alternateBody,maxTime)(block(r)))
  }

  final def timeoutAction(alternateBody: => SimpleResult = RequestTimeout("Request timeout"),
                          maxTime:FiniteDuration = actionTimeout): TimeoutActionDSL[AnyContent] =
                            timeoutAction(BodyParsers.parse.anyContent,alternateBody,maxTime)

  final def timeoutAction[A](bodyParser: BodyParser[A],
                             alternateBody: => SimpleResult = RequestTimeout("Request timeout"),
                             maxTime:FiniteDuration = actionTimeout): TimeoutActionDSL[A] =
                               new TimeoutActionDSL[A](bodyParser,alternateBody,maxTime)

}

object Application extends Controller with RequestTimeout {

  def index = Action.async {
    timeoutFuture() {
      ExpensiveThing.nextInt(Random.nextInt(100)).map { i =>
        Ok(views.html.index(s"The next int is: $i"))
      }
    }
  }

  // Same as above but using the DSL
  def index2 = timeoutAction()(ExpensiveThing.nextInt(Random.nextInt(100)).map { i =>
        Ok(views.html.index(s"The next int is: $i"))
      })
}