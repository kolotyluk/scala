* * *
# THE SCALA REPOSITORY
### Structure and build system
* * *

This document describes the Scala core (core library and compiler) repository
and how to build it. For information about Scala as a language, you can visit
the web site http://www.scala-lang.org/

## Part I. The repository layout

Follows the file layout of the Scala repository. Files marked with a † are not
part of the Subversion repository but are either automatically generated by the
build script or user-created if needed.  This is not a complete listing.

    scala/
      build.number              The version number of the current distribution.
      build.xml                 The main (deprecated) Ant build script.
      xsbt                      A script which will resolve and run SBT.
      docs/                     Documentation and sample code.
      lib/                      Pre-compiled libraries for the build.
          fjbg.jar              The Java byte-code generation library.
          scala-compiler.jar    The stable reference ('starr') compiler jar
          scala-library.jar     The stable reference ('starr') library jar
          scala-library-src.jar A snapshot of the source used to build starr.
          ant/                  Support libraries for ant.
      project/                  The 0.10 sbt build.
      README                    The file you are currently reading.
      src/                      All the source files of Scala.
          actors/               The sources of the Actor library.
          compiler/             The sources of the Scala compiler.
          fjbg/                 The modified Fork-Join library used by actors.
          jline/                The modified jline source used in the REPL.
          library/              The sources of the core Scala library.
          partest/              The parallel test suite for Scala.
          swing/                The sources of the Swing library.
      target/                   Build products output directory for sbt.
      test/                     The Scala test suite.
      tools/                    Developer utilities.



## Part II. Building Scala with SBT

[SBT](http://github.com/harrah/xsbt) (Simple Build Tool) is used to compile
Scala.   SBT itself is written in scala.  The Scala files relating to the
build use the version of Scala associated with SBT, currently 2.9.1, while
the Scala bootstrapping process makes use of the local 'starr' reference.

### LAYERS:

In order to guarantee the bootstrapping of the Scala compiler, sbt builds
Scala in layers. Each layer is a complete compiled Scala compiler and library.
A superior layer is always compiled by the layer just below it. Here is a short
description of the four layers that used, from bottom to top:

`starr`: the stable reference Scala release which is shared by all the
developers. It is found in the repository as 'lib/scala-compiler.jar' and
'lib/scala-library.jar'. Any committable source code must be compiled directly
by starr to guarantee the bootstrapping of the compiler.

'locker': the local reference which is compiled by starr and is the work
compiler in a typical development cycle. When it has been built once, it is
“frozen” in this state. Updating it to fit the current source code must be
explicitly requested (see below).

'quick': the layer which is incrementally built when testing changes in the
compiler or library. This is considered an actual new version when locker is
up-to-date in relation to the source code.

'strap': a test layer used to check stability of the build.

### DEPENDENT CHANGES:

The build compiles, for each layer, the Scala library first and the compiler next.
That means that any changes in the library can immediately be used in the
compiler without an intermediate build. On the other hand, if building the
library requires changes in the compiler, a new locker must be built if
bootstrapping is still possible, or a new starr if it is not.

### REQUIREMENTS FOR BUILD:

The Scala build systm uses the Simple Build Tool
  - A Java runtime environment (JRE) or SDK 1.6 or above.
  - On Linux/Mac
    * bash to run the xsbt script
    * curl or wget for obtaining SBT
  - On Windows
    * A valid SBT installation.


## Part III. Common SBT session use-cases


'locker-lock'
  Compiles locker and 'locks' it from being recompiled on source changes.

'compile'
  A quick compilation (to quick) of your changes using the locker compiler.
    - This will rebuild all quick if locker changed.
    - This will rebuild locker if it is unlocked.

'locker-unlock'
  Marks that locker should be recompiled on source changes.

'test'
  Runs the full testing suite against the 'quick' compiler.


'doc'
  Generates the HTML documentation for the library from the sources using the
  scaladoc tool in quick.

'dist'
  Builds a 'mini' distribution.
    - Builds everything twice more and compares bit-to-bit the two builds (to
      make sure it is stable).
    - Creates a local distribution in 'target/scala-dist.zip'.

'clean'
  Removes all temporary build files (locker is preserved if locked).

TODO - Add optimise/non-optimise targets...

## Part V. Contributing to Scala

If you wish to contribute, you can find all of the necessary information on
the official Scala website: www.scala-lang.org.

Specifically, you can subscribe to the Scala mailing lists, read all of the
available documentation, and browse the live SVN repository.  You can contact
the Scala team by sending us a message on one of the mailing lists, or by using
the available contact form.

In detail:

* Scala website (links to everything else):
  http://www.scala-lang.org

* Scala documentation:
  http://www.scala-lang.org/node/197

* Scala mailing lists:
  http://www.scala-lang.org/node/199

* Scala bug and issue tracker:
  https://issues.scala-lang.org

* Scala live SVN source tree:
  http://www.scala-lang.org/node/213

* Contact form:
  http://www.scala-lang.org/node/188


If you are interested in contributing code, we ask you to complete and submit
to us the Scala Contributor License Agreement, which allows us to ensure that
all code submitted to the project is unencumbered by copyrights or patents.
The form is available at:
http://www.scala-lang.org/sites/default/files/contributor_agreement.pdf

Thank you!
The Scala Team

