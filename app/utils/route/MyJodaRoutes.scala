package utils.route

object MyJodaRoutes {
  val myJodaRoutes = new com.github.tototoshi.play2.routes.JodaRoutes {
    override val format: String = "dd-MM-yyyy"
  }
}
