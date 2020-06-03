val x = -4
val result = if(x>0) 1 else -1
val res = if(x>0) "positive" else -1
val res2 = if(x>0) "positive"


val n = 10
for(i <- 1 to n) println(i)

for( c <- "Hello") println(c)

for( i <-  1 to 3; j <- 1 to 3)
  print((10*i + j) + " ")
println()


for( i <-  1 to 3; j <- 1 to 3 if i!=j)
  print((10*i + j) + " ")
println()

val res3 = for(i <- 1 to 10) yield i%3

//ALways specify return value of recursive functions
def fac(n:Int):Int = if(n<=0) 1 else n * fac(n-1)

fac(5)


//Function with no return
def box(s:String): Unit = {
  val border = "-" * s.length + "--\n"
  println(border + "|" + s + "|\n" + border)
}

box("Hello")

//Default arguments
def decorate(s:String, left:String="[", right:String="]") = left + s +  right

decorate("Hello")
decorate("Hello", ">>>[")
decorate("Hello", right = "]<<<<")

//varargs


def sum(args: Int*) = {
  var res = 0
  for(arg <- args) res += arg
  res
}

sum(1, 4, 9, 16, 25)

sum(1 to 10 : _*)

def recursivesSum(args: Int*): Int = {
  if(args.length == 0) 0
  else args.head + recursivesSum(args.tail: _*)
}

def isVowel(c: Char) = c=='a' || c=='e' || c=='i' || c=='0' || c=='u'

isVowel('a')
isVowel('z')


def isVowel1(c: Char) = "aeiou".contains(c)


isVowel1('a')
isVowel1('r')


def vowels(s: String) = for(c <- s if "aeio".contains(c)) yield c

vowels("qwertyasdfgzxioio")


def vowelsRecursive(s: String): String = {
  if(s.length == 0) ""
  else {
    val c = s(0)
    val rest = vowelsRecursive(s.substring(1))
    if(isVowel1(c)) c + rest else rest
  }
}

vowelsRecursive("qwertyasdfgzxioio")


def isVowel2(c: Char, vowelChars: String) = vowelChars.contains(c)

def vowels1(s: String, vowelChars: String = "aeiou", ignoreCase: Boolean = true): String = {
  if(ignoreCase) vowels1(s.toLowerCase, vowelChars, ignoreCase = false)
  else for(c <- s if isVowel2(c, vowelChars)) yield c
}

vowels1(("qwertyasdfgzxioio"))