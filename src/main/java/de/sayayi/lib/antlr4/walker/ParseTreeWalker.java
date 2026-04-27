/*
 * Copyright 2022 Jeroen Gremmen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sayayi.lib.antlr4.walker;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.IterativeParseTreeWalker;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;


/**
 * Internal utility class that provides heap-based iterative tree walking implementations.
 * <p>
 * This class contains the actual walking logic used by the {@link Walker} enum constants. All walking methods use
 * iterative algorithms with heap-based data structures instead of recursion, which prevents stack overflow errors when
 * traversing deeply nested parse trees.
 * <p>
 * This class is package-private and not intended for direct use outside this package. Use the {@link Walker} enum
 * for tree traversal instead.
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 *
 * @see Walker
 */
final class ParseTreeWalker
{
  private ParseTreeWalker() {
    // no instance
  }


  /**
   * Walks the parse tree and invokes only rule-specific enter methods on the listener.
   * <p>
   * This method performs a pre-order depth-first traversal of the parse tree, calling enter methods on parser rules
   * before processing their children. No exit methods, terminal node visits, or error node visits are invoked.
   *
   * @param listener           the parse tree listener to receive enter callbacks, not {@code null}
   * @param parserRuleContext  the root context to start walking from, not {@code null}
   *
   * @since 0.7.1
   */
  @Contract(mutates = "param2")
  static void walkEnterOnlyIterative(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext)
  {
    final var nodeStack = new ArrayDeque<ParserRuleContextNode>();
    nodeStack.addFirst(new ParserRuleContextNode(parserRuleContext));

    for(ParseTree childNode; !nodeStack.isEmpty();)
    {
      final var parentNode = nodeStack.peekFirst();

      if (parentNode.isFirst())
        parentNode.parserRuleContext.enterRule(listener);

      if ((childNode = parentNode.getNextChild()) == null)
        nodeStack.pollFirst();
      else if (childNode instanceof ParserRuleContext)
        nodeStack.addFirst(new ParserRuleContextNode((ParserRuleContext)childNode));
    }
  }


  /**
   * Walks the parse tree and invokes only rule-specific exit methods on the listener.
   * <p>
   * This method performs a post-order depth-first traversal of the parse tree, calling exit methods on parser rules
   * after all their children have been processed. No enter methods, terminal node visits, or error node visits are
   * invoked.
   *
   * @param listener           the parse tree listener to receive exit callbacks, not {@code null}
   * @param parserRuleContext  the root context to start walking from, not {@code null}
   *
   * @since 0.2.0
   */
  @Contract(mutates = "param2")
  static void walkExitsOnlyIterative(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext)
  {
    final var nodeStack = new ArrayDeque<ParserRuleContextNode>();
    nodeStack.addFirst(new ParserRuleContextNode(parserRuleContext));

    for(ParseTree childNode; !nodeStack.isEmpty();)
    {
      final var parentNode = nodeStack.peekFirst();

      if ((childNode = parentNode.getNextChild()) == null)
      {
        parentNode.parserRuleContext.exitRule(listener);
        nodeStack.pollFirst();
      }
      else if (childNode instanceof ParserRuleContext)
        nodeStack.push(new ParserRuleContextNode((ParserRuleContext)childNode));
    }
  }


  /**
   * Walks the parse tree and invokes rule-specific enter and exit methods on the listener.
   * <p>
   * This method performs a depth-first traversal of the parse tree, calling both enter and exit methods on parser
   * rules. Enter methods are called in pre-order (before processing a rule's children) and exit methods are called
   * in post-order (after all children have been processed). Terminal and error node visits are not invoked.
   *
   * @param listener           the parse tree listener to receive enter and exit callbacks, not {@code null}
   * @param parserRuleContext  the root context to start walking from, not {@code null}
   *
   * @since 0.2.0
   */
  @Contract(mutates = "param2")
  static void walkEnterAndExitsOnlyIterative(@NotNull ParseTreeListener listener,
                                             @NotNull ParserRuleContext parserRuleContext)
  {
    final var nodeStack = new ArrayDeque<ParserRuleContextNode>();
    nodeStack.addFirst(new ParserRuleContextNode(parserRuleContext));

    for(ParseTree childNode; !nodeStack.isEmpty();)
    {
      final var parentNode = nodeStack.peekFirst();

      if (parentNode.isFirst())
        parentNode.parserRuleContext.enterRule(listener);

      if ((childNode = parentNode.getNextChild()) == null)
      {
        parentNode.parserRuleContext.exitRule(listener);
        nodeStack.pollFirst();
      }
      else if (childNode instanceof ParserRuleContext)
        nodeStack.addFirst(new ParserRuleContextNode((ParserRuleContext)childNode));
    }
  }


  /**
   * Walks the parse tree and invokes all listener methods for a complete traversal.
   * <p>
   * This method performs a full depth-first traversal of the parse tree, invoking all listener callbacks including
   * enter/exit methods for rules, terminal node visits, and error node visits. This provides the most comprehensive
   * tree traversal.
   *
   * @param listener           the parse tree listener to receive all callbacks, not {@code null}
   * @param parserRuleContext  the root context to start walking from, not {@code null}
   */
  @Contract(mutates = "param2")
  static void walkFullIterative(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
    new IterativeParseTreeWalker().walk(listener, parserRuleContext);
  }
}
