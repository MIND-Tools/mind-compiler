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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.ow2.mind.preproc.parser.CPLLexer;
import org.ow2.mind.preproc.parser.CPLParser;

public class MIND_preproc {

  // Map<String, CmdOption> of options
  protected Map<String, CmdOption> options;

  // name of the MIND cpl file to pre-proceed.
  protected String                 cplFileName;
  // path to the MIND cpl file to pre-proceed.
  protected String                 cplFilePath;

  private final CmdOption          propertiesOpt = new CmdPropOption(
                                                     new String[]{"properties",
      "prop"                                         },
                                                     "a list of property files containing additional options");

  private final CmdOption          outDirOpt     = new CmdOption(new String[]{
      "out-path", "out-dir", "o"                 },
                                                     "the path where generated files will be put");
// , ".");

  private final CmdOption          stgOpt        = new CmdOption(
                                                     new String[]{"stg",
      "string-template-groupe"                       },
                                                     "the path to the StringTemplate Group (.stg) to use",
                                                     null);

  private final CmdOption          singletonMode = new CmdOption(
                                                     new String[]{"sm",
      "singleton", "singleton-mode"                  },
                                                     "singleton mode (true or false)",
                                                     "false");

  private CmdOption[]              allCmdOptions;

  protected MIND_preproc() {
    options = new HashMap<String, CmdOption>();
    this.setOptions(new CmdOption[]{propertiesOpt, outDirOpt, stgOpt,
        singletonMode});
    this.addOptions(this.allCmdOptions);
  }

  public static void main(final String args[]) throws Exception {
    StringTemplateGroup templates = null;
    final MIND_preproc mind_preproc = new MIND_preproc();
    mind_preproc.parseArgs(args);

    PrintStream p;

    if (mind_preproc.outDirOpt.value != null) {
      FileOutputStream out;
      out = new FileOutputStream(mind_preproc.outDirOpt.value + "_mpp_"
          + mind_preproc.cplFileName);
      p = new PrintStream(out);
    } else {
      p = System.out;
    }

    if (mind_preproc.stgOpt.value != null) {
      final FileReader groupFileMind_CPL = new FileReader(
          mind_preproc.stgOpt.value);
      templates = new StringTemplateGroup(groupFileMind_CPL);
      groupFileMind_CPL.close();
    }

    final CPLLexer lex = new CPLLexer(new ANTLRFileStream(
        mind_preproc.cplFilePath + mind_preproc.cplFileName));
    lex.setOutPutStream(p);
    final CommonTokenStream tokens = new CommonTokenStream(lex);

    final CPLParser mpp = new CPLParser(tokens);
    mpp.setOutputStream(p);
    mpp.setSingletonMode(mind_preproc.singletonMode.getValue().equals("true"));
    if (templates != null) {
      mpp.setTemplateLib(templates);
    }
    try {
      mpp.parseFile();
    } catch (final RecognitionException e) {
      e.printStackTrace();
    }
  }

  private void addOptions(final CmdOption[] options) {
    for (final CmdOption option : options) {
      for (final String option2 : option.options) {
        if (this.options.put(option2, option) != null) {
          throw new InternalError("Duplicated option '" + option2 + "'");
        }
      }
    }
  }

  private void setOptions(final CmdOption[] options) {
    allCmdOptions = new CmdOption[options.length];
    System.arraycopy(options, 0, allCmdOptions, 0, options.length);
  }

  private void parseArgs(final String[] args) {
    for (final String arg : args) {
      if (arg.startsWith("-")) {
        final int index = arg.indexOf('=');
        if (index < 2) {
          parseError(arg);
        }
        final String optionName = arg.substring(1, index);
        final String optionValue = arg.substring(index + 1);

        // validate option
        final CmdOption cmdOption = options.get(optionName);
        if (cmdOption == null) parseError(arg);
        cmdOption.setValue(optionValue);
      } else {
        if (cplFileName == null) {
          cplFileName = arg.substring(arg.lastIndexOf("/") + 1);
          cplFilePath = arg.substring(0, arg.lastIndexOf("/") + 1);
        } else
          parseError(arg);
      }
    }
    readProperyFiles();
    // archList = getArchs();
    // // readArchProperties();
    // prepareOutDir();
    // prepareSourceClassLoader();
  }

