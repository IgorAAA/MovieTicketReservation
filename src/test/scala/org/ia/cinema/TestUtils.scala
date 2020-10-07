package org.ia.cinema

import eu.timepit.refined.auto._
import org.ia.cinema.model.Ids.{ImdbId, ScreenId}

object TestUtils {
  val imdbId: ImdbId     = "tt0111161"
  val screenId: ScreenId = "screen_123456"
  val movieTitle         = "The Shawshank Redemption"
}
