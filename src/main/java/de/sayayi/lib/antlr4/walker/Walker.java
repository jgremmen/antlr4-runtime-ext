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
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public enum Walker
{
  /**
   * Walk and invoke all rule-related methods using recursion.
   */
  WALK_FULL_RECURSIVE {
    @Override
    public void walk(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
      walkFullRecursive(listener, parserRuleContext);
    }
  },


  /**
   * Walk and invoke all rule-related methods using the heap.
   */
  WALK_FULL_HEAP {
    @Override
    public void walk(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
      walkFullIterative(listener, parserRuleContext);
    }
  },


  /**
   * Walk and invoke rule-specific exit methods only.
   * <p>
   * This walker never invokes the following methods:
   * <ul>
   *   <li>{@link ParseTreeListener#enterEveryRule(ParserRuleContext)}</li>
   *   <li>{@link ParseTreeListener#exitEveryRule(ParserRuleContext)}</li>
   *   <li>{@link ParseTreeListener#visitTerminal(TerminalNode)}</li>
   *   <li>{@link ParseTreeListener#visitErrorNode(ErrorNode)}</li>
   * </ul>
   */
  WALK_EXIT_RULES_RECURSIVE {
    @Override
    public void walk(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
      walkExitsOnlyRecursive(listener, parserRuleContext);
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
   * Walk and invoke rule-specific enter and exit methods only.
   * <p>
   * This walker never invokes the following methods:
   * <ul>
   *   <li>{@link ParseTreeListener#enterEveryRule(ParserRuleContext)}</li>
   *   <li>{@link ParseTreeListener#exitEveryRule(ParserRuleContext)}</li>
   *   <li>{@link ParseTreeListener#visitTerminal(TerminalNode)}</li>
   *   <li>{@link ParseTreeListener#visitErrorNode(ErrorNode)}</li>
   * </ul>
   */
  WALK_ENTER_AND_EXIT_RULES_RECURSIVE {
    @Override
    public void walk(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
      walkEnterAndExitsOnlyRecursive(listener, parserRuleContext);
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


  @Contract(mutates = "param2")
  public abstract void walk(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext);
}
