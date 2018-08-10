name := """play-scala-seed"""
organization := "cagecode"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

val buildVersion = "0.16.0"

version := buildVersion

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "Sonatype Staging" at "https://oss.sonatype.org/content/repositories/staging/")

scalacOptions in Compile += "-target:jvm-1.8"

scalaVersion := "2.12.6"

libraryDependencies ++= {
  val (playVer, nativeVer) = buildVersion.span(_ != '-') match {
    case (major, "") =>
      s"${major}-play26" -> s"${major}-linux-x86-64"

    case (major, mod) =>
      s"${major}-play26${mod}" -> s"${major}-linux-x86-64${mod}"
  }

  Seq(
    guice,
    "com.typesafe.play" %% "play-iteratees" % "2.6.1",
    "com.typesafe.akka" %% "akka-slf4j" % "2.5.4",
    "org.reactivemongo" %% "play2-reactivemongo" % playVer,
    "org.reactivemongo" % "reactivemongo-shaded-native" % nativeVer,
//    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0-M2" % Test,

  "org.mockito" % "mockito-all" % "1.10.19" % Test

  )
}

libraryDependencies += "org.webjars.bower" % "bootstrap-sass" % "3.3.6"

routesGenerator := InjectedRoutesGenerator

fork in run := true
