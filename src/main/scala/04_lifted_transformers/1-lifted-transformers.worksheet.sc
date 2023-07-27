// see:
// https://chimney.readthedocs.io/en/latest/lifted-transformers/lifted-transformers.html

// Lifted transformers (deprecated)

import io.scalaland.chimney.dsl._
import io.scalaland.chimney.{Transformer, TransformerF}

case class RegistrationForm(email: String, username: String, password: String, age: String)

case class RegisteredUser(email: String, username: String, passwordHash: String, age: Int)

def hashpw(pw: String): String =
  pw.reverse

// We get field age: String as an input, but we would like to parse it into correct Int or signal an error,
// if provided value is not valid integer. This is simply not possible with total Transformer.
// This is a moment when lifted transformers, provided by TransformerF type class come into play.

val okForm = RegistrationForm("john@example.com", "John", "123456", "40")

@annotation.nowarn("cat=deprecation")
val resultOk =
  okForm
    .intoF[Option, RegisteredUser]                // (1)
    .withFieldComputed(_.passwordHash, form => hashpw(form.password))
    .withFieldComputedF(_.age, _.age.toIntOption) // (2)
    .transform                                    // (3)

// There are few differences to total transformers in the example above:
// - Instead of into[RegisteredUser], we use intoF[Option, RegisteredUser], which tells Chimney that Option type will be used for handling partial transformations.
// - Instead of withFieldComputed, we use withFieldComputedF, where second parameter is a function that wraps result into a type constructor provided in (1) - Option in this case.
// - Result type of transform call is not RegisteredUser, but Option[RegisteredUser].

val badForm = RegistrationForm("john@example.com", "John", "s3cr3t", "not an int")

@annotation.nowarn("cat=deprecation")
val resultBad =
  badForm
    .intoF[Option, RegisteredUser]                // (1)
    .withFieldComputed(_.passwordHash, form => hashpw(form.password))
    .withFieldComputedF(_.age, _.age.toIntOption) // (2)
    .transform                                    // (3)

// Similar to withFieldConst, withFieldComputed, withCoproductInstance operations in DSL, there are lifted counterparts available:
// - withFieldConstF
// - withFieldComputedF
// - withCoproductInstanceF

@annotation.nowarn("cat=deprecation")
def method() = {

  @annotation.nowarn("msg=never used")
  implicit val transformer: TransformerF[Option, RegistrationForm, RegisteredUser] =
    TransformerF
      .define[Option, RegistrationForm, RegisteredUser]
      .withFieldComputed(_.passwordHash, form => hashpw(form.password))
      .withFieldComputedF(_.age, _.age.toIntOption)
      .buildTransformer

  val result1 =
    Array(okForm, badForm)
      .transformIntoF[Option, List[RegisteredUser]]

  result1
}

val result1 = method()

result1

// Chimney supports out of the box Either[C[E], +*], as the wrapper type, where
// - E - type of a single error occurrence
// - C[_] - collection type to store all the transformation errors (like Seq, Vector, List, etc.)

// If we pick error type as String (as validation error message) and collection as Vector,
// we obtain wrapper type Either[Vector[String], +*].

// Let’s enhance our RegistrationForm to RegisteredUser lifted transformer with few additional validation rules:
// - email field should contain @ character
// - age must be at least 18 years

@annotation.nowarn("cat=deprecation")
implicit val transformer: TransformerF[Either[Vector[String], +*], RegistrationForm, RegisteredUser] = {
  Transformer
    .defineF[Either[Vector[String], +*], RegistrationForm, RegisteredUser]
    .withFieldComputedF(
      _.email,
      form => {
        if (form.email.contains('@')) {
          Right(form.email)
        } else {
          Left(Vector(s"${form.username}'s email: does not contain '@' character"))
        }
      }
    )
    .withFieldComputed(_.passwordHash, form => hashpw(form.password))
    .withFieldComputedF(
      _.age,
      form =>
        form.age.toIntOption match {
          case Some(value) if value >= 18 => Right(value)
          case Some(value)                => Left(Vector(s"${form.username}'s age: must have at least 18 years"))
          case None                       => Left(Vector(s"${form.username}'s age: invalid number"))
        }
    )
    .buildTransformer
}

@annotation.nowarn("cat=deprecation")
val resultBad2 = Array(
  RegistrationForm("john_example.com", "John", "123456", "10"),
  RegistrationForm("alice@example.com", "Alice", "123456", "19"),
  RegistrationForm("bob@example.com", "Bob", "123456", "21.5")
).transformIntoF[Either[Vector[String], +*], List[RegisteredUser]]

