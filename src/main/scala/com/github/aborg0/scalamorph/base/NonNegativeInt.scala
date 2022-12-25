package com.github.aborg0.scalamorph.base

import zio.*
import zio.parser.*
import zio.prelude.*

object NonNegativeInt extends Subtype[Int] {
  override inline def assertion: Assertion[RuntimeFlags] =
    Assertion.greaterThanOrEqualTo(0)

  private[base] def unsafeMake(i: Int) = NonNegativeInt.wrap(i)

  val nonNegativeInt: Syntax[String, Char, Char, NonNegativeInt] = Syntax.digit.+.transformEither[String, NonNegativeInt](
    cs => {
      val number = new String(cs.toArray)
      number.toIntOption.toRight[String](s"Cannot parse $number as a 32 bit signed int").flatMap(i => NonNegativeInt.make(i).toEitherAssociative)
    },
    nonNeg => Right(Chunk[Char](nonNeg.toString.toCharArray: _*))
  )
}
type NonNegativeInt = NonNegativeInt.Type

