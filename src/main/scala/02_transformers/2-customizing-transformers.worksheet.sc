// see:
// https://chimney.readthedocs.io/en/latest/transformers/customizing-transformers.html

// Customizing transformers

import io.scalaland.chimney.dsl._

case class Catterpillar(size: Int, name: String)
case class Butterfly(size: Int, name: String, wingsColor: String)

val stevie = Catterpillar(5, "Steve")
// val steve  = stevie.transformInto[Butterfly]
// error: Chimney can't derive transformation from Catterpillar to Butterfly
//
// Butterfly
//   wingsColor: String - no accessor named wingsColor in source type Catterpillar

// Providing missing values: `withFieldComputed`

val steve =
  stevie
    .into[Butterfly]
    .withFieldConst(_.wingsColor, "white")
    .transform

val steve2 =
  stevie
    .into[Butterfly]
    .withFieldComputed(_.wingsColor, c => if (c.size > 4) "yellow" else "gray")
    .transform

// Fields renaming: `withFieldRenamed`

case class SpyGB(name: String, surname: String)
case class SpyRU(imya: String, familia: String)

val jamesGB = SpyGB("James", "Bond")

val jamesRU =
  jamesGB
    .into[SpyRU]
    .withFieldRenamed(_.name, _.imya)
    .withFieldRenamed(_.surname, _.familia)
    .transform

  // Using method accessors: enableMethodAccessors

  case class Foo(a: Int) {
    def m: String = "m"
  }
case class FooV2(a: Int, m: String)

Foo(1)
  .into[FooV2]
  .enableMethodAccessors
  .transform

import `02_transformers`._ // for Color and Channel, see package.scala

val colRed: Color =
  Color.Red
val chanRed       =
  colRed.transformInto[Channel]

// chanRed.transformInto[Color]
// error: Chimney can't derive transformation from Channel to Color
//
// Color
//   can't transform coproduct instance Channel.Alpha to Color

val red =
  chanRed
    .into[Color]
    .withCoproductInstance { (_: Channel.Alpha.type) => Color.Blue }
    .transform

val alpha: Channel =
  Channel.Alpha
val blue           =
  alpha
    .into[Color]
    .withCoproductInstance { (_: Channel.Alpha.type) => Color.Blue }
    .transform
