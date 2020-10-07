package org.ia.cinema.service

import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.http4s._
import org.ia.cinema.Config.ApplicationConf
import org.ia.cinema.api.Model.{MovieInfoResult, RegisterMovieMessage, ReserveSeatMessage}
import org.ia.cinema.client.HttpClient
import org.ia.cinema.model.Ids.{CinemaId, ImdbId}
import org.ia.cinema.model.SeatsAvailablity.SeatsReserved
import org.ia.cinema.model.{ImdbResponse, RetrieveMovieInfo}
import org.ia.cinema.repository.CinemaRepository

trait CinemaService[F[_]] {
  def registerMovie(msg: RegisterMovieMessage): F[Unit]
  def reserveSeat(msg: ReserveSeatMessage): F[Unit]
  def retrieveMovieInfo(msg: RetrieveMovieInfo): F[MovieInfoResult]
}

class CinemaServiceImpl[F[_] : Sync](
  repository: CinemaRepository[F],
  imdbHttpClient: HttpClient[F, ImdbResponse],
  appConf: ApplicationConf
) extends CinemaService[F] {
  import org.ia.cinema.api.Model._
  override def registerMovie(msg: RegisterMovieMessage): F[Unit] =
    for {
      imdbResp <- getImdbMovieTitle(msg.imdbId)
      _ <- repository.registerMovie(
        CinemaId(msg.imdbId, msg.screenId),
        msg.availableSeats,
        imdbResp.title
      )
    } yield ()

  override def reserveSeat(msg: ReserveSeatMessage): F[Unit] =
    repository.reserveSeat(CinemaId(msg.imdbId, msg.screenId), SeatsReserved(1))

  override def retrieveMovieInfo(msg: RetrieveMovieInfo): F[MovieInfoResult] =
    repository.retrieveMovieInfo(CinemaId(msg.imdbId, msg.screenId))

  private def getImdbMovieTitle(imdbId: ImdbId): F[ImdbResponse] = {
    val imdbUrl: String = appConf.imdb.url
    val apiKey: String  = appConf.imdb.apiKey

    val uri: ParseResult[Uri] = Uri
      .fromString(s"$imdbUrl/${imdbId.value}")
      .map(_.withQueryParam("api_key", apiKey))
    uri.fold(err => err.raiseError, u => imdbHttpClient.response(u))
  }
}
