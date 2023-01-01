package com.github.aborg0.scalamorph.cli

import java.nio.file.Path as JPath
import zio.cli.*
import zio.prelude.*
import zio.prelude.Assertion.Regex
import zio.stream.{ZPipeline, ZSink, ZStream}

import java.nio.charset.Charset

object ScalaMorph extends ZIOCliDefault {

  private val inputBinOption: Options[JPath] = Options.file("bin", Exists.Yes)
  private val outputBinOption: Options[Option[JPath]] = Options.file("bin", Exists.No).optional

  private val inputAffOption: Options[JPath] = Options.file("aff", Exists.Yes)

  private val inputDicOption: Options[JPath] = Options.file("dic", Exists.Yes)

  // TODO add help
  private val inputEncoding: Options[String] = Options.text("encoding").withDefault("ISO-8859-2")

  private val inputOptions: Options[((JPath, JPath), String)] = Options.Both(Options.Both(inputAffOption, inputDicOption), inputEncoding)

  sealed trait ScalaMorphOptions extends Product with Serializable

  object ScalaMorphOptions {
    object AffixFile extends Newtype[JPath]

    type AffixFile = AffixFile.Type

    object DictionaryFile extends Newtype[JPath]

    type DictionaryFile = DictionaryFile.Type

    object BinFile extends Newtype[JPath]

    type BinFile = BinFile.Type

    object EncodingType extends Subtype[String] {
      override inline def assertion: Assertion[String] = Assertion.matches("ISO-8859-\\d+|UTF-8")
    }

    type EncodingType = EncodingType.Type

    final case class AffDicInputs(aff: AffixFile, dic: DictionaryFile, encoding: EncodingType) extends ScalaMorphOptions

    final case class AffDicInputWithBinOutput(aff: AffixFile, dic: DictionaryFile, encoding: EncodingType, bin: BinFile) extends ScalaMorphOptions

    final case class BinInput(bin: BinFile) extends ScalaMorphOptions
  }

  import ScalaMorphOptions.*

  private val scalaMorph = Command[ScalaMorphOptions]("scalamorph", Options.OrElse(
    Options.Both(inputOptions, outputBinOption),
    inputBinOption,
  ).map {
    case Left((((aff, dic), enc), Some(bin))) => ScalaMorphOptions.AffDicInputWithBinOutput(AffixFile(aff), DictionaryFile(dic), EncodingType.make(enc).fold(_ => EncodingType("UTF-8"), identity), BinFile(bin))
    case Left((((aff, dic), enc), None)) => ScalaMorphOptions.AffDicInputs(AffixFile(aff), DictionaryFile(dic), EncodingType.make(enc).fold(_ => EncodingType("UTF-8"), identity))
    case Right(bin) => ScalaMorphOptions.BinInput(BinFile(bin))
  })

  override val cliApp: CliApp[Any, Throwable, ScalaMorphOptions] = CliApp.make("scalamorph", "0.0.1",
    HelpDoc.Span.text("morphological analyser for Hungarian and English language"), scalaMorph) {
    case ScalaMorphOptions.AffDicInputWithBinOutput(aff, dic, enc, bin) => zio.Console.printLine(s"aff: $aff dic: $dic bin: $bin").orDie
    case ScalaMorphOptions.BinInput(bin) => zio.Console.printLine(s"bin: $bin").orDie
    case ScalaMorphOptions.AffDicInputs(aff, dic, enc) =>
      val decoder = EncodingType.unwrap(enc) match
        case "UTF-8" => ZPipeline.utf8Decode
        case s => ZPipeline.decodeStringWith(Charset.forName(s))
      ZStream.fromPath(AffixFile.unwrap(aff)).via(decoder).runDrain *>
//        ZStream.fromPath(DictionaryFile.unwrap(dic)).via(decoder).runDrain *>
        zio.Console.printLine(s"aff: $aff dic: $dic encoding: $enc").orDie
    case _ => zio.Console.printLineError("Should not happen").orDie
  }
}
