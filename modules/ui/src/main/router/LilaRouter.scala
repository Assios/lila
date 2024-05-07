package lila.ui

import play.api.mvc.{ PathBindable, QueryStringBindable }
import scalalib.newtypes.SameRuntime

import lila.core.id.*
import chess.variant.Variant

object LilaRouter:

  given opaquePathBindable[T, A](using
      sr: SameRuntime[A, T],
      rs: SameRuntime[T, A],
      bindable: PathBindable[A]
  ): PathBindable[T] =
    bindable.transform(sr.apply, rs.apply)

  given opaqueQueryStringBindable[T, A](using
      sr: SameRuntime[A, T],
      rs: SameRuntime[T, A],
      bindable: QueryStringBindable[A]
  ): QueryStringBindable[T] =
    bindable.transform(sr.apply, rs.apply)

  private def strPath[A](
      parse: String => Option[A],
      error: => String,
      write: A => String = (_: A).toString
  ): PathBindable[A] = new:
    def bind(_key: String, value: String) = parse(value).toRight(error)
    def unbind(_key: String, value: A)    = write(value)

  given PathBindable[UserStr] = strPath[UserStr](UserStr.read, "Invalid Lichess username")
  given PathBindable[PerfKey] = strPath[PerfKey](PerfKey.apply, "Invalid Lichess performance key")
  given PathBindable[Color] =
    strPath[Color](Color.fromName, "Invalid chess color, should be white or black", _.name)
  given PathBindable[GameId] = summon[PathBindable[GameAnyId]].transform(_.gameId, _.into(GameAnyId))

  private def urlEncode(str: String) = java.net.URLEncoder.encode(str, "utf-8")

  private def strQueryString[A](
      parse: String => Option[A],
      error: => String,
      write: A => String = (_: A).toString
  ): QueryStringBindable[A] = new:
    def bind(key: String, params: Map[String, Seq[String]]) =
      params
        .get(key)
        .flatMap(_.headOption)
        .map: value =>
          parse(value).toRight(error)
    def unbind(key: String, value: A) = s"${urlEncode(key)}=${urlEncode(write(value))}"

  given QueryStringBindable[Color] =
    strQueryString[Color](Color.fromName, "Invalid chess color, should be white or black", _.name)

  object conversions:
    given reportIdConv: Conversion[ReportId, String]       = _.value
    given Conversion[lila.core.i18n.Language, String]      = _.value
    given Conversion[PuzzleId, String]                     = _.value
    given Conversion[SimulId, String]                      = _.value
    given Conversion[SwissId, String]                      = _.value
    given Conversion[TourId, String]                       = _.value
    given Conversion[TeamId, String]                       = _.value
    given Conversion[RelayRoundId, String]                 = _.value
    given Conversion[chess.opening.OpeningKey, String]     = _.value
    given Conversion[chess.format.Uci, String]             = _.uci
    given postIdConv: Conversion[ForumPostId, String]      = _.value
    given Conversion[ForumCategId, String]                 = _.value
    given Conversion[ForumTopicId, String]                 = _.value
    given relayTourIdConv: Conversion[RelayTourId, String] = _.value
    given Conversion[chess.FideId, Int]                    = _.value
    given challengeIdConv: Conversion[ChallengeId, String] = _.value