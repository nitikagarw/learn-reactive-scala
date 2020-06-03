//case class: class that is optimized for use in pattern matching

abstract class Amount

case class Dollar(value: Double) extends Amount

case class Currency(value: Double, unit: String) extends Amount

case object Nothing extends Amount

val amt = Currency(100, "EUR")
//amt match {
//  case Dollar(v) => "$" + v
//  case Currency(_, u) => "Oh no! I got you " + u
//  case Nothing => ""
//}

val scores = Map("Alice" -> 1, "Bob" -> 10)
scores.get("Alice") match {
  case Some(score) => println(score)
  case None => println("No score")
}

abstract class Expr

case class Num(value: Int) extends Expr

case class Sum(left: Expr, right: Expr) extends Expr

case class Product(left: Expr, right: Expr) extends Expr

val e = Product(Num(3), Sum(Num(4), Num(5)))

def eval(e: Expr): Int = e match {
  case Num(v) => v
  case Sum(l, r) => eval(l) + eval(r)
  case Product(l, r) => eval(l) * eval(r)
}

eval(e)


/*==== LAB EXERCISE 1 PATTERN MATCHING =======*/
def swap(p: (Int, Int)) = p match {
  case (x, y) => (y, x)
}

swap((3, 4))

def swap2(a: Array[Int]) = a match {
  case Array(x, y, rest@_*) => Array(y, x) ++ rest
  case _ => a
}

swap2(Array(2, 4, 5, 8))
swap2(Array(1))


/*==== LAB EXERCISE 2 ARTICLES AND BUNDLES =======*/
abstract class Item

case class Article(description: String, price: Double) extends Item

case class Bundle(description: String, discount: Double, items: Item*) extends Item

val book = Article("Scala for the Impatient", 39.95)
val gift = Bundle("xmas special", 10, book, Article("Old Potrero Staright Rye Whiskey", 79.95))

def price(it: Item): Double = it match {
  case Article(_, p) => p
  case Bundle(_, d, its@_*) => its.map(price).sum - d
}

price(book)
price(gift)

val special = Bundle("Fathers Day Special", 20.0,
  Article("Scala for the Impatient", 39.95),
  Bundle("Anchor Sampler", 10.0,
    Article("Old Potrero Staright Rye Whiskey", 79.95),
    Article("Junipero Gin", 32.95)))

val Bundle(_, _, Article(descr, pr), _*) = special


/*==== LAB EXERCISE 1 OPTION TYPE =======*/
abstract class DoubleOption
case class SomeDouble(value: Double) extends DoubleOption
case object NoDouble extends DoubleOption

def inv(x: Double) = if(x == 0) NoDouble else SomeDouble(1 / x)

inv(2)
inv(0)

import scala.math._
def f(x: Double) = if(x <= 1) SomeDouble(sqrt(1-x)) else NoDouble

def compose(f: Double => DoubleOption, g: Double => DoubleOption) = (x: Double) => g(x) match {
  case SomeDouble(res) => f(res)
  case NoDouble => NoDouble
}

val h = compose(f, inv)
h(0)
h(1)
h(2)
h(0.5)

def isEmpty(opt: DoubleOption) = opt match {
  case NoDouble => true
  case _ => false
}

def get(opt: DoubleOption) = opt match {
  case NoDouble => throw new NoSuchElementException
  case SomeDouble(value) => value
}

/*=======================*/
abstract class DoubleOption1 {
  def get: Double
  def isEmpty: Boolean
}

class SomeDouble1(val value: Double) extends DoubleOption1 {
  def get = value
  def isEmpty = false
}

object NoDouble1 extends DoubleOption1 {
  def get = throw new NoSuchElementException
  def isEmpty = true
}