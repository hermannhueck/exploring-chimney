// see:
// https://chimney.readthedocs.io/en/latest/patchers/getting-started.html

// Getting started with patchers

import io.scalaland.chimney.dsl._

import `05_patchers`._ // for Email and Phone

case class User(id: Int, email: Email, phone: Phone)
case class UserUpdateForm(email: String, phone: Long)

// Let’s assume you want to apply update form to existing object of type User.

val user       = User(10, Email("abc@@domain.com"), Phone(1234567890L))
val updateForm = UserUpdateForm("xyz@@domain.com", 123123123L)

user.patchUsing(updateForm)
