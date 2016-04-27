name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "angularjs" % "1.5.0",
  "org.webjars" % "bootstrap" % "3.3.6"
)

resolvers := Seq[Resolver](
//"Typesafe maven Releases" at "https://dl.bintray.com/typesafe/maven-releases/",
//
//"Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/maven-releases/",
//
//"scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",

"Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
//,
//
//"Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
//
//"Typesafe Simple Repository" at "http://repo.typesafe.com/typesafe/simple/maven-releases/",
//
//"Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
)


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator

