package org.ia.cinema.repository

import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.syntax.either._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.ia.cinema.api.Model.MovieInfoResult
import org.ia.cinema.model.Ids.CinemaId
import org.ia.cinema.model.SeatsAvailablity.{SeatsAvailable, SeatsReserved}

class InMemoryCinemaRepository(
  private val ref: Ref[IO, Map[CinemaId, SeatsForMovie]]
) extends CinemaRepository[IO] {

  private val log = Slf4jLogger.create[IO]

  override def registerMovie(cid: CinemaId,
                             seatsAvailable: SeatsAvailable,
                             movieTitle: String): IO[Unit] = {
    val x = ref.modify { state =>
      if (state.keySet.contains(cid))
        (state, new IllegalStateException("Movie already registered").asLeft)
      else {
        val newState =
          SeatsForMovie(movieTitle, seatsAvailable, SeatsReserved(0))
        (state + (cid -> newState), ().asRight)
      }
    }
    x.flatMap(IO.fromEither)
  }

  override def reserveSeat(cid: CinemaId, seatsRsvd: SeatsReserved): IO[Unit] =
    for {
      logger <- log
      _ <- ref.update { s =>
        val newState =
          for {
            found <- s
              .get(cid)
              .fold("Movie not found".asLeft[SeatsForMovie])(_.asRight)
            avalSts <- found.availableSeats - SeatsAvailable(seatsRsvd.value)
            newSt = s ++ Map(
              cid -> found.copy(
                reservedSeats = found.reservedSeats + seatsRsvd,
                availableSeats = avalSts
              )
            )
          } yield newSt
        newState.fold(err => { logger.error(err); s }, st => {
          logger.info(s"New state: $newState"); st
        })
      }
    } yield ()

  override def retrieveMovieInfo(cid: CinemaId): IO[MovieInfoResult] =
    ref.get
      .flatMap(
        _.get(cid)
          .map(
            seats =>
              MovieInfoResult(
                imdbId = cid.imdbId,
                screenId = cid.screenId,
                movieTitle = seats.movieTitle,
                availableSeats = seats.availableSeats,
                reservedSeats = seats.reservedSeats
            )
          )
          .fold(
            IO.raiseError[MovieInfoResult](
              new IllegalStateException("no record")
            )
          )(IO.pure)
      )
}
