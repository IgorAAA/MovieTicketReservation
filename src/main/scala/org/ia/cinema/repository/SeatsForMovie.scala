package org.ia.cinema.repository

import org.ia.cinema.model.SeatsAvailablity.{SeatsAvailable, SeatsReserved}

// Contains info for one movie in the cinema
final case class SeatsForMovie(movieTitle: String,
                               availableSeats: SeatsAvailable,
                               reservedSeats: SeatsReserved)
