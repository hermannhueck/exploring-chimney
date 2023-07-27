// see:
// https://chimney.readthedocs.io/en/latest/patchers/options-handling.html

// Handling optional fields

import io.scalaland.chimney.dsl._

case class User(id: Int, email: String, phone: Long)
case class UserPatch(email: Option[String], phone: Option[Long])

val user   = User(10, "abc@@domain.com", 1234567890L)
val update = UserPatch(email = Some("updated@@example.com"), phone = None)

user.patchUsing(update)

// Field phone remained the same as in the original user, while the optional e-mail string got updated from a patch object.

// Option[T] on both sides

// An interesting case appears when both patch case class and patched object define fields f: Option[T].
// Depending on values of f in patched object and patch, we would like to apply following semantic table.

/*

patchedObject.f  | patch.f       | patching result
-----------------|---------------|----------------
None             | Some(value)   | Some(value)
Some(value1)     | Some(value2)  | Some(value2)
Some(value)      | None          | None
None             | None          | ???

 */

case class User2(name: Option[String], age: Option[Int])
case class UserPatch2(name: Option[String], age: Option[Int])

val user2      = User2(Some("John"), Some(30))
val userPatch2 = UserPatch2(None, None)

user2.patchUsing(userPatch2)
// clears both fields: User2(None, None)

user2
  .using(userPatch2)
  .ignoreNoneInPatch
  .patch
// ignores updating both fields: User2(Some("John"), Some(30))
