import java.util._

import scala.math._

val num = 3.14
val fun = ceil _

fun(num)

Array(2.3, 5.5, 6.2, 7.8, 1.9).map(fun)

Array(2.1, 5.5, 6.2, 7.8, 1.4).map((x: Double) => 3 * x)

def mulBy(factor: Double) = (x: Double) => factor * x
mulBy(3)

val quintuple = mulBy(5)
quintuple(20)

(1 to 9).map(0.1 * _)
(1 to 9).filter(_ % 2 == 0)
(1 to 9).reduceLeft(_ * _)
(1 to 9).filter(_ % 2 == 0).map(0.1 * _)
for (i <- 1 to 9 if i % 2 == 0) yield 0.1 * i

//Currying
val a = Array("Hello", "World")
val b = Array("hello", "world")
a.corresponds(b)(_.equalsIgnoreCase(_))



def runInThread(block: => Unit) {
  new Thread {
    override def run() {
      block
    }
  }.start()
}

runInThread {
  println("Hello!");
  Thread.sleep(10);
  println("Bye!")
}



/*==== LAB EXERCISE 1 LIFE WITHOUT LOOPS =======*/
println()

val zones = TimeZone.getAvailableIDs
zones.map(s => s.split("/")).filter(_.length > 1).map(m => m(1)).grouped(10).toArray.map(a => a(0))


/*==== LAB EXERCISE 2 REDUCTIONS =======*/

(1 to 9).reduceLeft(_ * _)

def fac(n: Int) = 1.to(n).reduceLeft(_ * _)
fac(5)

1.to(10).map(n => 2).reduceLeft(_ * _)

def pow(a: Int, b: Int) = 1.to(b).map(n => a).reduceLeft(_ * _)
pow(2, 12)


def concat(strings: Seq[String], sep: String) = strings.reduceLeft(_ + sep + _)

concat(Array("Mary", "had", "a", "little", "lamb"), ", ")

/*==== LAB EXERCISE 3 DO IT YOURSELF WHILE =======*/

def While(cond: () => Boolean, body: () => Unit) {
  if (cond()) {
    body();
    While(cond, body)
  }
}

val n = 10
var i = 1
var f = 1

While(() => i < n, () => {
  f *= i;
  i += 1
})
f




def While1(cond: => Boolean, body: => Unit) {
  if (cond) {
    body;
    While1(cond, body)
  }
}

val n1 = 10
var i1 = 1
var f1 = 1

While1(i1 < n1, {
  f1 *= i1;
  i1 += 1
})
f1


def While12(cond: => Boolean)(body: => Unit) {
  if (cond) {
    body;
    While12(cond)(body)
  }
}

val n2 = 10
var i2 = 1
var f2 = 1

While12(i2 < n2) {
  f2 *= i2;
  i2 += 1
}
f2



