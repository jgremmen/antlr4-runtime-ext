/**
 * Extensions and utilities for the ANTLR4 runtime library.
 * <p>
 * This module provides an abstract framework for building ANTLR4-based parsers with integrated syntax error handling,
 * custom vocabularies, and optimized parse tree walking. It simplifies the typical boilerplate involved in setting up
 * lexers, parsers, error listeners, and tree walkers.
 * <p>
 * The module exports the following packages:
 * <ul>
 *   <li>{@link de.sayayi.lib.antlr4} - core framework classes including the abstract parser base class,
 *       custom vocabulary support, and location token implementation</li>
 *   <li>{@link de.sayayi.lib.antlr4.syntax} - syntax error reporting and formatting utilities that produce
 *       human-readable error messages with visual source context</li>
 *   <li>{@link de.sayayi.lib.antlr4.walker} - optimized heap-based parse tree walkers that prevent stack overflows
 *       on deeply nested parse trees</li>
 * </ul>
 */
module de.sayayi.lib.antlr
{
  requires org.antlr.antlr4.runtime;

  requires static org.jetbrains.annotations;

  exports de.sayayi.lib.antlr4;
  exports de.sayayi.lib.antlr4.walker;
  exports de.sayayi.lib.antlr4.syntax;
}
