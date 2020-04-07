package org.ia.cinema

import cats.effect.concurrent.Ref
import cats.effect.{ ExitCode, IO, IOApp }
import cats.syntax.functor._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.ia.cinema.Config.ApplicationConf
import org.ia.cinema.client.ImdbHttpClient
import org.ia.cinema.model.Ids.CinemaId
import org.ia.cinema.model.ImdbResponse
import org.ia.cinema.repository.{ InMemoryCinemaRepository, SeatsForMovie }
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.ia.cinema.api.ApiRoutes
import org.ia.cinema.service.CinemaServiceImpl

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  implicit val ec: ExecutionContext = ExecutionContext.global

  private val httpClient = new ImdbHttpClient[IO, ImdbResponse]

  private val httpServer = BlazeServerBuilder[IO]
    .bindHttp(8080, "localhost")

  val program: IO[Unit] =
    for {
      state <- Ref.of[IO, Map[CinemaId, SeatsForMovie]](Map.empty)
      conf <- IO(ApplicationConf())
      repo = new InMemoryCinemaRepository(state)
      service = new CinemaServiceImpl[IO](repo, httpClient, conf)
      _ <- httpServer
        .withHttpApp(new ApiRoutes[IO].routes(service).orNotFound)
        .resource
        .use(_ => IO.never)
    } yield ()

  override def run(args: List[String]): IO[ExitCode] = {
    Slf4jLogger
      .create[IO]
      .flatMap(
        logger =>
          program
            .handleErrorWith(err => logger.error(err.getMessage))
      )
      .as(ExitCode.Success)
  }
}
