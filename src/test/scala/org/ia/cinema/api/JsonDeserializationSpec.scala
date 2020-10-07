package org.ia.cinema.api

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.ia.cinema.TestUtils._
import org.ia.cinema.api.Model._
import org.ia.cinema.model.SeatsAvailablity.{SeatsAvailable, SeatsReserved}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.io.Source

class JsonDeserializationSpec extends AnyWordSpecLike with Matchers {

  "Json deserializer" must {
    "be compatible with retrive_movie_info json" in {
      val jsonEncoded = MovieInfoResult(
        imdbId = imdbId,
        screenId = screenId,
        movieTitle = movieTitle,
        availableSeats = SeatsAvailable(100),
        reservedSeats = SeatsReserved(50)
      ).asJson

      val json =
        parse(Source.fromResource(s"jsons/retrieve_movie_info.json").mkString).getOrElse(Json.Null)

      jsonEncoded shouldBe json
    }
  }
}
