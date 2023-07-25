import scala.util.chaining._
import util._

@main
def MyApp3: Unit = // Scala 3 equivalent of `object MyApp extends App`
  line80.green pipe println
  "Hello, world from Scala 3 App!".yellow pipe println
  line80.green pipe println
