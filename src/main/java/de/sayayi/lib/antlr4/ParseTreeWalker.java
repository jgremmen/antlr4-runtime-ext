package de.sayayi.lib.antlr4;

import lombok.NoArgsConstructor;
import lombok.val;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;


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