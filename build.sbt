name := """url-shortener-play-scala"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.12"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0" % Test
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick"            % "5.2.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.2.0",
  "com.h2database" % "h2" % "2.1.214",
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

(Test / javaOptions) += "-Dslick.dbs.default.connectionTimeout=30 seconds"
(Test / javaOptions) ++= Seq("-Dconfig.file=conf/test.conf")