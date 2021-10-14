import sbt.Credentials
import sbtrelease.ReleaseStateTransformations._

lazy val commonScalacOptions = Seq(
  "-feature",
  "-deprecation",
  "-encoding", "utf8",
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings")

lazy val buildSettings = Seq(
  name := "minimus",
  Global / organization := "com.waioeka",
  Global / scalaVersion := "3.0.2"
)

lazy val noPublishSettings = Seq(
    publish / skip := true
)

lazy val commonSettings = Seq(
  scalacOptions ++= commonScalacOptions,
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.6.1"
  )
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := Function.const(false),
  sonatypeProfileName := "com.waioeka",
  publishTo := Some(
    if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
    else Opts.resolver.sonatypeStaging
  ),
  autoAPIMappings := true,
  licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/lewismj/minimus"),
      "scm:git:git@github.com:lewismj/minimus.git"
   )
 ),
 developers := List(
  Developer(id="lewismj", name="minimus", email="@lewismj", url=url("https://github.com/lewismj"))
 )
)


lazy val minimusSettings = buildSettings ++ commonSettings 

lazy val minimus = project.in(file("."))
  .settings(moduleName := "root")
  .settings(noPublishSettings)
  .aggregate(docs, tests, core)

lazy val core = project.in(file("core"))
  .settings(moduleName := "minimus-core")
  .settings(minimusSettings)
  .settings(publishSettings)

lazy val docsMappingsAPIDir = settingKey[String]("Name of subdirectory in site target directory for api docs.")

lazy val docSettings = Seq(
  autoAPIMappings := true,
  micrositeName := "minimus",
  micrositeDescription := "minimus",
  micrositeBaseUrl :="/minimus",
  micrositeDocumentationUrl := "/minimus/api",
  micrositeGithubOwner := "lewismj",
  micrositeGithubRepo := "minimus",
  micrositeHighlightTheme := "atom-one-light",
  micrositePalette := Map(
    "brand-primary" -> "#5B5988",
    "brand-secondary" -> "#292E53",
    "brand-tertiary" -> "#222749",
    "gray-dark" -> "#49494B",
    "gray" -> "#7B7B7E",
    "gray-light" -> "#E5E5E6",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"),
  //git.remoteRepo := "git@github.com:lewismj/minimus.git",
  git.remoteRepo := "https://github.com/lewismj/minimus.git",
  ghpagesNoJekyll := false,
  ScalaUnidoc /unidoc / unidocProjectFilter := inAnyProject -- inProjects(tests),
  docsMappingsAPIDir := "api",
  addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, docsMappingsAPIDir),
  ghpagesNoJekyll := false,
  makeSite / includeFilter := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md" | "*.svg",
  Jekyll / includeFilter := (makeSite / includeFilter).value,
  mdocIn := (LocalRootProject / baseDirectory).value / "docs" / "src" / "main" / "mdoc",
  mdocExtraArguments := Seq("--no-link-hygiene"),
  ScalaUnidoc / unidoc / scalacOptions ++= Seq(
    "-doc-source-url",
    scmInfo.value.get.browseUrl + "/tree/main€{FILE_PATH}.scala",
    "-sourcepath",
    (LocalRootProject / baseDirectory).value.getAbsolutePath
 )
)

lazy val docs = project.in(file("docs"))
    .enablePlugins(MicrositesPlugin)
    .enablePlugins(ScalaUnidocPlugin, GhpagesPlugin)
    .settings(moduleName := "minimus-docs")
    .dependsOn(core)
    .settings(docSettings)
    .settings(minimusSettings)
    .settings(noPublishSettings)
    .settings(publishSettings)

lazy val tests = project.in(file("tests"))
  .dependsOn(core)
  .settings(moduleName := "minimus-tests")
  .settings(minimusSettings)
  .settings(noPublishSettings)
  .settings(
    coverageEnabled := false,
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
    libraryDependencies ++= Seq(
      "org.scalatest"  %% "scalatest" % "3.2.10" % "test",
      "org.scalacheck" %% "scalacheck" % "1.15.4" % "test"
    )
  )

lazy val bench = project.in(file("bench"))
  .dependsOn(core)
  .dependsOn(tests  % "test->test")
  .settings(moduleName := "minimus-bench")
  .settings(minimusSettings)
  .settings(noPublishSettings)
  .settings(
    coverageEnabled := false
  ).enablePlugins(JmhPlugin)
