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

import lombok.NoArgsConstructor;
import lombok.val;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.IterativeParseTreeWalker;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

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


  /**
   * @since 0.2.0
   */
  @Contract(mutates = "param2")
  static void walkExitsOnlyIterative(@NotNull ParseTreeListener listener,
                                     @NotNull ParserRuleContext parserRuleContext)
  {
    val nodeStack = new ArrayDeque<ParserRuleContextNode>();
    nodeStack.addFirst(new ParserRuleContextNode(parserRuleContext));

    for(ParseTree childNode; !nodeStack.isEmpty();)
    {
      val parentNode = nodeStack.peekFirst();

      if ((childNode = parentNode.getNextChild()) == null)
      {
        parentNode.parserRuleContext.exitRule(listener);
        nodeStack.pollFirst();
      }
      else if (childNode instanceof ParserRuleContext)
        nodeStack.push(new ParserRuleContextNode((ParserRuleContext)childNode));
    }
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


  /**
   * @since 0.2.0
   */
  @Contract(mutates = "param2")
  static void walkEnterAndExitsOnlyIterative(@NotNull ParseTreeListener listener,
                                             @NotNull ParserRuleContext parserRuleContext)
  {
    val nodeStack = new ArrayDeque<ParserRuleContextNode>();
    nodeStack.addFirst(new ParserRuleContextNode(parserRuleContext));

    parserRuleContext.enterRule(listener);

    for(ParseTree childNode; !nodeStack.isEmpty();)
    {
      val parentNode = nodeStack.peekFirst();

      if ((childNode = parentNode.getNextChild()) == null)
      {
        parentNode.parserRuleContext.exitRule(listener);
        nodeStack.pollFirst();
      }
      else if (childNode instanceof ParserRuleContext)
      {
        val childNodeContext = (ParserRuleContext)childNode;
        childNodeContext.enterRule(listener);

        nodeStack.push(new ParserRuleContextNode(childNodeContext));
      }
    }
  }


  @Contract(mutates = "param2")
  static void walkFullRecursive(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
    org.antlr.v4.runtime.tree.ParseTreeWalker.DEFAULT.walk(listener, parserRuleContext);
  }


  @Contract(mutates = "param2")
  static void walkFullIterative(@NotNull ParseTreeListener listener, @NotNull ParserRuleContext parserRuleContext) {
    FULL_HEAP_WALKER.walk(listener, parserRuleContext);
  }
}