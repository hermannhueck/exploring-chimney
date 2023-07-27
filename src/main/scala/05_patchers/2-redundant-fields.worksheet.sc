// see:
// https://chimney.readthedocs.io/en/latest/patchers/redundant-fields.html

// Redundant fields in patchers

import io.scalaland.chimney.dsl._

// When patch case class contains a field that does not exist in patched object, Chimney will not be able to generate patcher.

case class User(id: Int, email: String, phone: Long)
case class UserUpdateForm(email: String, phone: Long, address: String)

val user = User(10, "abc@@domain.com", 1234567890L)

// user.patchUsing(UserUpdateForm("xyz@@domain.com", 123123123L, "some address"))
// error: Field named 'address' not found in target patching type User

// This default behavior is intentional to prevent silent oversight of typos in patcher field names.

// But there is a way to ignore redundant patcher fields explicitly with .ignoreRedundantPatcherFields operation.

user
  .using(UserUpdateForm("xyz@@domain.com", 123123123L, "some address"))
  .ignoreRedundantPatcherFields
  .patch
