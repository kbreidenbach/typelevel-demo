package tech.kevinbreidenbach.typeleveldemo.util

import cats.Applicative
import fs2.Stream
import fs2.text
import io.circe.Encoder
import io.circe.syntax.given

object CirceStreamEncoder {
  def pipe[F[_]: Applicative, T: Encoder]: Stream[F, T] => Stream[F, Byte] =
    ts =>
      (
        Stream.emit("[") ++
          ts.map(_.asJson.noSpaces).intersperse(",") ++
          Stream.emit("]")
      ).through(text.utf8.encode)
}
