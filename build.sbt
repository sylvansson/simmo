lazy val simmo =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings( // Normal SBT settings
      name := "simmo",
      version := "0.0.1",
      scalaVersion := "2.13.3",
      organization := "sylvanssoft",
      libraryDependencies ++= Seq(
        "com.beachape" %%% "enumeratum" % "1.6.1",
        "com.lihaoyi" %%% "utest" % "0.7.4" % "test",
        "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
      ),
      testFrameworks += new TestFramework("utest.runner.Framework")
    )
    .settings( // Indigo specific settings
      showCursor := true,
      title := "simmo",
      gameAssetsDirectory := "assets",
      windowStartWidth := 550,
      windowStartHeight := 400,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % "0.3.0",
        "io.indigoengine" %%% "indigo" % "0.3.0",
        "io.indigoengine" %%% "indigo-extras" % "0.3.0"
      )
    )

addCommandAlias("buildGame", ";compile;fastOptJS;indigoBuild")
addCommandAlias("runGame", ";compile;fastOptJS;indigoRun")
