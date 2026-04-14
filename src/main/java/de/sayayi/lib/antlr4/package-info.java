/*
 * Copyright 2026 Jeroen Gremmen
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

/**
 * Extensions and utilities for the ANTLR4 runtime library.
 * <p>
 * This package provides an abstract framework for building ANTLR4-based parsers with integrated syntax error
 * handling, custom vocabularies, and optimized parse tree walking. It simplifies the typical boilerplate involved
 * in setting up lexers, parsers, error listeners, and tree walkers.
 * <p>
 * The main components of this package are:
 * <ul>
 *   <li>
 *     {@link de.sayayi.lib.antlr4.AbstractAntlr4Parser} - abstract base class that provides a framework for lexing,
 *     parsing, and walking parse trees with consistent error handling
 *   </li>
 *   <li>
 *     {@link de.sayayi.lib.antlr4.AbstractVocabulary} - convenience base class for creating custom ANTLR4
 *     vocabularies with literal and symbolic token names
 *   </li>
 *   <li>
 *     {@link de.sayayi.lib.antlr4.LocationToken} - lightweight token implementation carrying only position
 *     information, used for syntax error formatting
 *   </li>
 * </ul>
 * <p>
 * Related sub-packages:
 * <ul>
 *   <li>{@link de.sayayi.lib.antlr4.syntax} - syntax error reporting and formatting utilities</li>
 *   <li>{@link de.sayayi.lib.antlr4.walker} - optimized heap-based parse tree walkers</li>
 * </ul>
 */
package de.sayayi.lib.antlr4;
