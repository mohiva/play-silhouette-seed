import play.PlayScala

scalaVersion := "2.11.1"

name := "play-silhouette-seed"

version := "2.0-SNAPSHOT"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "2.0-SNAPSHOT",
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.1.1",
  "org.webjars" % "jquery" % "1.11.0",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta4",
  cache
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
