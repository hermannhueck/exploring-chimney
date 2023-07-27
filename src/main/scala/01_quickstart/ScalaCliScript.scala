// see:
// https://chimney.readthedocs.io/en/latest/getting-started/quickstart.html

// Quickstart

// Using Scala CLI/Ammonite

//> using scala "2.13.11"
//> using lib "io.scalaland::chimney:0.7.5"

import io.scalaland.chimney.dsl._

case class Foo(x: String, y: Int, z: Boolean = true)
case class Bar(x: String, y: Int)

object ScalaCliScript extends App {
  println(Foo("abc", 10).transformInto[Bar])
  println(Bar("abc", 10).into[Foo].enableDefaultValues.transform)
}
