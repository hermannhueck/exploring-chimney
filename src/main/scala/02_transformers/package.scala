package object `02_transformers` {

  sealed trait Color extends Product with Serializable
  object Color {
    case object Red   extends Color
    case object Green extends Color
    case object Blue  extends Color
  }

  sealed trait Channel extends Product with Serializable
  object Channel {
    case object Alpha extends Channel
    case object Blue  extends Channel
    case object Green extends Channel
    case object Red   extends Channel
  }

  object rich  {
    case class PersonId(id: Int)        extends AnyVal
    case class PersonName(name: String) extends AnyVal
    case class Person(personId: PersonId, personName: PersonName, age: Int)
  }
  object plain {
    case class Person(personId: Int, personName: String, age: Int)
  }
}
