package tech.kevinbreidenbach.typeleveldemo.http

object HttpApi {
//  def make[F[_]: ContextualLogger: Async](
//      healthCheckRoutes: HttpRoutes[F],
//      documentedRoutes: DocumentedRoutes[F],
//      prometheus: PrometheusResource[F]
//  ) = new HttpApi[F](healthCheckRoutes, documentedRoutes, undocumentedRoutes, prometheus)
}
//final class HttpApi[F[_]: ContextualLogger: Async] private (
//    healthCheckRoutes: HttpRoutes[F],
//    documentedRoutes: DocumentedRoutes[F],
//    prometheus: PrometheusResource[F]
//) {
//  private val loggedRoutes   = documentedRoutes.all <+> undocumentedRoutes.routes
//  // these logs are very noisy. We are intentionally not logging metrics req/res for that reason
//  private val unloggedRoutes = prometheus.client.routes <+> healthCheckRoutes
//
//  private val errorReporting: HttpRoutes[F] => HttpRoutes[F] =
//    routes =>
//      ErrorAction.httpRoutes.log(
//        httpRoutes = routes,
//        messageFailureLogAction = ContextualLogger[F].error(_)(_),
//        serviceErrorLogAction = ContextualLogger[F].error(_)(_)
//      )
//
//  private def fixPetStoreMiddleware(route: HttpRoutes[F]): HttpRoutes[F] = {
//    val switch: HttpRoutes[F] = Kleisli { (req: Request[F]) =>
//      if (
//        req.method == Method.GET &&
//        req.pathInfo.segments.contains(Segment("docs")) &&
//        req.pathInfo.segments.contains(Segment("index.html"))
//      ) {
//        val segs: Vector[Segment] = Vector(Segment("docs"))
//        route.run(req.withPathInfo(Path(segs)))
//      } else OptionT.none
//    }
//    switch <+> loggedRoutes
//  }
//
//  private val middleware: HttpRoutes[F] => HttpRoutes[F] = { (http: HttpRoutes[F]) =>
//    fixPetStoreMiddleware(http)
//  } andThen { (http: HttpRoutes[F]) =>
//    AutoSlash(http) // removes trailing slashes
//  } andThen { (http: HttpRoutes[F]) =>
//    CORS.policy.httpRoutes(http)
//  } andThen { (http: HttpRoutes[F]) =>
//    errorReporting(http)
//  } andThen { (http: HttpRoutes[F]) =>
//    Metrics.apply[F](prometheus.metricsOps)(http)
//  }
//
//  private val loggers: HttpRoutes[F] => HttpRoutes[F] = { (http: HttpRoutes[F]) =>
//    RequestLogger.httpRoutes(true, false)(http)
//  } andThen { (http: HttpRoutes[F]) =>
//    ResponseLogger.httpRoutes(true, false)(http)
//  }
//
//  val httpApp: HttpApp[F] = (loggers(middleware(loggedRoutes)) <+> unloggedRoutes).orNotFound
//}
