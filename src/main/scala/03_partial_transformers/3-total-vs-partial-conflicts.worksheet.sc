// see:
// https://chimney.readthedocs.io/en/latest/partial-transformers/totals-vs-partial-conflicts.html

// Total vs partial conflicts

import io.scalaland.chimney._
import io.scalaland.chimney.dsl._

case class UserAPI(credits: String)
case class User(credits: Int)

implicit val unsafeStringToInt: Transformer[String, Int] =
  _.toInt

implicit val parseStringToInt: PartialTransformer[String, Int] =
  PartialTransformer[String, Int](str => partial.Result.fromCatching(str.toInt).map(_ * 2))

// UserAPI("10")
//   .transformIntoPartial[User]
//   .asOption
// error: Ambiguous implicits while resolving Chimney recursive transformation:
//
//        PartialTransformer[String, Int]: parseStringToInt
//        Transformer[String, Int]: unsafeStringToInt

// To avoid the ambiguity, Chimney would fail the derivation in such case and expect you to tell it,
// which transformer it should prefer: total or partial:

// prefer total transformer
UserAPI("10")
  .intoPartial[User]
  .enableImplicitConflictResolution(PreferTotalTransformer)
  .transform
  .asOption

// prefer partial transformer
UserAPI("10")
  .intoPartial[User]
  .enableImplicitConflictResolution(PreferPartialTransformer)
  .transform
  .asOption