  private void readProperyFiles() {
    final String propertyList = propertiesOpt.getValue();
    if (propertyList == null) return;
    final List<String> l = parsePathList(propertyList);
    final Iterator<String> iterator = l.iterator();
    while (iterator.hasNext()) {
      final String path = iterator.next();
      final Properties p = new Properties();
      loadProperties(p, new File(path));
      final Iterator propIterator = p.entrySet().iterator();
      while (propIterator.hasNext()) {
        final Map.Entry propEntry = (Map.Entry) propIterator.next();
        final CmdOption cmdOption = options.get(propEntry.getKey());
        if (cmdOption == null) {
          continue;
        }
        cmdOption.setValue((String) propEntry.getValue());
      }
    }
  }

  private List<String> parsePathList(String paths) {
    final List<String> l = new ArrayList<String>();
    int index = paths.indexOf(File.pathSeparatorChar);
    while (index != -1) {
      l.add(paths.substring(0, index));
      paths = paths.substring(index + 1);
      index = paths.indexOf(File.pathSeparatorChar);
    }
    l.add(paths);
    return l;
  }

  private void loadProperties(final Properties p, final File f) {
    try {
      p.load(new FileInputStream(f));
    } catch (final FileNotFoundException e) {
      error("Can't find property file '" + f.getAbsolutePath() + "'", 2);
    } catch (final IOException e) {
      error("Can't read property file '" + f.getAbsolutePath() + "'. "
          + e.getMessage(), 3);
    }
  }

  private void error(final String message, final int exitVal) {
    // if (exitOnFailure) {
    System.err.println(message);
    printFullUsage(System.err);
    System.exit(exitVal);
    // } else {
    // throw new Error(message);
    // }
  }

  private void parseError(final String arg) {
    error("Unknown option '" + arg + "'", 1);
  }

  protected void printUsage(final PrintStream ps) {
    ps.println("Usage: MIND_preproc [OPTIONS] <file>");
    ps
        .println("  where <file> is the name of the MIND CPL file component to be pre-proceed,");
  }

  private void printFullUsage(final PrintStream ps) {
    printUsage(ps);
    ps.println();
    ps
        .println("Available options are (from command line, options must be prefix with '-') :");
    int maxCol = 0;

    for (final CmdOption opt : allCmdOptions) {
      int col = 2 + opt.options[0].length();
      if (opt.options.length > 1) {
        col += opt.options[1].length() + 2;
        if (opt.options.length > 2)
          for (int j = 2; j < opt.options.length; j++) {
            col += opt.options[j].length() + 2;
          }
        else
          col += 2;
      }
      if (col > maxCol) maxCol = col;
    }
    for (final CmdOption opt : allCmdOptions) {
      final StringBuffer sb = new StringBuffer("  ");
      sb.append(opt.options[0]);
      if (opt.options.length > 1) {
        sb.append(" (");
        for (int j = 1; j < opt.options.length; j++) {
          sb.append(opt.options[j]);
          if (j < opt.options.length - 1) sb.append(", ");
        }
        sb.append(") ");
      }
      while (sb.length() < maxCol)
        sb.append(' ');
      sb.append(": ").append(opt.info);
      ps.println(sb);
    }
  }

  private class CmdOption {
    protected final String[] options;
    protected final String   info;
    protected final String   defaulValue;
    protected String         value;

    protected CmdOption(final String[] options, final String info,
        final String defaulValue) {
      this.options = options;
      this.info = info;
      this.defaulValue = defaulValue;
    }

    protected CmdOption(final String[] options, final String info) {
      this(options, info, null);
    }

    protected CmdOption(final String option, final String info) {
      this(new String[]{option}, info, null);
    }

    protected final String getValue() {
      return (value == null) ? defaulValue : value;
    }

    protected void setValue(final String value) {
      this.value = value;
    }

    // protected void setCommadLineValue(final String value) {
    // this.value = value;
    // }
    //
    // protected void setPropertyValue(final String value) {
    // this.value = value;
    // }

    // protected void setArchPropertyValue(final String value) {
    // if (this.value == null) this.value = value;
    // }
  }

  protected class CmdPropOption extends CmdOption {

    protected CmdPropOption(final String option, final String info) {
      super(option, info);
    }

    protected CmdPropOption(final String[] options, final String info,
        final String initialValue) {
      super(options, info, initialValue);
    }

    protected CmdPropOption(final String[] options, final String info) {
      super(options, info);
    }

    /** Append new value at end. */
    @Override
    protected void setValue(final String value) {
      this.value = (this.value == null) ? value : this.value + value;
    }

    /** Append new value at end. */
    protected void setPropertyValue(final String value) {
      error("Can't specify property files inside property file", 20);
    }

    // /** Append new value at begining. */
    // protected void setArchPropertyValue(final String value) {
    // error("Can't specify property files inside arch file", 20);
    // }
  }

}