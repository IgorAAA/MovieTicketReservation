package org.ia.cinema.model

import io.circe._
import io.circe.generic.extras.semiauto._

object Ids {
  final case class ImdbId(value: String) extends AnyVal
  final case class ScreenId(value: String) extends AnyVal
  final case class CinemaId(imdbId: ImdbId, screenId: ScreenId)

  implicit val imdbIdDec: Decoder[ImdbId] = deriveUnwrappedDecoder
  implicit val imdbIdEnc: Encoder[ImdbId] = deriveUnwrappedEncoder

  implicit val screenIdDec: Decoder[ScreenId] = deriveUnwrappedDecoder
  implicit val screenIdEnc: Encoder[ScreenId] = deriveUnwrappedEncoder
}
