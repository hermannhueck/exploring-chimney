// see:
// https://chimney.readthedocs.io/en/latest/transformers/customizing-transformers.html

// Customizing transformers

package `02_transformers`

import scala.util.chaining._
import util._

import io.scalaland.chimney.dsl._

object App2CustomizingTransformers extends App {

  line80.green pipe println

  case class Catterpillar(size: Int, name: String)
  case class Butterfly(size: Int, name: String, wingsColor: String)

  val stevie = Catterpillar(5, "Steve") tap println
  // val steve  = stevie.transformInto[Butterfly]
  // error: Chimney can't derive transformation from Catterpillar to Butterfly
  //
  // Butterfly
  //   wingsColor: String - no accessor named wingsColor in source type Catterpillar

  // Providing missing values: `withFieldComputed`

  val steve =
    stevie
      .into[Butterfly]
      .withFieldComputed(_.wingsColor, c => if (c.size > 4) "yellow" else "gray")
      .transform tap println

  // Fields renaming: `withFieldRenamed`

  println(" ")

  case class SpyGB(name: String, surname: String)
  case class SpyRU(imya: String, familia: String)

  val jamesGB =
    SpyGB("James", "Bond")
      .tap(println)

  val jamesRU =
    jamesGB
      .into[SpyRU]
      .withFieldRenamed(_.name, _.imya)
      .withFieldRenamed(_.surname, _.familia)
      .transform
      .tap(println)

  // Using method accessors: enableMethodAccessors

  println(" ")

  case class Foo(a: Int) { def m: String = "m" }
  case class FooV2(a: Int, m: String)

  Foo(1)
    .into[FooV2]
    .enableMethodAccessors
    .transform
    .tap(println)

  // Transforming coproducts: withCoproductInstance

  println(" ")

  sealed trait Color extends Product with Serializable
  object Color {
    case object Red   extends Color
    case object Green extends Color
    case object Blue  extends Color
  }

  sealed trait Channel extends Product with Serializable
  object Channel {
    case object Alpha extends Channel
    case object Blue  extends Channel
    case object Green extends Channel
    case object Red   extends Channel
  }

  val colRed: Color =
    Color
      .Red
      .tap(println)
  val chanRed       =
    colRed
      .transformInto[Channel]
      .tap(println)

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
      .tap(println)

  val alpha: Channel =
    Channel
      .Alpha
      .tap(println)
  val blue           =
    alpha
      .into[Color]
      .withCoproductInstance { (_: Channel.Alpha.type) => Color.Blue }
      .transform
      .tap(println)

  line80.green pipe println
}
