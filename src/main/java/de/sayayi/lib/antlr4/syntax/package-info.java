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
 * Provides syntax error reporting and formatting utilities for ANTLR4 parsers.
 * <p>
 * This package contains classes for detecting, building, and formatting syntax errors into human-readable messages
 * with visual context. The formatted output includes source code snippets with line numbers and visual markers that
 * pinpoint the exact location of the error, making it easy for users to identify and fix problems in their input.
 * <p>
 * The main components of this package are:
 * <ul>
 *   <li>
 *     {@link de.sayayi.lib.antlr4.syntax.SyntaxErrorFormatter} - the core interface for formatting syntax errors
 *     into human-readable messages
 *   </li>
 *   <li>
 *     {@link de.sayayi.lib.antlr4.syntax.GenericSyntaxErrorFormatter} - a configurable implementation that produces
 *     formatted output with source code context and visual error markers
 *   </li>
 *   <li>
 *     {@link de.sayayi.lib.antlr4.syntax.SyntaxErrorBuilder} - a fluent builder interface for constructing and
 *     reporting syntax errors with detailed location information
 *   </li>
 *   <li>
 *     {@link de.sayayi.lib.antlr4.syntax.SyntaxErrorException} - an exception that encapsulates syntax error details
 *     including token locations and formatted error messages
 *   </li>
 * </ul>
 */
package de.sayayi.lib.antlr4.syntax;
