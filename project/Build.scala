import sbt._
import Keys._

object ScalaBuild extends LayeredBuild {
  // lazy val projects  = Seq(root, compQuick, libQuick)
  lazy val root      = Project("scala", file(".")) aggregate(lockerLib)

  // --------------------------------------------------------------
  //  Libraries used by Scalac that change infrequently
  //  (or hopefully so).
  // --------------------------------------------------------------


  // Jline nested project.   Compile this sucker once and be done.
  override lazy val jline = Project("jline", file("src/jline"))
  // Fast Java Bytecode Generator (nested in every scala-compiler.jar)
  override lazy val fjbg = Project("fjbg", file("."),
                          settings = Defaults.defaultSettings ++ Seq(
                            javaSource in Compile := file("src/fjbg"),
                            javacOptions ++= Seq("-target", "1.5"),
                            target := file("target/fjbg"),
                            (classDirectory in Compile) <<= target(_ / "classes")))

  // Forkjoin backport
  override lazy val forkjoin = Project("forkjoin", file("."),
                          settings = Defaults.defaultSettings ++ Seq(
                            javaSource in Compile := file("src/forkjoin"),
                            javacOptions ++= Seq("-target", "1.5"),  
                            target := file("target/forkjoin"),                          
                            (classDirectory in Compile) <<= target(_ / "classes")))

  // MSIL code generator
  // TODO - This probably needs to compile against quick, but Sabbus
  // had it building against locker, so we'll do worse and build
  // build against STARR for now.
  lazy val msil = Project("msil", file("."),
                          settings = Defaults.defaultSettings ++ Seq(
                            javaSource in Compile := file("src/msil"),
                            scalaSource in Compile := file("src/msil"),
                            defaultExcludes ~= (_ || "tests"),
                            javacOptions ++= Seq("-target", "1.5",
                                                 "-source", "1.4"),
                            target := file("target/msil"),                          
                            (classDirectory in Compile) <<= target(_ / "classes")))


  // --------------------------------------------------------------
  //  The magic kingdom.
  //  Layered compilation of Scala.
  //   Stable Reference -> Locker ('Lockable' dev version) -> Quick -> Strap (Binary compatibility testing)
  // --------------------------------------------------------------

  // Need a report on this...
  // TODO - Resolve STARR from a repo..
  def STARR = scalaInstance <<= appConfiguration map { app =>
    val launcher = app.provider.scalaProvider.launcher
    ScalaInstance(
      file("build/locker/classes/library"),
      file("build/locker/classes/compiler"),
      launcher,
      file("lib/fjbg.jar"))
  }

  // Locker is a lockable Scala compiler that can be built of 'current' source to perform rapid development.
  lazy val (lockerLib, lockerComp) = makeLayer("locker", STARR)
  lazy val locker = Project("locker", file(".")) aggregate(lockerLib, lockerComp)

  // Quick is the general purpose project layer for the Scala compiler.
  lazy val (quickLib, quickComp) = makeLayer("quick", makeScalaReference(lockerLib, lockerComp, fjbg))
  lazy val quick = Project("quick", file(".")) aggregate(quickLib, quickComp)
}
