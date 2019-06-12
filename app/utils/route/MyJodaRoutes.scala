package utils.route

object MyJodaRoutes {
  val myJodaRoutes = new com.github.tototoshi.play2.routes.JodaRoutes {
    override val localDateFormat: String = "dd-MM-yyyy"
    override val dateTimeFormat: String = "dd-MM-yyyyTHH:mm:ss"
  }
}
