package org.ia.cinema.client

import cats.effect.{ConcurrentEffect, Sync}
import cats.syntax.apply._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.Stream
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.Decoder
import io.circe.fs2._
import org.http4s.{Method, Request, Uri}
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.blaze.BlazeClientBuilder
import org.typelevel.jawn.AsyncParser.SingleValue

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

// IMDB client api
trait HttpClient[F[_], Out] {
  def response(reqUri: Uri)(implicit dec: Decoder[Out]): F[Out]
}

class ImdbHttpClient[F[_] : ConcurrentEffect, Out](implicit ec: ExecutionContext)
    extends HttpClient[F, Out] {

  private val clientBuilder: BlazeClientBuilder[F] = BlazeClientBuilder[F](ec)

  override def response(reqUri: Uri)(implicit dec: Decoder[Out]): F[Out] =
    for {
      logger <- Slf4jLogger.create[F]
      resp <-
        clientBuilder.resource
          .use { client =>
            client.expect[Out](reqUri)
          }
          .handleErrorWith(err => logger.error(err.getMessage) *> Sync[F].raiseError(err))
      _ = logger.debug(resp.toString)
    } yield resp
}

class ImdbHttpStreamClient[F[_] : ConcurrentEffect](implicit ec: ExecutionContext) {
  private def request(uri: Uri): Request[F] = Request[F](Method.GET, uri)

  def retriveImdbMovieTitle[A : Decoder](reqUri: Uri): Stream[F, A] =
    for {
      logger <- Stream.eval(Slf4jLogger.create[F])
      client <- BlazeClientBuilder[F](ec).stream
      resp <-
        client
          .stream(request(reqUri))
          .flatMap(_.body.chunks.through(byteParserC(SingleValue)).through(decoder[F, A]))
          .handleErrorWith(
            err => Stream.eval(logger.error(err.getMessage)) >> Stream.raiseError(err)
          )
    } yield resp
}
