// see:
// https://chimney.readthedocs.io/en/latest/transformers/java-beans.html

// Java Beans: enableBeanGetters

import io.scalaland.chimney.dsl._

// Reading from Java beans
class MyBean(private var id: Long, private var name: String, private var flag: Boolean) {
  def getId: Long     = id
  def getName: String = name
  def isFlag: Boolean = flag
}

case class MyCaseClass(id: Long, name: String, flag: Boolean)

new MyBean(1L, "beanie", true)
  .into[MyCaseClass]
  .enableBeanGetters
  .transform

// Writing to Java beans: enableBeanSetters

// Chimney considers as bean a class that:
// - primary constructor is public and parameter-less
// - contains at least one, single-parameter setter method that returns Unit

// Chimney will then require data sources for all such setters.

class MyBean2 {
  private var id: Long      = _
  private var name: String  = _
  private var flag: Boolean = _

  def getId: Long           = id
  def setId(id: Long): Unit = { this.id = id }

  def getName: String             = name
  def setName(name: String): Unit = { this.name = name }

  def isFlag: Boolean              = flag
  def setFlag(flag: Boolean): Unit = { this.flag = flag }
}

val obj  =
  MyCaseClass(10L, "beanie", true)
val bean = obj
  .into[MyBean2]
  .enableBeanSetters
  .transform

// Chimney generates code equivalent to:

val bean2 = new MyBean2
bean2.setId(obj.id)
bean2.setName(obj.name)
bean2.setFlag(obj.flag)

// Current limitations

// Currently it’s not possible to override or provide values for missing setters.
