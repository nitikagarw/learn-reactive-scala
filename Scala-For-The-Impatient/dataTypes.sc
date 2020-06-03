import scala.collection.SortedSet

var nums = Set(1,2,2,2,3,3,3,3,4,5.9)
var nums1 = SortedSet(1,2,2,2,3,3,3,3,4,5)



val v =(1, 56, "hi")
v._2
val (first, second, third) = v
val symbols = Array("<", "-", ">")
val counts = Array(2,10,2)
val pairs = symbols.zip(counts)

for((s,n) <- pairs){
  print(s*n)
}
println



def divideBy10(n:Int):Tuple2[Int, Int] = (n/10, n%10)
val (tens, ones) = divideBy10(99)



var a = Array(1,2,3,4,5 )
var result = for(n <- a) yield 2*n
var even = for(n <- a if n%2 == 0) yield n

val l1 = List(3.0, 5, 'a')
val l2 = 1::2::3::4::5::Nil
val l3 = List.range(10,20)
val l4 = l1:::l2

var sum = 0
l3.foreach(sum += _)
println(sum)

def average(a:Array[Int]):Double = {
  var sum = 0
  a.foreach(sum += _)

  sum/a.length
}

average(Array(10,20,30))