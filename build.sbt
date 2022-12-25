ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.1"

lazy val root = (project in file("."))
//  .enablePlugins(ZIOCLIPlugin)
  .settings(
    name := "scalamorph",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-parser" % "0.1.7",
      "dev.zio" %% "zio-cli" % "0.2.8",
      "dev.zio" %% "zio-prelude" % "1.0.0-RC16",
      "dev.zio" %% "zio-test" % "2.0.5" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.5" % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
//    CLIPlugin.zioCliMainClass := Some("com.github.aborg0.scalamorph.ScalaMorph"),
  )
