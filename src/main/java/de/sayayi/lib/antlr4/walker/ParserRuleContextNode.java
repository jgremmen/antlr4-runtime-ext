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
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * Internal node wrapper used during iterative parse tree traversal.
 * <p>
 * This class encapsulates a parser rule context along with iteration state, allowing tree walkers to track which
 * children have been visited during heap-based traversal. Each instance represents a single node in the parse tree
 * being walked.
 * <p>
 * This class is package-private and used internally by {@link ParseTreeWalker}.
 *
 * @author Jeroen Gremmen
 * @since 0.2.0
 *
 * @see ParseTreeWalker
 */
final class ParserRuleContextNode
{
  final @NotNull ParserRuleContext parserRuleContext;
  private final int childCount;
  private int index;


  /**
   * Creates a new node wrapper for the given parser rule context.
   * <p>
   * Initializes the iteration state to begin traversing the context's children.
   *
   * @param parserRuleContext  the parser rule context to wrap, not {@code null}
   */
  ParserRuleContextNode(@NotNull ParserRuleContext parserRuleContext)
  {
    this.parserRuleContext = parserRuleContext;

    final var childList = parserRuleContext.children;

    childCount = childList == null ? 0 : childList.size();
    index = 0;
  }


  /**
   * Returns the next child node to visit in the traversal sequence.
   * <p>
   * Each call advances the internal position, moving to the next child. Once all children have been returned,
   * subsequent calls return {@code null}.
   *
   * @return  the next child parse tree node, or {@code null} if all children have been visited
   */
  @Contract(mutates = "this")
  ParseTree getNextChild() {
    return index < childCount ? parserRuleContext.children.get(index++) : null;
  }


  /**
   * Checks if the iteration is at the first child position.
   * <p>
   * This is useful for determining when to invoke enter callbacks during tree traversal.
   *
   * @return  {@code true} if no children have been visited yet, {@code false} otherwise
   */
  @Contract(pure = true)
  boolean isFirst() {
    return index == 0;
  }
}
