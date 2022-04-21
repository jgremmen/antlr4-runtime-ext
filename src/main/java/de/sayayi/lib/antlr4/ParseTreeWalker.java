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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@NoArgsConstructor(access = PRIVATE)
final class ParseTreeWalker
{
  @Contract(mutates = "param2")
  static void walkExitsOnly(@NotNull ParseTreeListener listener, @NotNull ParseTree parseTree)
  {
    if (parseTree instanceof ParserRuleContext)
    {
      val children = ((ParserRuleContext)parseTree).children;
      if (children != null)
        for(val parseTreeChild: children)
          walkExitsOnly(listener, parseTreeChild);

      ((ParserRuleContext)parseTree).exitRule(listener);
    }
  }


  @Contract(mutates = "param2")
  static void walkEnterAndExitsOnly(@NotNull ParseTreeListener listener, @NotNull ParseTree parseTree)
  {
    if (parseTree instanceof ParserRuleContext)
    {
      ((ParserRuleContext)parseTree).enterRule(listener);

      val children = ((ParserRuleContext)parseTree).children;
      if (children != null)
        for(val parseTreeChild: children)
          walkEnterAndExitsOnly(listener, parseTreeChild);

      ((ParserRuleContext)parseTree).exitRule(listener);
    }
  }
}