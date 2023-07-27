// see:
// https://chimney.readthedocs.io/en/latest/transformers/default-values.html

// Default values support

import io.scalaland.chimney.dsl._

// Enabling default values in generated transformer

// Warning:
// Prior to version 0.7.0 fallback to default values was automatically enabled and required explicit disabling.

// Field’s default value can be enabled as a target value when constructing target object.
// The support for them has to be explicitly enabled to avoid accidents.

case class Catterpillar(size: Int, name: String)
case class Butterfly(size: Int, name: String, wingsColor: String = "purple")

val stevie = Catterpillar(5, "Steve")

// val steve = stevie.transformInto[Butterfly] // fails with
// error: Chimney can't derive transformation from Catterpillar to Butterfly
//
// Butterfly
//   wingsColor: String - no field named wingsColor in source type Catterpillar

val steve =
  stevie
    .into[Butterfly]
    .enableDefaultValues
    .transform

// Providing the value manually has always a priority over the default.

val steve2 =
  stevie
    .into[Butterfly]
    .enableDefaultValues
    .withFieldConst(_.wingsColor, "yellow")
    .transform

// Default values for Option fields

// In case you have added an optional field to a type, wanting to write migration from old data,
// usually you set new optional type to None.

case class Foo(a: Int, b: String)
case class FooV2(a: Int, b: String, newField: Option[Double])

// Usual approach would be to use .withFieldConst to set new field value or give newField field a default value.

Foo(5, "test")
  .into[FooV2]
  .withFieldConst(_.newField, None)
  .transform

// Alternatively use `enableOptionDefaultsToNone`

Foo(5, "test")
  .into[FooV2]
  .enableOptionDefaultsToNone
  .transform

// Default values for Unit fields

// Having a target case class type that contains a field of type Unit,
// Chimney is able to automatically fill it with the unit value `()`.

case class Foo2(x: Int, y: String)
case class Bar2(x: Int, y: String, z: Unit)

Foo2(10, "test")
  .transformInto[Bar2]
