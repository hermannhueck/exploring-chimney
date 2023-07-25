class MySuite extends munit.FunSuite {
  test("hello") {
    val obtained = 21 * 2
    val expected = 42
    assertEquals(obtained, expected)
  }
}
