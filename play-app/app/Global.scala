import play.api._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("----- Application is started!!!")
  }

  override def onStop(app: Application) {
    controllers.ExpensiveThing.shutdown()
    Logger.info("----- Application is stopping!!!")
  }
}