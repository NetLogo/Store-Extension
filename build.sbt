enablePlugins(org.nlogo.build.NetLogoExtension)

netLogoExtName := "store"

netLogoClassManager := "org.nlogo.extension.store.StoreExtension"

netLogoZipSources := false

version := "1.0.0"

isSnapshot := true

scalaVersion := "2.12.8"

scalaSource in Compile := baseDirectory.value / "src"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-Xfatal-warnings", "-encoding", "us-ascii")

libraryDependencies += "com.h2database" % "h2" % "1.4.199"

// The remainder of this file is for options specific to bundled netlogo extensions
// if copying this extension to build your own, you need nothing past line 14 to build
// sample-scala.zip
netLogoTarget :=
  org.nlogo.build.NetLogoExtension.directoryTarget(baseDirectory.value)

netLogoVersion := "6.1.0-RC2"
