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
package de.sayayi.lib.antlr4;

import lombok.NoArgsConstructor;
import lombok.val;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.IterativeParseTreeWalker;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import static java.util.Collections.emptyIterator;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@NoArgsConstructor(access = PRIVATE)
final class ParseTreeWalker
{
  private static final IterativeParseTreeWalker FULL_HEAP_WALKER = new IterativeParseTreeWalker();


  @Contract(mutates = "param2")
  static void walkExitsOnlyRecursive(@NotNull ParseTreeListener listener,
                                     @NotNull ParserRuleContext parserRuleContext)
  {
    val children = parserRuleContext.children;
    if (children != null)
    {
      for(val parseTreeChild: children)
        if (parseTreeChild instanceof ParserRuleContext)
          walkExitsOnlyRecursive(listener, (ParserRuleContext)parseTreeChild);
    }

    parserRuleContext.exitRule(listener);
  }


  @Contract(mutates = "param2")
  static void walkEnterAndExitsOnlyRecursive(@NotNull ParseTreeListener listener,
                                             @NotNull ParserRuleContext parserRuleContext)
  {
    parserRuleContext.enterRule(listener);

    val children = parserRuleContext.children;
    if (children != null)
    {
      for(val parseTreeChild: children)
        if (parseTreeChild instanceof ParserRuleContext)
          walkEnterAndExitsOnlyRecursive(listener, (ParserRuleContext)parseTreeChild);
    }

    parserRuleContext.exitRule(listener);
  }


  @Contract(mutates = "param2")
  static void walkFullRecursive(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
    org.antlr.v4.runtime.tree.ParseTreeWalker.DEFAULT.walk(listener, parserRuleContext);
  }


  @Contract(mutates = "param2")
  static void walkFullHeap(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
    FULL_HEAP_WALKER.walk(listener, parserRuleContext);
  }


  private static @NotNull Iterator<ParseTree> fromParseRuleContext(@NotNull ParserRuleContext parserRuleContext)
  {
    val children = parserRuleContext.children;
    return children == null ? emptyIterator() : children.iterator();
  }
}