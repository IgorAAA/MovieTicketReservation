package org.ia.cinema.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class ImdbResponse(title: String)

object ImdbResponse {
  implicit val imdbResponseDec: Decoder[ImdbResponse] = deriveDecoder
}
