/**
 * Copyright (C) 2009 France Telecom
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
 * Authors: Olivier Lobry
 * Contributors: Matthieu Leclercq
 */

package org.ow2.mind.preproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.ow2.mind.preproc.parser.CPLLexer;
import org.ow2.mind.preproc.parser.CPLParser;

public class PreprocFilesOnCompilerCommand {

  private static String preproc(final File file, final List<String> command)
      throws Exception {
    Process child;
    String tempFileName = null;
    command.add("-c");
    command.add("-E");
    File tmpFile = null;
    final String orgName = file.getName();
    tmpFile = File.createTempFile("cgcparse_preproc_"
        + orgName.subSequence(0, orgName.length() - 2), ".c");
    tempFileName = tmpFile.getAbsolutePath();
    command.add(file.getAbsolutePath());
    // .replace(" ", "\\ ")
    command.add("-o");
    command.add(tempFileName);

    child = Runtime.getRuntime().exec(
        command.toArray(new String[command.size()]), null);
    if (child.waitFor() != 0)
      throw new Exception("Error while compiling files \""
          + file.getAbsolutePath() + "...\"");
    child.destroy();
    return tempFileName;
  }

  public static void main(final String[] args) throws Exception {
    final List<String> preprocCommand = new ArrayList<String>();
    final Map<File, File> fileMap = new HashMap<File, File>();
    for (final String arg : args) {
      if (arg.endsWith(".c")) {
        File resFile = null;
        final File orgFile = new File(arg);
        try {
          final String orgName = orgFile.getName();
          resFile = File.createTempFile("mindparse_res_"
              + orgName.subSequence(0, orgName.length() - 2), ".c");
        } catch (final IOException e1) {
          throw new Exception("cannot create res file");
        }
        fileMap.put(orgFile, resFile);
      } else if (arg.startsWith("-") || (arg == args[0])) {
        if (!arg.startsWith("-M") && !arg.startsWith("-o")) {
          preprocCommand.add(arg);
        }
      }
    }

    for (final Map.Entry<File, File> entry : fileMap.entrySet()) {
      final String tmp = preproc(entry.getKey(), preprocCommand);

      final CPLLexer lex = new CPLLexer(new ANTLRFileStream(tmp));

      final CommonTokenStream tokens = new CommonTokenStream(lex);

      final CPLParser mpp = new CPLParser(tokens);

      mpp.parseFile();
    }

  }

}
