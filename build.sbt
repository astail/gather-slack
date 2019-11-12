organization := "net.astail"

name := "gather-slack"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.4",
  "com.github.slack-scala-client" %% "slack-scala-client" % "0.2.6",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j" % "slf4j-api" % "1.7.28"
)

enablePlugins(JavaAppPackaging)
