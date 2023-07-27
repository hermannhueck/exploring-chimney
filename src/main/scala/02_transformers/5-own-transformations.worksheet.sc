// see:
// https://chimney.readthedocs.io/en/latest/transformers/own-transformations.html

// Plugging in own transformations

// Transformer type class

// The library defines a type class Transformer that just wraps transformation function.

// trait Transformer[From, To] {
//   def transform(src: From): To
// }

// You can plug your own transformer in by providing implicit instance in a local context.

import io.scalaland.chimney.dsl._
import io.scalaland.chimney.Transformer

object v1 {
  case class User(id: Int, name: String, street: String, postalCode: String)
}
object v2 {
  case class Address(street: String, postalCode: String)
  case class User(id: Int, name: String, addresses: List[Address])
}

// 1.)

// implicit val userV1toV2_1: Transformer[v1.User, v2.User] =
//   (user: v1.User) =>
//     v2.User(
//       id = user.id,
//       name = user.name,
//       addresses = List(v2.Address(user.street, user.postalCode))
//     )

// Transformer definition DSL

// 2.) doesn't work

// Warning:
// While it looks reasonably, it will not work as expected :(
// This leads to a StackOverflowError.
// implicit val userV1toV2_2: Transformer[v1.User, v2.User] =
//   (user: v1.User) =>
//     user
//       .into[v2.User]
//       .withFieldComputed(_.addresses, u => List(v2.Address(u.street, u.postalCode)))
//       .transform

// 3.)

// Note
// Since version 0.4.0 there is a simple solution to this problem.

// We need to use special syntax Transformer.define[From, To]
// which introduces us to new transformer builder between types From and To.

implicit val userV1toV2_3: Transformer[v1.User, v2.User] =
  Transformer
    .define[v1.User, v2.User]
    .withFieldComputed(_.addresses, u => List(v2.Address(u.street, u.postalCode)))
    .buildTransformer

// In transformer builder we can use all operations available to usual transformer DSL.
// The only difference is that we don’t call .transform at the end (since we don’t transform value in place),
// but buildTransformer (because we generate transformer here).
// Such generated transformer is semantically equivalent to hand-written transformer from previous section.

// Chimney solves self reference implicit problem by not looking for implicit instance for Transformer[From, To]
// when using transformer builder Transformer.define[From, To].

val v1Users = List(
  v1.User(1, "Steve", "Love street", "27000"),
  v1.User(2, "Anna", "Broadway", "00321")
)

val v2Users =
  v1Users
    .transformInto[List[v2.User]]

// Recursive data types support

case class Foo(x: Option[Foo])
case class Bar(x: Option[Bar])

// We would like to define transformer instance which would be able to convert a value Foo(Some(Foo(None))) to Bar(Some(Bar(None))).
// In order to avoid aforementioned issues with self-referencing, you must define your recursive transformer instance
// as implicit def or implicit lazy val.

@annotation.nowarn("msg=Implicit resolves to enclosing method")
implicit def fooToBarTransformer: Transformer[Foo, Bar] =
  Transformer.derive[Foo, Bar] // or Transformer.define[Foo, Bar].buildTransformer

Foo(Some(Foo(None)))
  .transformInto[Bar]
