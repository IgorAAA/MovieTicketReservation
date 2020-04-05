package org.ia.cinema

import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import io.circe.syntax._
import org.ia.cinema.api.Model._
import org.ia.cinema.model.Ids.{ImdbId, ScreenId}
import org.ia.cinema.model.RetrieveMovieInfo
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.QueryParamDecoderMatcher
import org.ia.cinema.service.CinemaService

import scala.language.higherKinds

class ApiRoutes[F[_]: Sync] extends Http4sDsl[F] {
  val Cinema = "cinema"

  object ImdbIdQueryParamMatcher
      extends QueryParamDecoderMatcher[String]("imdb")
  object ScreenIdQueryParamMatcher
      extends QueryParamDecoderMatcher[String]("screen")

  def routes(repo: CinemaService[F]): HttpRoutes[F] = HttpRoutes.of {
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
    case GET -> Root / Cinema / "movie_info" :? ImdbIdQueryParamMatcher(imdb) +& ScreenIdQueryParamMatcher(
          screen
        ) =>
      repo
        .retrieveMovieInfo(RetrieveMovieInfo(ImdbId(imdb), ScreenId(screen)))
        .flatMap(success => Ok(success.asJson))
        .handleErrorWith(err => BadRequest(err.getMessage.asJson))

  }
}
