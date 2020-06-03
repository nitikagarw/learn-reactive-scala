import scala.collection.mutable.ArrayBuffer

val nums = new Array[Int](10)

for (i <- 0 until nums.length) nums(i) = i * i
nums

for (elem <- nums) println(elem)

val b = new ArrayBuffer[Int]
b += 1
b += (2, 3, 4, 5)
b ++= Array(8, 13, 24)
b.insert(2, 6)
b
b.remove(1)
b

b.toArray

(1 to 5).toBuffer
(1 until 10 by 2).toBuffer

val a = ArrayBuffer.range(1, 10)
a.transform(_ * 2)

Array(1, 2, 5, 6, 7).sum
Array(1, 2, 5, 6, 7).max
Array(1, 2, 5, 6, 7).min


val arr = Array(10, 43, 1, 2, 0, 5, 6, 7)
arr.sorted
arr
val sortedArr = arr.sorted
sortedArr.reverse
sortedArr
arr

//largest in dictionary order
ArrayBuffer("Mary", "had", "a", "little", "lamb").max

Array(1, 2, 3).toString
Array(1, 2, 3).mkString("|")



// Maps(immutable by default)

val scores = Map("Alice" -> 10, "Bob" -> 3, "Cindy" -> 8)
val mscores = scala.collection.mutable.Map("Alice" -> 10)

val bobScore = scores("Bob")
mscores("Bob") = 20
mscores

//scores("Fred") //NoSuchElementException
scores.getOrElse("Fred", 0)

scores + ("Cindy" -> 5, "Fred" -> 6)
scores

mscores += ("Cindy" -> 8, "Fred" -> 6)
mscores -= "Alice"

for ((k, v) <- mscores)
  println(k + " has score of " + v)


def removeAllButFirstNegative(buf: ArrayBuffer[Int]) {
  val indices = for (i <- 0 until buf.length if buf(i) < 0) yield i
  val indicesToRemove = indices.drop(1)
  for (i <- indicesToRemove.reverse) buf.remove(i)
}

def removeAllButFirstNegative1(buf: ArrayBuffer[Int]) {
  val indicesToRemove = (for (i <- 0 until buf.length if buf(i) < 0) yield i).drop(1)
  for (i <- indicesToRemove.reverse) buf.remove(i)
}

val buf = ArrayBuffer(1, -4, 7, -3, -65, -23, 9, 12, 564, 67, -234)
removeAllButFirstNegative1(buf)
buf

//Doesn't mutate the original ArrayBuffer
def removeAllButFirstNegative2(buf: ArrayBuffer[Int]) = {
  val indicesToRemove = (for (i <- 0 until buf.length if buf(i) < 0) yield i).drop(1)
  for (i <- 0 until buf.length if !indicesToRemove.contains(i)) yield b(i)
}

removeAllButFirstNegative2(buf)



"New York".partition(_.isUpper)

val arr1 = ArrayBuffer(1, -4, 7, -3, -65, -23, 9, 12, 564, 67, -234)
val (neg, pos) = arr1.partition(_<0)



