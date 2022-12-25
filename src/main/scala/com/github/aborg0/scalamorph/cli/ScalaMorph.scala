package com.github.aborg0.scalamorph.cli

import java.nio.file.{Path => JPath}

import zio.cli.*
import zio.prelude.*

object ScalaMorph extends ZIOCliDefault {

  private val inputBinOption: Options[JPath] = Options.file("bin", Exists.Yes)
  private val outputBinOption: Options[Option[JPath]] = Options.file("bin", Exists.No).optional

  private val inputAffOption: Options[JPath] = Options.file("aff", Exists.Yes)

  private val inputDicOption: Options[JPath] = Options.file("dic", Exists.Yes)

  private val inputOptions: Options.Both[JPath, JPath] = Options.Both(inputAffOption, inputDicOption)

  sealed trait ScalaMorphOptions extends Product with Serializable

  object ScalaMorphOptions {
    object AffixFile extends Newtype[JPath]
    type AffixFile = AffixFile.Type
    object DictionaryFile extends Newtype[JPath]
    type DictionaryFile = DictionaryFile.Type
    object BinFile extends Newtype[JPath]
    type BinFile = BinFile.Type

    final case class AffDicInputs(aff: AffixFile, dic: DictionaryFile) extends ScalaMorphOptions
    final case class AffDicInputWithBinOutput(aff: AffixFile, dic: DictionaryFile, bin: BinFile) extends ScalaMorphOptions
    final case class BinInput(bin: BinFile) extends ScalaMorphOptions
  }

  import ScalaMorphOptions.*
  private val scalaMorph = Command[ScalaMorphOptions]("scalamorph", Options.OrElse(
    Options.Both(inputOptions, outputBinOption),
    inputBinOption,
  ).map{
    case Left(((aff, dic), Some(bin))) => ScalaMorphOptions.AffDicInputWithBinOutput(AffixFile(aff), DictionaryFile(dic), BinFile(bin))
    case Left(((aff, dic), None)) => ScalaMorphOptions.AffDicInputs(AffixFile(aff), DictionaryFile(dic))
    case Right(bin) => ScalaMorphOptions.BinInput(BinFile(bin))
  })

  override val cliApp = CliApp.make("scalamorph", "0.0.1",
    HelpDoc.Span.text("morphological analyser for Hungarian and English language"), scalaMorph) {
    case ScalaMorphOptions.AffDicInputWithBinOutput(aff, dic, bin) => zio.Console.printLine(s"aff: $aff dic: $dic bin: $bin").orDie
    case ScalaMorphOptions.BinInput(bin) => zio.Console.printLine(s"bin: $bin").orDie
    case ScalaMorphOptions.AffDicInputs(aff, dic) => zio.Console.printLine(s"aff: $aff dic: $dic").orDie
  }
}
