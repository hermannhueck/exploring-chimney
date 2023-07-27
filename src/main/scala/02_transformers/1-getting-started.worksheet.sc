// see:
// https://chimney.readthedocs.io/en/latest/transformers/getting-started.html

// Getting started with transformers

import io.scalaland.chimney.dsl._

// Basic transformations

case class Catterpillar(size: Int, name: String)
case class Butterfly(size: Int, name: String)

val stevie = Catterpillar(5, "Steve")
val steve  = stevie.transformInto[Butterfly]

// Nested transformations

case class Youngs(insects: List[Catterpillar])
case class Adults(insects: List[Butterfly])

val kindergarden = Youngs(List(Catterpillar(5, "Steve"), Catterpillar(4, "Joe")))
val highschool   = kindergarden.transformInto[Adults]
