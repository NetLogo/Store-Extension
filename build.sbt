enablePlugins(org.nlogo.build.NetLogoExtension)

name       := "store"
version    := "1.0.2"
isSnapshot := true

scalaVersion           := "3.7.0"
Compile / scalaSource  := baseDirectory.value / "src" / "main"
Test / scalaSource     := baseDirectory.value / "src" / "test"
scalacOptions          ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-encoding", "us-ascii",
                               "-release", "17", "-Wunused:linted")

libraryDependencies += "com.h2database" % "h2" % "1.4.199"

netLogoExtName      := "store"
netLogoClassManager := "org.nlogo.extension.store.StoreExtension"
netLogoVersion      := "7.0.1"
