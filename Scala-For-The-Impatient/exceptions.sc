import java.io.FileNotFoundException

import scala.io.Source._
try {
//  var q = 10/0
  for(line <- fromFile("hello.txt"))
    println(line)
} catch {
  case e: ArithmeticException => println("don't divide by zero")
  case e:FileNotFoundException => println("file not found")
  case _: Exception => println("qaz")

}

def greekAlphabet(letter:Char) = {
  letter match {
    case 'a' | 'A' => "alpha"
    case 'b' | 'B' => "beta"
    case _ => println("Invalid")
  }
}