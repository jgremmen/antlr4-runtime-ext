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
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.sayayi.lib.antlr4.walker.ParseTreeWalker.*;


/**
 * Variants of depth-first-search walkers for a parser rule context.
 * <p>
 * This enum provides different walking strategies for traversing ANTLR4 parse trees. Each walker variant determines
 * which listener methods are invoked during tree traversal and uses heap-based iteration to avoid stack overflow
 * issues with deeply nested parse trees.
 * <p>
 * Choose the appropriate walker based on which listener callbacks you need during traversal:
 * <ul>
 *   <li>Use {@link #WALK_FULL_HEAP} for complete tree traversal with all callbacks</li>
 *   <li>Use {@link #WALK_ENTER_RULES_HEAP} when you only need rule enter callbacks</li>
 *   <li>Use {@link #WALK_EXIT_RULES_HEAP} when you only need rule exit callbacks</li>
 *   <li>
 *     Use {@link #WALK_ENTER_AND_EXIT_RULES_HEAP} when you need both enter and exit callbacks but not
 *     terminal/error nodes
 *   </li>
 * </ul>
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public enum Walker
{
  /**
   * Full tree walker that invokes all listener methods during traversal.
   * <p>
   * This walker visits every node in the parse tree and invokes all corresponding listener methods: enter/exit
   * methods for rules, terminal node visits, and error node visits. It uses a heap-based iterative approach to
   * prevent stack overflow with deeply nested trees.
   */
  WALK_FULL_HEAP {
    @Override
    public void walk(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
      walkFullIterative(listener, parserRuleContext);
    }
  },


  /**
   * Walk and invoke rule-specific enter methods only using the heap.
   * <p>
   * This walker never invokes the following methods:
   * <ul>
   *   <li>{@link ParseTreeListener#enterEveryRule(ParserRuleContext)}</li>
   *   <li>{@link ParseTreeListener#exitEveryRule(ParserRuleContext)}</li>
   *   <li>{@link ParseTreeListener#visitTerminal(TerminalNode)}</li>
   *   <li>{@link ParseTreeListener#visitErrorNode(ErrorNode)}</li>
   * </ul>
   *
   * @since 0.7.1
   */
  WALK_ENTER_RULES_HEAP {
    @Override
    public void walk(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
      walkEnterOnlyIterative(listener, parserRuleContext);
    }
  },


  /**
   * Walk and invoke rule-specific exit methods only using the heap.
   * <p>
   * This walker never invokes the following methods:
   * <ul>
   *   <li>{@link ParseTreeListener#enterEveryRule(ParserRuleContext)}</li>
   *   <li>{@link ParseTreeListener#exitEveryRule(ParserRuleContext)}</li>
   *   <li>{@link ParseTreeListener#visitTerminal(TerminalNode)}</li>
   *   <li>{@link ParseTreeListener#visitErrorNode(ErrorNode)}</li>
   * </ul>
   *
   * @since 0.2.0
   */
  WALK_EXIT_RULES_HEAP {
    @Override
    public void walk(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
      walkExitsOnlyIterative(listener, parserRuleContext);
    }
  },


  /**
   * Walk and invoke rule-specific enter and exit methods only using the heap.
   * <p>
   * This walker never invokes the following methods:
   * <ul>
   *   <li>{@link ParseTreeListener#enterEveryRule(ParserRuleContext)}</li>
   *   <li>{@link ParseTreeListener#exitEveryRule(ParserRuleContext)}</li>
   *   <li>{@link ParseTreeListener#visitTerminal(TerminalNode)}</li>
   *   <li>{@link ParseTreeListener#visitErrorNode(ErrorNode)}</li>
   * </ul>
   *
   * @since 0.2.0
   */
  WALK_ENTER_AND_EXIT_RULES_HEAP {
    @Override
    public void walk(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
      walkEnterAndExitsOnlyIterative(listener, parserRuleContext);
    }
  };


  /**
   * Walks the parse tree starting from the given parser rule context.
   * <p>
   * The walk strategy is determined by the specific walker variant. During traversal, appropriate listener methods
   * are invoked on the provided listener based on the nodes encountered and the walker's behavior.
   *
   * @param listener           the parse tree listener that receives callbacks during tree traversal, not {@code null}
   * @param parserRuleContext  the root context from which to start walking the parse tree, not {@code null}
   */
  @Contract(mutates = "param2")
  public abstract void walk(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext);
}
