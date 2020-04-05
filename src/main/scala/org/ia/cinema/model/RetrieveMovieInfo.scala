package org.ia.cinema.model

import org.ia.cinema.model.Ids.{ImdbId, ScreenId}

case class RetrieveMovieInfo(imdbId: ImdbId, screenId: ScreenId)
