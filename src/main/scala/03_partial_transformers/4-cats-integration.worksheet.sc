// see:
// https://chimney.readthedocs.io/en/latest/partial-transformers/cats-integration.html

// Cats integration

// build.sbt:
// libraryDependencies += "io.scalaland" %% "chimney-cats" % "0.7.5"

// Contents

// Cats integration module contains the following stuff:

// - type classes instances for partial transformers data structures
//   - Applicative instance for partial.Result
//   - Semigroup instance for partial.Result.Errors
// - integration with Validated (and ValidatedNel, ValidatedNec) data type for partial transformers

// Important:
// You need to import io.scalaland.chimney.cats._ in order to have all the above in scope.

// Example

import io.scalaland.chimney._
import io.scalaland.chimney.dsl._
import io.scalaland.chimney.partial
import io.scalaland.chimney.cats._
import _root_.cats.data._

case class RegistrationForm(email: String, username: String, password: String, age: String)

case class RegisteredUser(email: String, username: String, passwordHash: String, age: Int)

def hashpw(pw: String): String =
  pw.reverse

def validateEmail(form: RegistrationForm): ValidatedNec[String, String] = {
  if (form.email.contains('@')) {
    Validated.valid(form.email)
  } else {
    Validated.invalid(NonEmptyChain(s"${form.username}'s email: does not contain '@' character"))
  }
}

def validateAge(form: RegistrationForm): ValidatedNec[String, Int] = form.age.toIntOption match {
  case Some(value) if value >= 18 => Validated.valid(value)
  case Some(value)                => Validated.invalid(NonEmptyChain(s"${form.username}'s age: must have at least 18 years"))
  case None                       => Validated.invalid(NonEmptyChain(s"${form.username}'s age: invalid number"))
}

implicit val partialTransformer: PartialTransformer[RegistrationForm, RegisteredUser] =
  PartialTransformer
    .define[RegistrationForm, RegisteredUser]
    .withFieldComputedPartial(_.email, form => validateEmail(form).toPartialResult)
    .withFieldComputed(_.passwordHash, form => hashpw(form.password))
    .withFieldComputedPartial(_.age, form => validateAge(form).toPartialResult)
    .buildTransformer

val resultOk =
  RegistrationForm("john@example.com", "John", "123456", "40")
    .transformIntoPartial[RegisteredUser]

resultOk.asValidatedNec

val resultBad =
  RegistrationForm("john@example.com", "John", "123456", "not a number")
    .transformIntoPartial[RegisteredUser]

resultBad.asValidatedNec

val resultOk2 =
  Array(
    RegistrationForm("john@example.com", "John", "123456", "40"),
    RegistrationForm("alice@example.com", "Alice", "123456", "19"),
    RegistrationForm("bob@example.com", "Bob", "123456", "21")
  ).transformIntoPartial[Array[RegisteredUser]]

resultOk2.asValidatedNec

val resultBad2 =
  Array(
    RegistrationForm("john_example.com", "John", "123456", "10"),
    RegistrationForm("alice@example.com", "Alice", "123456", "19"),
    RegistrationForm("bob@example.com", "Bob", "123456", "21.5")
  ).transformIntoPartial[Array[RegisteredUser]]

resultBad2.asValidatedNec
