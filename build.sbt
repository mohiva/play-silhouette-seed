import com.typesafe.sbt.SbtScalariform._

import scalariform.formatter.preferences._

name := "play-silhouette-seed"

version := "6.0.0"

scalaVersion := "2.12.8"

resolvers += Resolver.jcenterRepo

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "6.0.1-SNAPSHOT",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "6.0.1-SNAPSHOT",
  "com.mohiva" %% "play-silhouette-persistence" % "6.0.1-SNAPSHOT",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "6.0.1-SNAPSHOT",
  "org.webjars" %% "webjars-play" % "2.7.0",
  "org.webjars" % "bootstrap" % "3.3.7-1" exclude("org.webjars", "jquery"),
  "org.webjars" % "jquery" % "3.2.1",
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "com.iheart" %% "ficus" % "1.4.3",
  "com.typesafe.play" %% "play-mailer" % "7.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "7.0.0",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.6.1-akka-2.5.x",
  "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B3-SNAPSHOT",
  "com.nappin" %% "play-recaptcha" % "2.3",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "com.typesafe.play" %% "play-slick" % "4.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.1",
  "com.typesafe.slick" %% "slick" % "3.3.0",
  "com.typesafe.slick" %% "slick-codegen" % "3.3.0",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.4.0",
  "joda-time" % "joda-time" % "2.10.1",
  "org.joda" % "joda-convert" % "1.9.2",
  "mysql" % "mysql-connector-java" % "8.0.16",
  "com.mohiva" %% "play-silhouette-testkit" % "6.0.1-SNAPSHOT" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % "test",
  specs2 % Test,
  ehcache,
  guice,
  filters
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

routesImport += "utils.route.Binders._"

// https://github.com/playframework/twirl/issues/105
TwirlKeys.templateImports := Seq()

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  //"-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  // Play has a lot of issues with unused imports and unsued params
  // https://github.com/playframework/playframework/issues/6690
  // https://github.com/playframework/twirl/issues/105
  "-Xlint:-unused,_"
)

//********************************************************
// Scalariform settings
//********************************************************

scalariformAutoformat := true

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentConstructorArguments, false)
  .setPreference(DanglingCloseParenthesis, Preserve)

fork in Test := true
javaOptions in Test += "-Dconfig.file=conf/application.test.conf"