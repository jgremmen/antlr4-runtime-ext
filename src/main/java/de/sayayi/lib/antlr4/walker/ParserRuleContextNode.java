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
 * @author Jeroen Gremmen
 * @since 0.2.0
 */
final class ParserRuleContextNode
{
  final @NotNull ParserRuleContext parserRuleContext;
  private final int childCount;
  private int index;


  ParserRuleContextNode(@NotNull ParserRuleContext parserRuleContext)
  {
    this.parserRuleContext = parserRuleContext;

    final var childList = parserRuleContext.children;

    childCount = childList == null ? 0 : childList.size();
    index = 0;
  }


  @Contract(mutates = "this")
  ParseTree getNextChild() {
    return index < childCount ? parserRuleContext.children.get(index++) : null;
  }


  @Contract(pure = true)
  boolean isFirst() {
    return index == 0;
  }
}
