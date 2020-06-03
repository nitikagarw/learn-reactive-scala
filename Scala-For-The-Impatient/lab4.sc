class Time(val hours: Int, val minutes: Int = 0) {
  if (hours < 0 || hours >= 24 || minutes < 0 || minutes >= 60) throw new IllegalArgumentException

  def before(other: Time) = hours < other.hours || hours == other.hours && minutes < other.minutes

  override def toString: String = f"${hours}:${minutes}%02d"

}

val morning = new Time(9, 5)
//val crazy = new Time(-3, 222)
val afternoon = new Time(15, 26)

morning.before(afternoon)
afternoon.before(morning)

val noon = new Time(12)


//Uniform Access
class Time1(h: Int, m: Int = 0) {
  private var minutesSinceMidnight = h * 60 + m

  def hours = minutesSinceMidnight / 60

  def minutes = minutesSinceMidnight % 60

  def minutes_=(newValue: Int): Unit = {
    if (newValue < 0 || newValue >= 60) throw new IllegalArgumentException
    minutesSinceMidnight = h * 60 + newValue
  }

  if (h < 0 || h >= 24 || m < 0 || m >= 60) throw new IllegalArgumentException

  def before(other: Time1) = minutesSinceMidnight < other.minutesSinceMidnight

  override def toString: String = f"${hours}:${minutes}%02d"

}

val n = new Time1(12)
n.hours
n.minutes = 30
n


class Time3(val hours: Int, val minutes: Int) {
  def this(h: Int) {
    this(h, 0)
  }

  if (hours < 0 || hours >= 24 || minutes < 0 || minutes >= 60) throw new IllegalArgumentException

  def -(other: Time3) = hours * 6 - +minutes - other.hours * 60 - other.minutes

  def <(other: Time3) = this - other < 0

  override def toString: String = f"${hours}:${minutes}%02d"

}

object Time3 {
  def apply(h: Int, m: Int) = new Time3(h, m)

}

Time3(9, 0) < Time3(11, 34)