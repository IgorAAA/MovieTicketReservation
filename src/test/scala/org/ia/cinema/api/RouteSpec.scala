package org.ia.cinema.api

import cats.effect._
import cats.scalatest.EitherMatchers
import eu.timepit.refined.auto._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.ia.cinema.api.ApiRoutes.validateMovieInfo
import org.ia.cinema.api.Model.{MovieInfoResult, RegisterMovieMessage, ReserveSeatMessage}
import org.ia.cinema.model.Ids.CinemaId
import org.ia.cinema.model.RetrieveMovieInfo
import org.ia.cinema.model.SeatsAvailablity._
import org.ia.cinema.service.CinemaService
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class RouteSpec extends AnyWordSpecLike with Matchers with EitherMatchers {
  import RouteSpec._

  "ApiRoute" should {

    "register a new movie" in {
      val response = routes.orNotFound.run(
        Request(method = POST, uri = uri"/cinema/register_movie")
          .withEntity(
            RegisterMovieMessage(happyPathCid.imdbId, happyPathCid.screenId, SeatsAvailable(100))
          )
      )
      check[Json](response, Status.Created, Some(().asJson)) shouldBe true
    }

    "reserve a seat" in {
      val response = routes.orNotFound.run {
        Request(method = PUT, uri = uri"/cinema/reserve_seat")
          .withEntity(ReserveSeatMessage(happyPathCid.imdbId, happyPathCid.screenId))
      }
      check[Json](response, Status.Ok, Some(().asJson)) shouldBe true
    }

    "retrieve movie title with seats both available and reserved" in {
      val uri = Uri.unsafeFromString(
        s"/cinema/movie_info?imdb=${happyPathCid.imdbId.value}&screen=${happyPathCid.screenId.value}"
      )
      val response = routes.orNotFound.run {
        Request(method = GET, uri = uri)
      }
      check[Json](
        response,
        Status.Ok,
        Some(
          MovieInfoResult(
            happyPathCid.imdbId,
            happyPathCid.screenId,
            happyPathTitle,
            happyPathSeatsAvail,
            happyPathSeatsResvd
          ).asJson
        )
      ) shouldBe true
    }

    "validate movie info with correct imdb (starting with 'ev')" in {
      validateMovieInfo("ev01", "nonEmptyId") shouldBe Right(
        RetrieveMovieInfo("ev01", "nonEmptyId")
      )
    }

    "validate movie info with incorrect imdb" in {
      validateMovieInfo("zws", "nonEmptyId") shouldBe left[IllegalArgumentException]
    }

    "validate movie info with empty screen id" in {
      validateMovieInfo("tt22", "") shouldBe left[IllegalArgumentException]
    }
  }

}

object RouteSpec {
  val happyPathCid        = CinemaId("tt12", "42")
  val happyPathSeatsAvail = SeatsAvailable(100)
  val happyPathSeatsResvd = SeatsReserved(50)
  val happyPathTitle      = "My Movie"

  val serviceMock = new CinemaService[IO] {
    override def registerMovie(msg: RegisterMovieMessage): IO[Unit] = {
      val cid = CinemaId(msg.imdbId, msg.screenId)
      if (cid == happyPathCid) IO.pure(())
      else IO.raiseError(new IllegalStateException("Cannot register a movie"))
    }

    override def reserveSeat(msg: ReserveSeatMessage): IO[Unit] = {
      val cid = CinemaId(msg.imdbId, msg.screenId)
      if (cid == happyPathCid) IO.pure(())
      else IO.raiseError(new IllegalStateException("Cannot reserve a seat"))
    }

    override def retrieveMovieInfo(msg: RetrieveMovieInfo): IO[MovieInfoResult] = {
      val cid = CinemaId(msg.imdbId, msg.screenId)
      if (cid == happyPathCid)
        IO.pure(
          MovieInfoResult(
            cid.imdbId,
            cid.screenId,
            happyPathTitle,
            happyPathSeatsAvail,
            happyPathSeatsResvd
          )
        )
      else
        IO.raiseError(new IllegalStateException("Cannot retrieve movie info"))
    }
  }

  val routes = new ApiRoutes[IO].routes(serviceMock)

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(implicit
    ev: EntityDecoder[IO, A]
  ): Boolean = {
    val actualResp  = actual.unsafeRunSync
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](
      actualResp.body.compile.toVector.unsafeRunSync.isEmpty
    )(expected => actualResp.as[A].unsafeRunSync == expected)
    statusCheck && bodyCheck
  }
}
