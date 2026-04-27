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
 * Provides optimized tree walking utilities for ANTLR4 parse trees.
 * <p>
 * This package contains walker implementations that traverse ANTLR4 parse trees using heap-based iterative algorithms
 * instead of traditional recursion. This approach prevents stack overflow errors when processing deeply nested parse
 * trees, which is particularly important when parsing complex or large input files.
 * <p>
 * The main entry point is the {@link de.sayayi.lib.antlr4.walker.Walker} enum, which provides different walking
 * strategies:
 * <ul>
 *   <li><b>Full traversal</b> - visits all nodes and invokes all listener callbacks</li>
 *   <li><b>Enter-only traversal</b> - invokes only rule enter methods for top-down processing</li>
 *   <li><b>Exit-only traversal</b> - invokes only rule exit methods for bottom-up processing</li>
 *   <li><b>Enter/Exit traversal</b> - invokes rule enter and exit methods, skipping terminal nodes</li>
 * </ul>
 * <p>
 * Choose the appropriate walker variant based on your processing needs to optimize performance by avoiding
 * unnecessary callbacks.
 */
package de.sayayi.lib.antlr4.walker;
