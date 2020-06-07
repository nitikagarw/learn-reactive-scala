package com.lightbend.akkassembly

object Color {
  def apply(hexString: String): Color = {
    new Color(
      Integer.parseInt(hexString.substring(0, 2), 16),
      Integer.parseInt(hexString.substring(2, 4), 16),
      Integer.parseInt(hexString.substring(4, 6), 16)
    )
  }
}
case class Color(red: Int, green: Int, blue: Int) {
  require(red >= 0 && red <= 255)
  require(green >= 0 && green <= 255)
  require(blue >= 0 && blue <= 255)
}
