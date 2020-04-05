package org.ia.cinema.model

import cats.syntax.either._
import io.circe._
import io.circe.generic.extras.semiauto._

object SeatsAvailablity {
  final case class SeatsAvailable(value: Int) {
    def -(that: SeatsAvailable): Either[String, SeatsAvailable] =
      if (value >= that.value)
        Either.right(SeatsAvailable(this.value - that.value))
      else
        Either.left("Seats are unavailable")
  }

  final case class SeatsReserved(value: Int) {
    def +(that: SeatsReserved): SeatsReserved =
      SeatsReserved(this.value + that.value)
  }

  implicit val saDec: Decoder[SeatsAvailable] = deriveUnwrappedDecoder
  implicit val saEnc: Encoder[SeatsAvailable] = deriveUnwrappedEncoder

  implicit val srDec: Decoder[SeatsReserved] = deriveUnwrappedDecoder
  implicit val srEnc: Encoder[SeatsReserved] = deriveUnwrappedEncoder
}
