// see:
// https://chimney.readthedocs.io/en/latest/lifted-transformers/cats-integration.html

// Cats integration (deprecated)

// build.sbt:
// libraryDependencies += "io.scalaland" %% "chimney-cats" % "0.7.5"

import io.scalaland.chimney.dsl._
import io.scalaland.chimney.{TransformationError, Transformer, TransformerF}
import io.scalaland.chimney.cats._
import cats.data.{Ior, NonEmptyChain, Validated, ValidatedNec}
// import cats.Applicative

// Validated support for lifted transformers

// Through Chimney cats integration module, you obtain support for Validated[EE, +*], as the wrapper type for lifted transformers, where:

// - EE - type of an error channel
// - cats.Semigroup implicit instance is available for chosen EE type

// Usual choice for EE is cats.data.NonEmptyChain[String].

case class RegistrationForm(email: String, username: String, password: String, age: String)

case class RegisteredUser(email: String, username: String, passwordHash: String, age: Int)

def hashpw(pw: String): String =
  pw.reverse

@annotation.nowarn("cat=deprecation")
implicit val transformerValidated
    : TransformerF[Validated[NonEmptyChain[String], +*], RegistrationForm, RegisteredUser] = {
  Transformer
    .defineF[Validated[NonEmptyChain[String], +*], RegistrationForm, RegisteredUser]
    .withFieldComputedF(
      _.email,
      form => {
        if (form.email.contains('@')) {
          Validated.valid(form.email)
        } else {
          Validated.invalid(NonEmptyChain(s"${form.username}'s email: does not contain '@' character"))
        }
      }
    )
    .withFieldComputed(_.passwordHash, form => hashpw(form.password))
    .withFieldComputedF(
      _.age,
      form =>
        form.age.toIntOption match {
          case Some(value) if value >= 18 => Validated.valid(value)
          case Some(value)                => Validated.invalid(NonEmptyChain(s"${form.username}'s age: must have at least 18 years"))
          case None                       => Validated.invalid(NonEmptyChain(s"${form.username}'s age: invalid number"))
        }
    )
    .buildTransformer
}

@annotation.nowarn("cat=deprecation")
val resultBad =
  Array(
    RegistrationForm("john_example.com", "John", "123456", "10"),
    RegistrationForm("alice@example.com", "Alice", "123456", "19"),
    RegistrationForm("bob@example.com", "Bob", "123456", "21.5")
  ).transformIntoF[Validated[NonEmptyChain[String], +*], List[RegisteredUser]]

@annotation.nowarn("cat=deprecation")
val resultOk =
  Array(
    RegistrationForm("john@example.com", "John", "123456", "40"),
    RegistrationForm("alice@example.com", "Alice", "123456", "19"),
    RegistrationForm("bob@example.com", "Bob", "123456", "21")
  ).transformIntoF[Validated[NonEmptyChain[String], +*], List[RegisteredUser]]

// Ior support for lifted transformers

// Like Validated[EE, +*], the Chimney cats integration module also supports Ior[EE, +*] where:

// - EE - type of an error channel
// - cats.Semigroup implicit instance is available for chosen EE type

// The usual choice for EE is cats.data.NonEmptyChain[String] (which has a Semigroup typeclass instance).

@annotation.nowarn("cat=deprecation")
implicit val transformerIor: TransformerF[Ior[NonEmptyChain[String], +*], RegistrationForm, RegisteredUser] =
  Transformer
    .defineF[Ior[NonEmptyChain[String], +*], RegistrationForm, RegisteredUser]
    .withFieldComputedF(
      _.username,
      form =>
        if (form.username.contains("."))
          Ior.both(NonEmptyChain(s"${form.username} . is deprecated"), form.username)
        else
          Ior.right(form.username)
    )
    .withFieldComputedF(
      _.email,
      form => {
        if (form.email.contains('@'))
          Ior.right(form.email)
        else if (form.username.contains("."))
          Ior.both(NonEmptyChain(s"${form.username} contains . which is deprecated"), form.email)
        else
          Ior.left(NonEmptyChain(s"${form.username}'s email: does not contain '@' character"))
      }
    )
    .withFieldComputed(_.passwordHash, form => hashpw(form.password))
    .withFieldComputedF(
      _.age,
      form =>
        form.age.toIntOption match {
          case Some(value) if value >= 18 => Ior.right(value)
          case Some(value) if value >= 10 => Ior.both(NonEmptyChain(s"${form.username}: quite young"), value)
          case Some(_)                    => Ior.left(NonEmptyChain(s"${form.username}'s age: must be at least 18 years of age"))
          case None                       => Ior.left(NonEmptyChain(s"${form.username}'s age: invalid number"))
        }
    )
    .buildTransformer

@annotation.nowarn("cat=deprecation")
val resultIorLeft =
  Array(
    RegistrationForm("john@example.com", "John.Doe", "123456", "10"), // Both
    RegistrationForm("alice@example.com", "Alice", "123456", "19"),   // Right
    RegistrationForm("bob@example.com", "Bob", "123456", "21.5")      // Left
  ).transformIntoF[Ior[NonEmptyChain[String], +*], List[RegisteredUser]]

@annotation.nowarn("cat=deprecation")
val resultIorBoth =
  Array(
    RegistrationForm("john@example.com", "John.Doe", "123456", "40"),
    RegistrationForm("alice@example.com", "Alice", "123456", "17"),
    RegistrationForm("bob@example.com", "Bob", "123456", "21")
  ).transformIntoF[Ior[NonEmptyChain[String], +*], List[RegisteredUser]]

// Error path support for cats-based transformers

// Chimney provides instance of TransformerFErrorPathSupport for F[_]
// if there is ApplicativeError[F, EE[TransformationError[M]]] instance and Applicative[E] instance.

// In particular ValidatedNec[TransformationError[M], +*], ValidatedNel[TransformationError[M], +*],
// IorNec[TransformationError[M], +*], IorNel[TransformationError[M], +*] satisfy this requirement.

// Let’s look to example based on ValidatedNec[TransformationError[M], +*]

import scala.util.Try

@annotation.nowarn("cat=deprecation")
type V[+A] = ValidatedNec[TransformationError[String], A]

@annotation.nowarn("cat=deprecation")
def printError(err: TransformationError[String]): String =
  s"${err.message} on ${err.showErrorPath}"

@annotation.nowarn("cat=deprecation")
implicit val intParse: TransformerF[V, String, Int] =
  str =>
    Validated.fromOption(
      Try(str.toInt).toOption,
      NonEmptyChain.one(TransformationError(s"Can't parse int from '$str'"))
    )

// Raw domain
case class RawClass(id: String, inner: RawInner)
case class RawInner(id: String, description: String)

// Domain
case class Class(id: Int, inner: Inner)
case class Inner(id: Int, description: String)

val raw = RawClass("null", RawInner("undefined", "description"))

@annotation.nowarn("cat=deprecation")
val obtained =
  raw
    .transformIntoF[V, Class]
    .leftMap(_.map(printError))

val expected =
  Validated.Invalid(
    NonEmptyChain(
      "Can't parse int from 'null' on id",
      "Can't parse int from 'undefined' on inner.id"
    )
  )

obtained == expected
