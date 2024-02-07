import mill._
import $ivy.`com.lihaoyi::mill-contrib-playlib:`,  mill.playlib._

object urlshortenerplayscala extends PlayModule with SingleModule {

  def scalaVersion = "2.13.12"
  def playVersion = "2.9.1"
  def twirlVersion = "1.6.2"

  object test extends PlayTests
}
