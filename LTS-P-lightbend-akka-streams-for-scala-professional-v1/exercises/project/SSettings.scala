package sbtstudent

object SSettings {
  import scala.Console._
  val consoleColorMap: Map[String, String] =
    Map("RESET" -> RESET, "GREEN" -> GREEN, "RED" -> RED, "BLUE" -> BLUE, "CYAN" -> CYAN, "YELLOW" -> YELLOW, "WHITE" -> WHITE, "BLACK" -> BLACK, "MAGENTA" -> MAGENTA)

  val testFolders = List("src/test", "common/src/test")

  val studentifiedBaseFolder = "exercises"

  val promptManColor         = "GREEN"
  val promptExerciseColor    = "GREEN"
  val promptCourseNameColor = "RESET"
}
       