package com.github.aborg0.scalamorph.parser

import com.github.aborg0.scalamorph.base.*

import zio.*
import zio.parser.*
import zio.prelude.*

import java.nio.charset.StandardCharsets

object AffParser {
  sealed trait Rule extends Product with Serializable

  object Rule {

    final case class FlagCount(count: NonNegativeInt) extends Rule
  }

  val rules: Syntax[Nothing, Char, Char, Rule] = ???
}
