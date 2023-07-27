// see:
// https://chimney.readthedocs.io/en/latest/index.html

// Welcome to Chimney’s documentation!

// Transformers

import java.time.ZonedDateTime
import scala.util.Random

case class MakeCoffee(id: Int, kind: String, addict: String)
case class CoffeeMade(id: Int, kind: String, forAddict: String, at: ZonedDateTime)

// Usual approach is to just rewrite fields one by one

val command =
  MakeCoffee(id = Random.nextInt(), kind = "Espresso", addict = "Piotr")

  // Usual approach is to just rewrite fields one by one

val event_ =
  CoffeeMade(id = command.id, kind = command.kind, forAddict = command.addict, at = ZonedDateTime.now)

// Chimney provides a compact DSL for such transformations

import io.scalaland.chimney.dsl._

val event = command
  .into[CoffeeMade]
  .withFieldComputed(_.at, _ => ZonedDateTime.now)
  .withFieldRenamed(_.addict, _.forAddict)
  .transform

// Partial transformers

// For computations that may potentially fail, Chimney provides partial transformers.

import io.scalaland.chimney.dsl._
import io.scalaland.chimney.partial._

case class UserForm(name: String, ageInput: String, email: Option[String])
case class User(name: String, age: Int, email: String)

UserForm("John", "21", Some("john@example.com"))
  .intoPartial[User]
  .withFieldComputedPartial(_.age, form => Result.fromCatching(form.ageInput.toInt))
  .transform
  .asOption

val result = UserForm("Ted", "eighteen", None)
  .intoPartial[User]
  .withFieldComputedPartial(_.age, form => Result.fromCatching(form.ageInput.toInt))
  .transform

result.asOption
result.asEither
result.asErrorPathMessages
result.asErrorPathMessageStrings

// Patching

case class User2(id: Int, email: String, address: String, phone: Long)
case class UserUpdateForm(email: String, phone: Long)

val user       =
  User2(10, "abc@example.com", "Broadway", 123456789L)
val updateForm =
  UserUpdateForm("xyz@example.com", 123123123L)

user
  .patchUsing(updateForm)
