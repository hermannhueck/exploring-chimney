// see:
// https://chimney.readthedocs.io/en/latest/transformers/scoped-configuration.html

// Providing scoped configuration

// Motivating example

import io.scalaland.chimney.dsl._

class Source { def field1: Int = 100 }
case class Target(field1: Int, field2: Option[String])

(new Source)
  .into[Target]
  .enableMethodAccessors
  .enableOptionDefaultsToNone
  .transform

// In order to make it working without providing any specific values,
// we must enable method accessors and None as Option default value.

// Instead of enabling them per use-site, we can define default transformer configuration in implicit scope.

// implicit val myTransformerConfig =
//   TransformerConfiguration
//     .default
//     .enableMethodAccessors
//     .enableOptionDefaultsToNone

// (new Source)
//   .into[Target]
//   .transform

// (new Source)
//   .transformInto[Target]

// Overriding scoped configuration locally

// (new Source)
//   .into[Target]
//   .disableMethodAccessors
//   .transform
// error: Chimney can't derive transformation from Source to Target
// Target
//   field1: scala.Int - no accessor named field1 in source type Source

implicit val myTransformerConfig2 =
  TransformerConfiguration
    .default
    .enableMethodAccessors

(new Source)
  .into[Target]
  .enableOptionDefaultsToNone
  .transform
