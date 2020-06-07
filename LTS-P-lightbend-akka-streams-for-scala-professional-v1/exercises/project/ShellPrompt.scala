package sbtstudent

/**
  * Copyright © 2014 - 2017 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
  */

import sbt.Keys._
import sbt._

import scala.Console

object ShellPrompt extends AutoPlugin {
  override val requires = sbt.plugins.JvmPlugin
  override val trigger = allRequirements

  override lazy val projectSettings =
    Seq(
      shellPrompt := { state =>
        val promptCourseNameColor = SSettings.consoleColorMap(SSettings.promptCourseNameColor)
        val promptExerciseColor = SSettings.consoleColorMap(SSettings.promptExerciseColor)
        val promptManColor = SSettings.consoleColorMap(SSettings.promptManColor)
        val base: File = Project.extract(state).get(sourceDirectory)
        val basePath: String = base + "/test/resources/README.md"
        val exercise = promptExerciseColor + IO.readLines(new sbt.File(basePath)).head + Console.RESET
        val manRmnd = promptManColor + "man [e]" + Console.RESET
        val prjNbrNme = promptCourseNameColor + IO.readLines(new sbt.File(new sbt.File(Project.extract(state).structure.root), ".courseName")).head + Console.RESET
        s"$manRmnd > $prjNbrNme > $exercise > "
      }
    )
}
