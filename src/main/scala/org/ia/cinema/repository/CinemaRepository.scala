package org.ia.cinema.repository

import org.ia.cinema.api.Model._
import org.ia.cinema.model.Ids.{CinemaId, ImdbId}
import org.ia.cinema.model.SeatsAvailablity.{SeatsAvailable, SeatsReserved}

trait CinemaRepository[F[_]] {
  def registerMovie(cid: CinemaId, seatsAvailable: SeatsAvailable, movieTitle: String): F[Unit]
  def reserveSeat(cid: CinemaId, seatsReserved: SeatsReserved): F[Unit]
  def retrieveMovieInfo(cid: CinemaId): F[MovieInfoResult]
}
