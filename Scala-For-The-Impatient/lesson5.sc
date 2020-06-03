
trait Logged {
  def log(msg: String) {}
}

trait ConsoleLogger extends Logged {
  override def log(msg: String) {
    println(msg)
  }
}

trait TimestampLogger extends Logged {
  override def log(msg: String): Unit = {
    super.log(new java.util.Date() + " " + msg)
  }
}

trait ShortLogger extends Logged {
  val maxLength: Int = 15

  override def log(msg: String): Unit = {
    super.log(
      if (msg.length <= maxLength) msg
      else msg.substring(0, maxLength - 3) + "..."
    )
  }
}

class SavingsAccount extends Logged {
  private var balance: Double = 0

  def withdraw(amount: Double): Unit = {
    if (amount > balance) log("Insufficient funds")
    else balance -= amount
  }

}

val acct = new SavingsAccount with ConsoleLogger
acct.withdraw(100)

val acct1 = new SavingsAccount with ConsoleLogger with TimestampLogger with ShortLogger {
  override val maxLength = 10
}
acct1.withdraw(10)

/*==== LAB EXERCISE 1 =======*/

trait RectangleLike {
  def setFrame(x: Double, y:Double, w:Double, h:Double): Unit
  def getX: Double
  def getY: Double
  def getWidth: Double
  def getHeight: Double
  def translate(dx: Double, dy:Double): Unit = {
    setFrame(getX + dx, getY + dy, getWidth, getHeight)
  }

  override def toString = f"(${getX}, ${getY})"
}


import java.awt.geom._
import java.io._


val egg = new Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
egg.translate(10, 20)
egg


/*==== LAB EXERCISE 2 REVERSING THE MIXIN ORDER =======*/
val acct2 = new SavingsAccount with ConsoleLogger with ShortLogger with TimestampLogger {
  override val maxLength = 40
}
acct2.withdraw(10)

val acct3 = new SavingsAccount with ShortLogger with TimestampLogger with ConsoleLogger {
  override val maxLength = 10
}
acct3.withdraw(10)

/*==== LAB EXERCISE 3 BUFFERING =======*/

trait Buffered extends InputStream {
  val SIZE = 1024
  private var end = 0
  private val buffer = new Array[Byte](SIZE)
  private var pos = 0

  override def read(): Int = {
    if(pos == end) {
      end = super.read(buffer, 0, SIZE)
      pos = 0
    }
    if(pos == end) -1
    else {
      pos += 1
      buffer(pos-1)
    }
  }
}

val myStream = new FileInputStream("") with Buffered
myStream.read()
myStream.read()









