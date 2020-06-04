import com.lightbend.cinnamon.sbt.Cinnamon.CinnamonKeys.{cinnamon, cinnamonLogLevel}
import com.typesafe.sbteclipse.core.EclipsePlugin.{EclipseCreateSrc, EclipseKeys}
import sbt.Keys._
import sbt._
import sbtstudent.AdditionalSettings

object CommonSettings {
  lazy val commonSettings = Seq(
    organization := "com.lightbend.training",
    version := "1.3.0",
    scalaVersion := Version.scalaVer,
    scalacOptions ++= CompileOptions.compileOptions,
    unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value),
    unmanagedSourceDirectories in Test := List((scalaSource in Test).value),
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.ManagedClasses,
    EclipseKeys.eclipseOutput := Some(".target"),
    EclipseKeys.withSource := true,
    EclipseKeys.skipParents in ThisBuild := true,
    EclipseKeys.skipProject := true,
    parallelExecution in GlobalScope := false,
    cinnamon in run := true,
    cinnamonLogLevel := "INFO",
    // Required for Cinnamon due to our usage of StdIn.readLine().
    // See: https://www.scala-sbt.org/1.x/docs/Forking.html#Configuring+Input
    connectInput in run := true,
    logBuffered in Test := false,
    parallelExecution in ThisBuild := false,
    fork in Test := true,
    libraryDependencies ++= Dependencies.dependencies
  ) ++
    AdditionalSettings.initialCmdsConsole ++
    AdditionalSettings.initialCmdsTestConsole ++
    AdditionalSettings.cmdAliases
}
