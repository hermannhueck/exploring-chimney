// see:
// https://chimney.readthedocs.io/en/latest/partial-transformers/partial-transformers.html

// Partial transformers

import io.scalaland.chimney.dsl._

case class RegistrationForm(email: String, username: String, password: String, age: String)

case class RegisteredUser(email: String, username: String, passwordHash: String, age: Int)

// We would like to hash the password and parse provided age: String field into a correct Int or return an error,
// when the value is not valid integer. This is not possible using total Transformer type.

import io.scalaland.chimney.partial

val okForm = RegistrationForm("john@example.com", "John", "123456", "40")

def hashpw(pw: String): String =
  pw.reverse

val resultOk = okForm
  .intoPartial[RegisteredUser]
  .withFieldComputed(_.passwordHash, form => hashpw(form.password))
  .withFieldComputedPartial(_.age, form => partial.Result.fromOption(form.age.toIntOption))
  .transform

resultOk.asOption
resultOk.asEither

resultOk match {
  case partial.Result.Value(value)   => println(s"transformed to: $value")
  case partial.Result.Errors(errors) => println(s"got ${errors.size} errors")
}

// Capturing errors

val badForm = RegistrationForm("john@example.com", "John", "123456", "not a number")

val resultBad = badForm
  .intoPartial[RegisteredUser]
  .withFieldComputed(_.passwordHash, form => hashpw(form.password))
  .withFieldComputedPartial(_.age, form => partial.Result.fromOption(form.age.toIntOption))
  .transform

resultBad.asOption
resultBad.asEither

resultBad match {
  case partial.Result.Value(value)   => println(s"transformed to: $value")
  case partial.Result.Errors(errors) => println(s"got ${errors.size} errors")
}

// additional convenience methods for quick accessing error information,
// together with path to the affected field
resultBad.asErrorPathMessages
resultBad.asErrorPathMessageStrings

// See also Cats integration for other ways of accessing error info.

// Custom error messages

import scala.util.{Failure, Success, Try}

// using custom error message with explicit usage of Try
val resultBad2 = badForm
  .intoPartial[RegisteredUser]
  .withFieldComputed(_.passwordHash, form => hashpw(form.password))
  .withFieldComputedPartial(
    _.age,
    form => {
      Try(form.age.toInt) match {
        case Success(value) => partial.Result.fromValue(value)
        case Failure(why)   => partial.Result.fromErrorString(why.toString)
      }
    }
  )
  .transform

// using built-in Try integration
val resultBad3 = badForm
  .intoPartial[RegisteredUser]
  .withFieldComputed(_.passwordHash, form => hashpw(form.password))
  .withFieldComputedPartial(_.age, form => partial.Result.fromTry(Try(form.age.toInt)))
  .transform

// or catching the exception directly, without Try acting as intermediary
val resultBad4 = badForm
  .intoPartial[RegisteredUser]
  .withFieldComputed(_.passwordHash, form => hashpw(form.password))
  .withFieldComputedPartial(_.age, form => partial.Result.fromCatching(form.age.toInt))
  .transform

resultBad2.asOption
resultBad2.asEither

resultBad2.asErrorPathMessages
resultBad2.asErrorPathMessageStrings

// Partial transformers operations

// Similar to withFieldConst, withFieldComputed, withCoproductInstance operations, there are partial counterparts available:

// - withFieldConstPartial
// - withFieldComputedPartial
// - withCoproductInstancePartial

// Analogously to Transformer definition DSL for Transformer, we can define above transformation
// as implicit PartialTransformer[RegistrationForm, RegisteredUser]. In order to do this,
// we use PartialTransformer.define (or equivalently Transformer.definePartial).

def method() = {

  import io.scalaland.chimney._

  @annotation.nowarn("msg=never used")
  implicit val transformer: PartialTransformer[RegistrationForm, RegisteredUser] =
    PartialTransformer
      .define[RegistrationForm, RegisteredUser]
      .withFieldComputed(_.passwordHash, form => hashpw(form.password))
      .withFieldComputedPartial(_.age, form => partial.Result.fromCatching(form.age.toInt))
      .buildTransformer

  val result1 = Array(okForm, badForm)
    .transformIntoPartial[List[RegisteredUser]]

  // Short-circuit semantics: transformFailFast

  // By default, partial transformers work in the error-accumulating mode, meaning that given the first error,
  // they progress the computation to capture all the possible errors that might happen later.

  val result2 = Array(badForm, okForm, badForm.copy(age = null))
    .transformIntoPartial[List[RegisteredUser]]
    .asErrorPathMessageStrings
  // List(
  //   ("(0).age", "For input string: \"not a number\""),
  //   ("(2).age", "Cannot parse null string")
  // )

  // short circuiting the computation after the first error is encountered
  // using transformFailFast
  val result3 = Array(badForm, okForm, badForm.copy(age = null))
    .intoPartial[List[RegisteredUser]]
    .transformFailFast
    .asErrorPathMessageStrings
  // List(
  //   ("(0).age", "For input string: \"not a number\""),
  // )

  (result1, result2, result3)
}

val (result1, result2, result3) = method()

result1
result2
result3
