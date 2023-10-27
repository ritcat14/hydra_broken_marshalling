lazy val akkaHttpVersion = "10.5.3"
lazy val akkaVersion    = "2.8.5"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "3.3.1"
    )),
    name := "hydra_broken_marshalling",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"       %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka"       %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka"       %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka"       %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"          % "logback-classic"           % "1.4.7",
      "com.thesamet.scalapb"    %% "scalapb-runtime"          % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "com.thesamet.scalapb"    %% "scalapb-json4s"           % "0.12.0",
      "commons-codec"           % "commons-codec"             % "1.16.0",
      "org.bouncycastle"        % "bcprov-jdk18on"            % "1.76",
      "org.bouncycastle"        % "bcpkix-jdk18on"            % "1.76",

      "com.typesafe.akka"       %% "akka-http-testkit"        % akkaHttpVersion                                         % Test,
      "com.typesafe.akka"       %% "akka-actor-testkit-typed" % akkaVersion                                             % Test,
      "org.scalatest"           %% "scalatest"                % "3.2.15"                                                % Test
    )
  )
