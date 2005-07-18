/**
 * Schema to Java compiler.
 *
 * <p>
 * This module contains the code that implements the schema compiler 'XJC'.
 *
 *
 * <h2>Overview</h2>
 * <p>
 * XJC consists of the following major components.
 * <dl>
 *  <dt>{@link com.sun.tools.xjc.model Model}
 *  <dd>
 *   Model represents the 'blueprint' of the code to be generated.
 *   Model talks in terms of higher level constructs like 'class' and 'property'
 *   without getting too much into the details of the Java source code.
 *
 *  <dt>{@link com.sun.tools.xjc.reader Schema reader}
 *  <dd>
 *   Schema readers read XML Schema documents (or DTD, RELAX NG, ...)
 *   and builds a model.
 *
 *  <dt>{@link com.sun.tools.xjc.generator Code generator}
 *  <dd>
 *   Code generators use a model as an input and builds Java code AST
 *   into CodeModel. It also produces an {@link Outline} which captures
 *   this work.
 *
 *  <dt>{@link Outline Outline}
 *  <dd>
 *   Outline can be thought as a series of links between a model
 *   and CodeModel.
 * </dl>
 */
package com.sun.tools.xjc;

import com.sun.tools.xjc.outline.Outline;
