package org.ia.cinema.api

import cats.ApplicativeError
import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import eu.timepit.refined._
import eu.timepit.refined.collection.NonEmpty
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.ia.cinema.api.Model._
import org.ia.cinema.model.Ids.Imdb
import org.ia.cinema.model.RetrieveMovieInfo
import org.ia.cinema.service.CinemaService

class ApiRoutes[F[_] : Sync] extends Http4sDsl[F] {
  import ApiRoutes.validateMovieInfo

  val Cinema = "cinema"

  object ImdbIdQueryParamMatcher   extends QueryParamDecoderMatcher[String]("imdb")
  object ScreenIdQueryParamMatcher extends QueryParamDecoderMatcher[String]("screen")

  def routes(repo: CinemaService[F]): HttpRoutes[F] =
    HttpRoutes.of {
      case req @ POST -> Root / Cinema / "register_movie" =>
        req
          .as[RegisterMovieMessage]
          .flatMap(req => repo.registerMovie(req))
          .flatMap(Created(_))
          .handleErrorWith(err => BadRequest(err.getMessage.asJson))
      case req @ PUT -> Root / Cinema / "reserve_seat" =>
        req
          .as[ReserveSeatMessage]
          .flatMap(req => repo.reserveSeat(req))
          .flatMap(Ok(_))
      case GET -> Root / Cinema / "movie_info" :? ImdbIdQueryParamMatcher(
            imdb
          ) +& ScreenIdQueryParamMatcher(screen) =>
        val validRetriveMovieInfo = validateMovieInfo(imdb, screen)
        val response = for {
          movieInfo <- ApplicativeError[F, Throwable].fromEither(validRetriveMovieInfo)
          response  <- repo.retrieveMovieInfo(movieInfo)
          result    <- Ok(response.asJson)
        } yield result

        response.handleErrorWith(err => BadRequest(err.getMessage.asJson))

    }
}

object ApiRoutes {
  private[api] def validateMovieInfo(imdb: String, screen: String) =
    (
      for {
        validImdb     <- refineV[Imdb](imdb)
        validScreenId <- refineV[NonEmpty](screen)
      } yield RetrieveMovieInfo(validImdb, validScreenId)
    ).leftMap(s => new IllegalArgumentException(s))
}
