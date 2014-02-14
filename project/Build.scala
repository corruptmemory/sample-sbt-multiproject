import sbt._
import Keys._
import play.Project._

object SampleProject extends Build {

  val appVersion = "0.1"

  def defaultSettings: Seq[Setting[_]] =
    Seq(
      organization := "com.corruptmemory",
      version := appVersion,
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
      scalaVersion := "2.10.3"
    )


  lazy val playApp = (
    play.Project("play-app", path = file("play-app"))
      settings(defaultSettings:_*)
      dependsOn(artifact)
  )

  lazy val artifact = Project("artifact", base = file("artifact")).settings(defaultSettings:_*).
                        settings(
                          libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % "2.2.1")
                        )
}
