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

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.stream.Collectors.joining;
import static org.antlr.v4.runtime.Token.EOF;


/**
 * Convenience class for creating custom vocabularies.
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
 * <p>
 * By default, literal {@code <EOF>} is assigned to the {@link Token#EOF EOF} token but can be overwritten using
 * {@link #add(int, String, String)}.
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
public abstract class AbstractVocabulary implements Vocabulary
{
  private final SortedMap<Integer,Name> vocabulary = new TreeMap<>();
  private boolean sealed;


  /**
   * Constructs a new vocabulary.
   * <p>
   * The constructor registers the default {@code <EOF>} literal for the {@link Token#EOF EOF} token and then
   * invokes {@link #addTokens()} to allow subclasses to populate the vocabulary. After construction, the vocabulary
   * is sealed and cannot be modified.
   */
  protected AbstractVocabulary()
  {
    sealed = false;

    add(EOF, "<EOF>", "EOF");
    addTokens();

    sealed = true;
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
  protected void add(int tokenType, @NotNull String literal, @NotNull String symbol)
  {
    if (sealed)
      throw new IllegalStateException("vocabulary cannot be modified");

    vocabulary.put(tokenType, new Name(literal, symbol));
  }


  /**
   * {@inheritDoc}
   *
   * @return  the maximum token type value registered in this vocabulary
   */
  @Override
  public int getMaxTokenType() {
    return vocabulary.lastKey();
  }


  /**
   * {@inheritDoc}
   *
   * @return  the literal name for the token type, or {@code null} if not found
   */
  @Override
  public String getLiteralName(int tokenType)
  {
    var name = vocabulary.get(tokenType);
    return name == null ? null : name.literal();
  }


  /**
   * {@inheritDoc}
   *
   * @return  the symbolic name for the token type, or {@code null} if not found
   */
  @Override
  public String getSymbolicName(int tokenType)
  {
    var name = vocabulary.get(tokenType);
    return name == null ? null : name.symbol();
  }


  /**
   * {@inheritDoc}
   * <p>
   * If the token type is not found in the vocabulary, the numeric token type is returned as a string.
   *
   * @return  the display name for the token type, never {@code null}
   */
  @Override
  public String getDisplayName(int tokenType)
  {
    var name = vocabulary.get(tokenType);
    return name == null ? Integer.toString(tokenType) : name.literal();
  }


  /**
   * Returns a string representation of this vocabulary listing all registered tokens.
   *
   * @return  a string containing all token entries with their type and literal name
   */
  @Override
  public String toString()
  {
    return vocabulary
        .entrySet()
        .stream()
        .map(entry -> {
          var name = entry.getValue();
          return "{token=" + entry.getKey() + ",literal=" + name.literal() + '}';
        })
        .collect(joining(",", "Vocabulary[", "]"));
  }




  /**
   * Holds the literal and symbolic name for a vocabulary token.
   *
   * @param literal  the literal representation of the token, not {@code null}
   * @param symbol   the symbolic representation of the token, not {@code null}
   */
  private record Name(@NotNull String literal, @NotNull String symbol) {
  }
}
