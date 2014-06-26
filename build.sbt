import play.PlayScala

scalaVersion := "2.11.1"

name := "play-silhouette-seed"

version := "1.0"

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "1.0",
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.1.1",
  "org.webjars" % "jquery" % "1.11.0",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta4",
  "com.typesafe.slick" %% "slick" % "2.1.0-M2",
  "mysql" % "mysql-connector-java" % "5.1.18",
  cache
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
