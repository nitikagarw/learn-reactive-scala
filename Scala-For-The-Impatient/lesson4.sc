//immutable class
class Point(val x: Double, val y: Double) {
  println(f"Welcome to (${x}, ${y})") //extra work by primary constructor
  def this() {this(0, 0)} //auxiliary constructor
  def move(dx: Double, dy:Double) = new Point(x+dx, y+dy)
  def distanceFromOrigin= math.sqrt(x*x + y*y)

  override def toString: String = f"(${x}, ${y})"
}

val p = new Point(3, 4)
p.move(10, 20)

p.distanceFromOrigin //method without a paramater

p.x
p.y

val p1 = new Point()

//eliminate auxiliary constructor by using default args
class Point1(val x: Double = 0, val y: Double = 0) {
  def move(dx: Double, dy:Double) = new Point(x+dx, y+dy)
  def distanceFromOrigin= math.sqrt(x*x + y*y)

  override def toString: String = f"(${x}, ${y})"
}

val p2 = new Point1()
val p3 = new Point(4, 6)


1 to 10 map(3 * _)
1 to 10 map (3 * _) filter (_ % 5 == 2)
1.to(10).map(3 * _).filter(_ % 5 == 2)


//Scala also has private class variables
object Accounts {
  private var lastNumber = 0
  def newUniqueNumber() = { // Use () since it mutates state
    lastNumber += 1
    lastNumber
  }
}

Accounts
Accounts.newUniqueNumber()
Accounts.newUniqueNumber()