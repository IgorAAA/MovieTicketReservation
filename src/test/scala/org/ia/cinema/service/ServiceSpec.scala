package org.ia.cinema.service

import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.syntax.either._
import eu.timepit.refined.auto._
import io.circe.Decoder
import org.http4s.Uri
import org.ia.cinema.Config.ApplicationConf
import org.ia.cinema.TestUtils
import org.ia.cinema.api.Model.{MovieInfoResult, RegisterMovieMessage, ReserveSeatMessage}
import org.ia.cinema.client.HttpClient
import org.ia.cinema.model.Ids.CinemaId
import org.ia.cinema.model.SeatsAvailablity.{SeatsAvailable, SeatsReserved}
import org.ia.cinema.model.{ImdbResponse, RetrieveMovieInfo}
import org.ia.cinema.repository.{InMemoryCinemaRepository, SeatsForMovie}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ServiceSpec extends AnyWordSpecLike with Matchers {
  import ServiceSpec._
  import org.ia.cinema.TestUtils._

  "CinemaService" should {
    "register a movie" in {
      val res = for {
        conf    <- IO(ApplicationConf())
        state   <- Ref.of[IO, Map[CinemaId, SeatsForMovie]](Map.empty)
        repo    <- IO(new InMemoryCinemaRepository(state))
        service <- IO(new CinemaServiceImpl[IO](repo, testHttpClient, conf))
        _       <- service.registerMovie(RegisterMovieMessage(imdbId, screenId, seatsAvailable))
        movie   <- service.retrieveMovieInfo(RetrieveMovieInfo(imdbId, screenId))
      } yield movie

      res
        .map(
          _ shouldBe (MovieInfoResult(
            imdbId,
            screenId,
            movieTitle,
            seatsAvailable,
            SeatsReserved(0)
          ))
        )
        .unsafeRunSync()
    }

    "register a movie and reserve 2 seats" in {
      val res = for {
        conf    <- IO(ApplicationConf())
        state   <- Ref.of[IO, Map[CinemaId, SeatsForMovie]](Map.empty)
        repo    <- IO(new InMemoryCinemaRepository(state))
        service <- IO(new CinemaServiceImpl[IO](repo, testHttpClient, conf))
        _       <- service.registerMovie(RegisterMovieMessage(imdbId, screenId, seatsAvailable))
        _       <- service.reserveSeat(ReserveSeatMessage(imdbId, screenId))
        _       <- service.reserveSeat(ReserveSeatMessage(imdbId, screenId))
        movie   <- service.retrieveMovieInfo(RetrieveMovieInfo(imdbId, screenId))
      } yield movie

      res
        .map(
          _ shouldBe (MovieInfoResult(
            imdbId,
            screenId,
            movieTitle,
            SeatsAvailable(1),
            SeatsReserved(2)
          ))
        )
        .unsafeRunSync()
    }

    "register a movie twice" in {
      val res = for {
        conf    <- IO(ApplicationConf())
        state   <- Ref.of[IO, Map[CinemaId, SeatsForMovie]](Map.empty)
        repo    <- IO(new InMemoryCinemaRepository(state))
        service <- IO(new CinemaServiceImpl[IO](repo, testHttpClient, conf))
        _       <- service.registerMovie(RegisterMovieMessage(imdbId, screenId, seatsAvailable))
        _       <- service.registerMovie(RegisterMovieMessage(imdbId, screenId, SeatsAvailable(100)))
        movie   <- service.retrieveMovieInfo(RetrieveMovieInfo(imdbId, screenId))
      } yield movie

      res.attempt
        .map(_.leftMap(_.getMessage) shouldBe Left("Movie already registered"))
        .unsafeRunSync()
    }

    "register a movie and try to reserve more seats than initially available" in {
      val res = for {
        conf    <- IO(ApplicationConf())
        state   <- Ref.of[IO, Map[CinemaId, SeatsForMovie]](Map.empty)
        repo    <- IO(new InMemoryCinemaRepository(state))
        service <- IO(new CinemaServiceImpl[IO](repo, testHttpClient, conf))
        _       <- service.registerMovie(RegisterMovieMessage(imdbId, screenId, seatsAvailable))
        _       <- service.reserveSeat(ReserveSeatMessage(imdbId, screenId))
        _       <- service.reserveSeat(ReserveSeatMessage(imdbId, screenId))
        _       <- service.reserveSeat(ReserveSeatMessage(imdbId, screenId))
        _       <- service.reserveSeat(ReserveSeatMessage(imdbId, screenId))
        _       <- service.reserveSeat(ReserveSeatMessage(imdbId, screenId))
        movie   <- service.retrieveMovieInfo(RetrieveMovieInfo(imdbId, screenId))
      } yield movie

      res
        .map(
          _ shouldBe (MovieInfoResult(
            imdbId,
            screenId,
            movieTitle,
            SeatsAvailable(0),
            SeatsReserved(3)
          ))
        )
        .unsafeRunSync()
    }

    "register a movie and try to retrieve non-existent movie" in {
      val res = for {
        conf    <- IO(ApplicationConf())
        state   <- Ref.of[IO, Map[CinemaId, SeatsForMovie]](Map.empty)
        repo    <- IO(new InMemoryCinemaRepository(state))
        service <- IO(new CinemaServiceImpl[IO](repo, testHttpClient, conf))
        _       <- service.registerMovie(RegisterMovieMessage(imdbId, screenId, seatsAvailable))
        movie   <- service.retrieveMovieInfo(RetrieveMovieInfo(imdbId, "NONEXISTENT"))
      } yield movie

      res.attempt
        .map(_.leftMap(_.getMessage) shouldBe Left("no record"))
        .unsafeRunSync()
    }
  }
}

object ServiceSpec {
  val seatsAvailable = SeatsAvailable(3)

  val testHttpClient: HttpClient[IO, ImdbResponse] =
    new HttpClient[IO, ImdbResponse] {
      override def response(reqUri: Uri)(implicit dec: Decoder[ImdbResponse]): IO[ImdbResponse] =
        IO.pure(ImdbResponse(TestUtils.movieTitle))
    }
}
