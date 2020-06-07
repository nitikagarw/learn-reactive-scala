import com.lightbend.cinnamon.sbt._
import sbt._

object Version {
  val akkaVer         = "2.5.19"
  val scalaVer        = "2.12.8"
  val scalaTestVer    = "3.0.1"
}

object Dependencies {
  val dependencies = Seq(
    "com.typesafe.akka"       %% "akka-actor"                 % Version.akkaVer withSources(),
    "com.typesafe.akka"       %% "akka-testkit"               % Version.akkaVer withSources(),
    "com.typesafe.akka"       %% "akka-stream"                % Version.akkaVer withSources(),
    "com.typesafe.akka"       %% "akka-stream-testkit"        % Version.akkaVer withSources(),
    Cinnamon.library.cinnamonAkkaStream,
    Cinnamon.library.cinnamonPrometheus,
    Cinnamon.library.cinnamonPrometheusHttpServer,
    "org.scalatest"           %% "scalatest"                  % Version.scalaTestVer % "test"
  )
}
