/* NSC -- new Scala compiler
 * Copyright 2005-2010 LAMP/EPFL
 * @author Paul Phillips
 */
 
package scala.tools.nsc
package interpreter

import scala.reflect.NameTransformer

/** An interface for objects which are aware of tab completion and
 *  will supply their own candidates and resolve their own paths.
 */
trait CompletionAware {
  /** The delimiters which are meaningful when this CompletionAware
   *  object is in control.
   */
  // TODO
  // def delimiters(): List[Char] = List('.')

  /** The complete list of unqualified Strings to which this
   *  object will complete.
   */
  def completions(): List[String]
  def completions(start: String): List[String] = completions filter (_ startsWith start)
  
  /** Default filter to apply to completions.
   */
  def filterNotFunction(s: String): Boolean = false
  
  /** Default sort.
   */
  def sortFunction(s1: String, s2: String): Boolean = s1 < s2
  
  /** Default map.
   */
  def mapFunction(s: String) = NameTransformer decode s
  
  /** The next completor in the chain.
   */
  def follow(id: String): Option[CompletionAware] = None
  
  /** What to return if this completion is given as a command.  It
   *  returns None by default, which means to allow the repl to interpret
   *  the line normally.  Returning Some(_) means the line will never
   *  reach the scala interpreter.
   */
  def execute(id: String): Option[Any] = None
  
  /** Given string 'buf', return a list of all the strings
   *  to which it can complete.  This may involve delegating
   *  to other CompletionAware objects.
   */
  def completionsFor(parsed: Parsed): List[String] = {
    import parsed._
    
    def cs = 
      if (isEmpty) completions()
      else if (isUnqualified && !isLastDelimiter) completions(buffer)
      else follow(bufferHead) map (_ completionsFor bufferTail) getOrElse Nil
  
    cs filterNot filterNotFunction map mapFunction sortWith (sortFunction _)
  }
  
  /** TODO - unify this and completionsFor under a common traverser.
   */
  def executionFor(parsed: Parsed): Option[Any] = {
    import parsed._
    
    if (isUnqualified && !isLastDelimiter && (completions contains buffer)) execute(buffer)
    else if (!isQualified) None
    else follow(bufferHead) flatMap (_ executionFor bufferTail)
  }
}

object CompletionAware {
  val Empty = new CompletionAware { val completions = Nil }
  
  // class Forwarder(underlying: CompletionAware) extends CompletionAware {
  //   override def completions() = underlying.completions()
  //   override def filterNotFunction(s: String) = underlying.filterNotFunction(s)
  //   override def sortFunction(s1: String, s2: String) = underlying.sortFunction(s1, s2)
  //   override def mapFunction(s: String) = underlying.mapFunction(s)
  //   override def follow(id: String) = underlying.follow(id)
  //   override def execute(id: String) = underlying.execute(id)
  //   override def completionsFor(parsed: Parsed) = underlying.completionsFor(parsed)
  //   override def executionFor(parsed: Parsed) = underlying.executionFor(parsed)
  // } 
  //
  
  def unapply(that: Any): Option[CompletionAware] = that match {
    case x: CompletionAware => Some((x))
    case _                  => None
  }
  
  /** Create a CompletionAware object from the given functions.
   *  The first should generate the list of completions whenever queried,
   *  and the second should return Some(CompletionAware) object if
   *  subcompletions are possible.
   */
  def apply(terms: () => List[String], followFunction: String => Option[CompletionAware]): CompletionAware =
    new CompletionAware {
      def completions = terms()
      override def follow(id: String) = followFunction(id)
    }

  /** Convenience factories.
   */
  def apply(terms: () => List[String]): CompletionAware = apply(terms, _ => None)
  def apply(map: collection.Map[String, CompletionAware]): CompletionAware =
    apply(() => map.keys.toList, map.get _)
}

