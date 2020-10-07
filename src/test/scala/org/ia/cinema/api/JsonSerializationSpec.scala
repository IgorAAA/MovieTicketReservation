package org.ia.cinema.api

import cats.scalatest.EitherMatchers
import io.circe._
import io.circe.parser.decode
import org.ia.cinema.api.Model.{RegisterMovieMessage, ReserveSeatMessage}
import org.ia.cinema.model.SeatsAvailablity.SeatsAvailable
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.io.Source

class JsonSerializationSpec extends AnyWordSpecLike with EitherMatchers with Matchers {
  import JsonSerializationSpec._
  import org.ia.cinema.TestUtils._

  private val registerMovieMessage =
    RegisterMovieMessage(imdbId = imdbId, screenId = screenId, availableSeats = SeatsAvailable(100))

  private val reserveSeatMessage =
    ReserveSeatMessage(imdbId = imdbId, screenId = screenId)

  private def checkCompatibility[A : Encoder : Decoder](a: A, filename: String) = {
    val decodedJson = decodeJson[A](filename)

    decodedJson shouldBe right
    decodedJson.map { x =>
      x shouldBe a
    }
  }

  "Json serializer" must {
    "be compatible with RegisterMovieMessage" in {
      checkCompatibility(registerMovieMessage, "register_movie")
    }

    "be compatible with ReserveSeatMessage" in {
      checkCompatibility(reserveSeatMessage, "reserve_seat")
    }
  }
}

object JsonSerializationSpec {
  private def decodeJson[A : Decoder](filename: String): Either[Error, A] = {
    val json = Source.fromResource(s"jsons/$filename.json").mkString
    decode[A](json)
  }
}
