import NativePackagerHelper._

name := "sobatch"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++=Seq(
  "com.typesafe.akka" % "akka-http-core_2.11" % "2.4.9-RC2",
  "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % "2.4.9-RC2",
  "com.typesafe" % "config" % "1.3.0",
  "org.scalactic" % "scalactic_2.11" % "3.0.0",
  "javax.mail" % "mail" % "1.4.1",
  "javax.activation" % "activation" % "1.1.1",
  "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.9",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
)

enablePlugins(JavaServerAppPackaging)

mainClass in Compile := Some("com.nbmfk.sobatch.Boot")

mappings in Universal ++= {
  directory("scripts") ++
  contentOf("src/main/resources").toMap.mapValues("config/" + _)
}

scriptClasspath := Seq("../config/") ++ scriptClasspath.value


licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))