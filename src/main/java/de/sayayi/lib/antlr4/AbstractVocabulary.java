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

import org.antlr.v4.runtime.Vocabulary;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.stream.Collectors.joining;
import static org.antlr.v4.runtime.Recognizer.EOF;


/**
 * Convenience class for creating a custom vocabulary.
 * <p>
 * Example:
 * <pre>
 *   public MyVocabulary extends AbstractVocabulary
 *   {
 *     &#x40;Override
 *     protected addTokens()
 *     {
 *       add(1, "'while'", "WHILE");
 *       add(2, "&lt;number&gt;", "NUMBER");
 *       add(3, "'-'", "DASH");
 *       ...
 *     }
 *   }
 * </pre>
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public abstract class AbstractVocabulary implements Vocabulary
{
  private final SortedMap<Integer,Name> vocabulary = new TreeMap<>();


  protected AbstractVocabulary()
  {
    add(EOF, "<EOF>", "EOF");
    addTokens();
  }


  /**
   * This method is invoked by the constructor and is meant for implementing classes to
   * {@link #add(int, String, String) add} tokens to the vocabulary.
   */
  protected abstract void addTokens();


  /**
   * Adds a token to the vocabulary. If a token with the same {@code tokenType} already exists,
   * it will be overwritten.
   *
   * @param tokenType  token type; this is the number uniquely identifying a lexer or parser token
   * @param literal    literal representation of the token, not {@code null}
   * @param symbol     symbolic representation of the token, not {@code null}
   */
  @Contract(mutates = "this")
  protected void add(int tokenType, @NotNull String literal, @NotNull String symbol) {
    vocabulary.put(tokenType, new Name(literal, symbol));
  }


  @Override
  public int getMaxTokenType() {
    return vocabulary.lastKey();
  }


  @Override
  public String getLiteralName(int tokenType)
  {
    var name = vocabulary.get(tokenType);
    return name == null ? null : name.literal;
  }


  @Override
  public String getSymbolicName(int tokenType)
  {
    var name = vocabulary.get(tokenType);
    return name == null ? null : name.symbol;
  }


  @Override
  public String getDisplayName(int tokenType)
  {
    var name = vocabulary.get(tokenType);
    return name == null ? Integer.toString(tokenType) : name.literal;
  }


  @Override
  public String toString()
  {
    return vocabulary
        .entrySet()
        .stream()
        .map(entry -> {
          var name = entry.getValue();
          return "{token=" + entry.getKey() + ",literal=" + name.literal + ",symbol=" + name.symbol + '}';
        })
        .collect(joining(",", "Vocabulary[", "]"));
  }




  private static final class Name
  {
    final String literal;
    final String symbol;


    private Name(@NotNull String literal, @NotNull String symbol)
    {
      this.literal = literal;
      this.symbol = symbol;
    }
  }
}
