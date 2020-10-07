package org.ia.cinema.model

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.Or
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string._
import io.circe._
import io.circe.refined._

object Ids {
  type Imdb = StartsWith["tt"] Or
    StartsWith["nm"] Or StartsWith["co"] Or StartsWith["ev"] Or
    StartsWith["ch"] Or StartsWith["ni"]
  type ImdbId   = String Refined Imdb
  type ScreenId = String Refined NonEmpty
  final case class CinemaId(imdbId: ImdbId, screenId: ScreenId)

  implicit val imdbIdDec: Decoder[ImdbId] = refinedDecoder
  implicit val imdbIdEnc: Encoder[ImdbId] = refinedEncoder

  implicit val screenIdDec: Decoder[ScreenId] = refinedDecoder
  implicit val screenIdEnc: Encoder[ScreenId] = refinedEncoder
}
