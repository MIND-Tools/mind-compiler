/**
 * Copyright (C) 2010 STMicroelectronics
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.idl.ast;

public interface BinaryOperation extends ConstantExpression {

  String LOGICAL_OR  = "||";
  String LOGICAL_AND = "&&";
  String OR          = "|";
  String XOR         = "^";
  String AND         = "&";
  String LEFT_SHIFT  = "<<";
  String RIGHT_SHIFT = ">>";
  String ADD         = "+";
  String SUB         = "-";
  String MULL        = "*";
  String DIV         = "/";
  String MOD         = "%";

  String getOperation();

  void setOperation(String operation);

  void addConstantExpression(ConstantExpression node);

  void removeConstantExpression(ConstantExpression node);

  ConstantExpression[] getConstantExpressions();

}
