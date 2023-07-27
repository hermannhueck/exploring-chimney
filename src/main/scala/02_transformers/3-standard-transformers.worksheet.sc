// see:
// https://chimney.readthedocs.io/en/latest/transformers/standard-transformers.html

// Standard transformers

import io.scalaland.chimney.dsl._

case class Catterpillar(size: Int, name: String)
case class Butterfly(size: Int, name: String)

// Identity transformation

// Given any type T, Chimney is able to derive identity transformer: Transformer[T, T].

1234.transformInto[Int]
true.transformInto[Boolean]
3.14159.transformInto[Double]
"test".transformInto[String]
Butterfly(3, "Steve").transformInto[Butterfly]

// Supertype transformation

// Given any types T and U such that T <: U (T is subtype of U), Chimney is able to derive supertype transformer: Transformer[T, U].

class Vehicle(val maxSpeed: Double)
class Car(maxSpeed: Double, val seats: Int) extends Vehicle(maxSpeed)

val vehicle =
  (new Car(180, 5)).transformInto[Vehicle]
vehicle.maxSpeed

// Value classes

// automatic value class field extraction and wrapping.

import `02_transformers`._ // see package.scala

val richPerson  = rich.Person(rich.PersonId(10), rich.PersonName("Bill"), 30)
val plainPerson = richPerson.transformInto[plain.Person]
val richPerson2 = plainPerson.transformInto[rich.Person]

// Options

// Given any types T, U such that there exists Chimney transformer between them (Transformer[T, U]),
// Chimney is able to derive Transformer[Option[T], Option[U]].

Some(1234)
  .transformInto[Option[Int]]
Option
  .empty[Int]
  .transformInto[Option[Int]]

Some("test")
  .transformInto[Option[String]]
Option
  .empty[String]
  .transformInto[Option[String]]

Some(new Car(180, 5))
  .transformInto[Option[Vehicle]]
Option
  .empty[Car]
  .transformInto[Option[Vehicle]]

Some(rich.Person(rich.PersonId(10), rich.PersonName("Bill"), 30))
  .transformInto[Option[plain.Person]]
Option
  .empty[rich.Person]
  .transformInto[Option[plain.Person]]

// Collections

// Given any collection types C1[_] and C2[_], and types T, U
// such that there exists Chimney transformer between them (Transformer[T, U]),
// Chimney is able to derive Transformer[C1[T], C2[U]].

List(123, 456)
  .transformInto[Array[Int]]

Seq("foo", "bar")
  .transformInto[Vector[String]]

Vector(new Car(160, 4), new Car(220, 5))
  .transformInto[List[Vehicle]]

// Maps

// Given any collection types K1, K2, V1, V2 such that there exist transformers Transformer[K1, K2] and Transformer[V1, V2], Chimney is able to derive Transformer[Map[K1, V1], Map[K2, V2]].

Map(1 -> "Alice", 2 -> "Bob")
  .transformInto[Map[Int, rich.PersonName]]

val mapIV =
  Map(rich.PersonId(10) -> new Car(200, 5), rich.PersonId(22) -> new Car(170, 4))
    .transformInto[Map[Int, Vehicle]]
// mapIV
//   .into[Map[rich.PersonId, Car]]
//   .withFieldConst(_.key.value, 5)

// Either

// Given any collection types L1, L2, R1, R2
// such that there exist transformers Transformer[L1, L2] and Transformer[R1, R2],
// Chimney is able to derive Transformer[Either[L1, R1], Either[L2, R2]].

(Right("Batman"): Either[Int, String])
  .transformInto[Either[rich.PersonId, rich.PersonName]]
Right("Batman")
  .withLeft[Int]
  .transformInto[Either[rich.PersonId, rich.PersonName]]

(Left(10): Either[Int, String])
  .transformInto[Either[rich.PersonId, rich.PersonName]]
Left(10)
  .withRight[String]
  .transformInto[Either[rich.PersonId, rich.PersonName]]

(Right(Array(10, 20)): Either[String, Array[Int]])
  .transformInto[Either[String, List[Int]]]
Right(Array(10, 20))
  .withLeft[String]
  .transformInto[Either[String, List[Int]]]

(Left("test"): Either[String, Array[Int]])
  .transformInto[Either[String, List[Int]]]
Left("test")
  .withRight[Array[Int]]
  .transformInto[Either[String, List[Int]]]