@annotation.nowarn("cat=deprecation")
val resultOk2 = Array(
  RegistrationForm("john@example.com", "John", "123456", "40"),
  RegistrationForm("alice@example.com", "Alice", "123456", "19"),
  RegistrationForm("bob@example.com", "Bob", "123456", "21")
).transformIntoF[Either[Vector[String], +*], List[RegisteredUser]]

// TransformerF type class

// Similar to the Transformer type class, Chimney defines a TransformerF type class,
// which allows to express partial (lifted, wrapped) transformation of type From => F[To].

// trait TransformerF[F[+_], From, To] {
//   def transform(src: From): F[To]
// }

// Supporting custom F[_]

// Chimney provides pluggable interface that allows you to use your own F[_] type constructor in lifted transformations.

// The library defines TransformerFSupport type class, as follows.

// trait TransformerFSupport[F[+_]] {
//   def pure[A](value: A): F[A]
//   def product[A, B](fa: F[A], fb: => F[B]): F[(A, B)]
//   def map[A, B](fa: F[A], f: A => B): F[B]
//   def traverse[M, A, B](it: Iterator[A], f: A => F[B])(implicit fac: Factory[B, M]): F[M]
// }

// Error path support

// Warning:
// Support for enhanced error paths is currently an experimental feature and we don’t guarantee
// it will be included in the next library versions in the same shape.

// Chimney provides ability to trace errors in lifted transformers.
// For using it you need to implement an instance of TransformerFErrorPathSupport

// trait TransformerFErrorPathSupport[F[+_]] {
//   def addPath[A](fa: F[A], node: ErrorPathNode): F[A]
// }

// There are 4 different types of of ErrorPathNode:
// - Accessor for case class field or java bean getter
// - Index for collection index
// - MapKey for map key
// - MapValue for map value

// There are 4 different types of of ErrorPathNode:
// - Accessor for case class field or java bean getter
// - Index for collection index
// - MapKey for map key
// - MapValue for map value

import io.scalaland.chimney.TransformationError
import scala.util.Try

@annotation.nowarn("cat=deprecation")
type V[+A] = Either[List[TransformationError[String]], A]

@annotation.nowarn("cat=deprecation")
implicit val intParse: TransformerF[V, String, Int] =
  str =>
    Try(str.toInt)
      .toEither
      .left
      .map(_ => List(TransformationError(s"Can't parse int from '$str'")))

// Raw domain
case class RawData(id: String, links: List[RawLink])
case class RawLink(id: String, mapping: Map[RawLinkKey, RawLinkValue])
case class RawLinkKey(id: String)
case class RawLinkValue(value: String)

// Domain
case class Data(id: Int, links: List[Link])
case class Link(id: Int, mapping: Map[LinkKey, LinkValue])
case class LinkKey(id: Int)
case class LinkValue(value: Int)

val rawData = RawData(
  "undefined",
  List(RawLink("null", Map(RawLinkKey("error") -> RawLinkValue("invalid"))))
)

@annotation.nowarn("cat=deprecation")
val obtained =
  rawData
    .transformIntoF[V, Data]

  import io.scalaland.chimney.ErrorPathNode._

@annotation.nowarn("cat=deprecation")
val expected = Left(
  List(
    TransformationError(
      "Can't parse int from 'undefined'",
      List(Accessor("id"))
    ),
    TransformationError(
      "Can't parse int from 'null'",
      List(Accessor("links"), Index(0), Accessor("id"))
    ),
    TransformationError(
      "Can't parse int from 'error'",
      List(
        Accessor("links"),
        Index(0),
        Accessor("mapping"),
        MapKey(RawLinkKey("error")),
        Accessor("id")
      )
    ),
    TransformationError(
      "Can't parse int from 'invalid'",
      List(
        Accessor("links"),
        Index(0),
        Accessor("mapping"),
        MapValue(RawLinkKey("error")),
        Accessor("value")
      )
    )
  )
)

obtained == expected

// Using build in showErrorPath
@annotation.nowarn("cat=deprecation")
def printError(err: TransformationError[String]): String =
  s"${err.message} on ${err.showErrorPath}"

@annotation.nowarn("cat=deprecation")
val obtainedErrors = rawData.transformIntoF[V, Data].left.toOption.map(_.map(printError))

val expectedErrors =
  Some(
    List(
      "Can't parse int from 'undefined' on id",
      "Can't parse int from 'null' on links(0).id",
      "Can't parse int from 'error' on links(0).mapping.keys(RawLinkKey(error)).id",
      "Can't parse int from 'invalid' on links(0).mapping(RawLinkKey(error)).value"
    )
  )

obtainedErrors == expectedErrors

// Emitted code

// see documenation for more details

// Deriving lifted transformers

// see documenation for more details
