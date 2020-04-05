package org.ia.cinema.api

import io.circe._
import io.circe.generic.semiauto._
import org.ia.cinema.model.Ids.{ImdbId, ScreenId}
import org.ia.cinema.model.SeatsAvailablity.{SeatsAvailable, SeatsReserved}

object Model {

  /**
    * Register a movie
    *
    * @param imdbId         IMDB movie identifier
    * @param availableSeats The total seats available for this movie
    * @param screenId       An externally managed identifier of information when and where the movie is screened
    */
  case class RegisterMovieMessage(imdbId: ImdbId,
                                  screenId: ScreenId,
                                  availableSeats: SeatsAvailable)

  /**
    * Reserve a seat
    *
    * @param imdbId   IMDB movie identifier
    * @param screenId An externally managed identifier of information when and where the movie is screened
    */
  case class ReserveSeatMessage(imdbId: ImdbId, screenId: ScreenId)

  /**
    * Response for movie information and the number of seats available
    *
    * @param imdbId         IMDB movie identifier
    * @param screenId       An externally managed identifier of information when and where the movie is screened
    * @param movieTitle     A movie title
    * @param availableSeats The total seats available for this movie
    * @param reservedSeats  The total number of reserved seats for a movie and screen
    */
  case class MovieInfoResult(imdbId: ImdbId,
                             screenId: ScreenId,
                             movieTitle: String,
                             availableSeats: SeatsAvailable,
                             reservedSeats: SeatsReserved)

  import org.ia.cinema.model.Ids._

  implicit val movieDecoder: Decoder[RegisterMovieMessage] = deriveDecoder
  implicit val movieEncoder: Encoder[RegisterMovieMessage] = deriveEncoder

  implicit val seatDecoder: Decoder[ReserveSeatMessage] = deriveDecoder
  implicit val seatEncoder: Encoder[ReserveSeatMessage] = deriveEncoder

  implicit val movieInfoDecoder: Decoder[MovieInfoResult] = deriveDecoder
  implicit val movieInfoEncoder: Encoder[MovieInfoResult] = deriveEncoder
}
