package com.github.aborg0.scalamorph.base

import zio.*
import zio.parser.*
import zio.parser.Parser.ParserError
import zio.test.*
import zio.test.Assertion.*

object NonNegativeIntTest extends ZIOSpecDefault {
  private val syntaxUnderTest = NonNegativeInt.nonNegativeInt
  override def spec: Spec[Any, Nothing] = suite("NonNegativeInt syntax spec")(
    test("can parse 0") {
      assert(syntaxUnderTest.parseString("0"))(isRight(equalTo(NonNegativeInt(0))))
    },
    test("can parse positive numbers") {
      check(Gen.int.filter(_ > 0)) { i =>
        assert(syntaxUnderTest.parseString(i.toString))(isRight(equalTo(NonNegativeInt.unsafeMake(i))))
      }
    },
    test("fail for negative numbers") {
      check(Gen.int.filter(_ < 0)) { i =>
        assert(syntaxUnderTest.parseString(i.toString))(isLeft(equalTo(
          ParserError.UnexpectedEndOfInput)))//Failure[String](List.empty[String], 0, s"$i is negative"))))
      }
    },
    test("fail for too large numbers") {
      assert(syntaxUnderTest.parseString((Int.MaxValue + 1L).toString))(isLeft(equalTo(
        ParserError.Failure(List.empty[String], 10, "Cannot parse 2147483648 as a 32 bit signed int"))))
    },
    test("can generate non-negative values") {
      check(Gen.int.filter(_ >= 0)) { i =>
        assert(syntaxUnderTest.print(NonNegativeInt.unsafeMake(i)))(isRight(equalTo(Chunk(i.toString.toCharArray: _*))))
      }
    },
    test("cannot generate negative values (not possible outside of this package)") {
      check(Gen.int.filter(_ < 0)) { i =>
        assert(syntaxUnderTest.print(NonNegativeInt.unsafeMake(i)))(isLeft(equalTo("not a digit")))
      }
    }
  )
}
