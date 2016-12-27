import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.universal.UniversalPlugin

val http4sVersion = "0.14.11"

val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value) Some("snapshots" at s"${nexus}content/repositories/snapshots")
    else Some("releases" at s"${nexus}service/local/staging/deploy/maven2")
  },
  pomExtra := {
    <url>https://github.com/fomkin/korolev</url>
    <licenses>
      <license>
        <name>Apache License, Version 2.0</name>
        <url>http://apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:fomkin/korolev.git</url>
      <connection>scm:git:git@github.com:fomkin/korolev.git</connection>
    </scm>
    <developers>
      <developer>
        <id>fomkin</id>
        <name>Aleksey Fomkin</name>
        <email>aleksey.fomkin@gmail.com</email>
      </developer>
    </developers>
  }
)

val commonSettings = publishSettings ++ Seq(
  scalaVersion := "2.11.8",
  organization := "com.github.fomkin",
  version := "0.0.6",
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
  ),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-language:postfixOps",
    "-language:implicitConversions"
  )
)

val exampleSettings = commonSettings ++ Seq(
  libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.21",
  publish := {}
)

lazy val vdom = crossProject.crossType(CrossType.Pure).
  settings(commonSettings: _*).
  settings(normalizedName := "korolev-vdom")

lazy val vdomJS = vdom.js
lazy val vdomJVM = vdom.jvm

lazy val server = project.
  settings(commonSettings: _*).
  settings(
    normalizedName := "korolev-server",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "io.verizon.delorean" %% "core" % "1.1.37"
    )
  ).
  dependsOn(korolevJVM)

lazy val async = crossProject.crossType(CrossType.Pure).
  settings(commonSettings: _*).
  settings(normalizedName := "korolev-async")

lazy val asyncJS = async.js
lazy val asyncJVM = async.jvm

lazy val bridge = crossProject.crossType(CrossType.Pure).
  settings(commonSettings: _*).
  settings(
    normalizedName := "korolev-bridge",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % "0.4.4" % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    unmanagedResourceDirectories in Compile += file("bridge") / "src" / "main" / "resources"
  ).
  dependsOn(async)

lazy val bridgeJS = bridge.js
lazy val bridgeJVM = bridge.jvm

lazy val korolev = crossProject.crossType(CrossType.Pure).
  settings(commonSettings: _*).
  settings(
    normalizedName := "korolev",
    unmanagedResourceDirectories in Compile += file("korolev") / "src" / "main" / "resources"
  ).
  dependsOn(vdom, bridge)

lazy val korolevJS = korolev.js
lazy val korolevJVM = korolev.jvm

// Examples
val examples = file("examples")

lazy val simpleExample = (project in  examples / "simple").
  enablePlugins(JavaAppPackaging).
  enablePlugins(UniversalPlugin).
  settings(exampleSettings: _*).
  dependsOn(server)

lazy val routingExample = (project in examples / "routing").
  enablePlugins(JavaAppPackaging).
  enablePlugins(UniversalPlugin).
  settings(exampleSettings: _*).
  dependsOn(server)

lazy val gameOfLifeExample = (project in examples / "game-of-life").
  enablePlugins(JavaAppPackaging).
  enablePlugins(UniversalPlugin).
  settings(exampleSettings: _*).
  dependsOn(server)

lazy val root = project.in(file(".")).
  settings(publish := {}).
  aggregate(
    korolevJS, korolevJVM,
    bridgeJS, bridgeJVM,
    vdomJS, vdomJVM,
    asyncJS, asyncJVM,
    server, simpleExample,
    routingExample, gameOfLifeExample
  )

publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))

crossScalaVersions := Seq("2.11.8")

publishArtifact := false

