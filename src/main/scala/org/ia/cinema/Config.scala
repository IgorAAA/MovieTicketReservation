package org.ia.cinema

import pureconfig._
import pureconfig.generic.auto._

object Config {
  final case class AppConf(host: String, port: Int)

  final case class ImdbConf(url: String, apiKey: String)

  final case class ApplicationConf(app: AppConf, imdb: ImdbConf)

  object ApplicationConf {
    def apply(): ApplicationConf =
      ConfigSource.default.loadOrThrow[ApplicationConf]
  }
}
