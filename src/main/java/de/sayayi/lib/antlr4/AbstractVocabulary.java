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

import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Vocabulary;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.stream.Collectors.joining;


/**
 * Convenience class for creating a custom vocabulary.
 *
 * @author Jeroen Gremmen
 * @since 0.1.0
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractVocabulary implements Vocabulary
{
  private final SortedMap<Integer,Name> vocabulary = new TreeMap<>();


  protected AbstractVocabulary()
  {
    add(Recognizer.EOF, "<EOF>", "EOF");
    addTokens();
  }


  protected abstract void addTokens();


  @Contract(mutates = "this")
  protected void add(int tokenType, @NotNull String literal, @NotNull String symbol) {
    vocabulary.put(tokenType, new Name(literal, symbol));
  }


  @Override
  public int getMaxTokenType() {
    return vocabulary.lastKey();
  }


  @Override
  public String getLiteralName(int tokenType) {
    return vocabulary.containsKey(tokenType) ? vocabulary.get(tokenType).literal : null;
  }


  @Override
  public String getSymbolicName(int tokenType) {
    return vocabulary.containsKey(tokenType) ? vocabulary.get(tokenType).symbol : null;
  }


  @Override
  public String getDisplayName(int tokenType) {
    return !vocabulary.containsKey(tokenType) ? Integer.toString(tokenType) : vocabulary.get(tokenType).literal;
  }


  @Override
  public String toString()
  {
    return vocabulary.entrySet()
        .stream()
        .map(this::toString_entry)
        .collect(joining(",", "Vocabulary[", "]"));
  }


  private @NotNull String toString_entry(@NotNull Entry<Integer,Name> entry)
  {
    final Name name = entry.getValue();
    return "{token=" + entry.getKey() + ",literal=" + name.literal + ",symbol=" + name.symbol + '}';
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
