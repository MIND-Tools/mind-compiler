Mind compiler ${project.version}

Installing Mind
==================

  The following instructions show how to install Mind:

  1) Unpack the archive where you would like to store the binaries, eg:

# tar zxvf mind-${project.version}-bin.tar.gz

  2) A directory called "mind-${project.version}" will be created.

  3) Add the bin sub-directory to your PATH, eg:
  
# export PATH=/usr/local/mind-${project.version}/bin:$PATH

  4) Make sure JAVA_HOME is set to the location of your JRE or JDK

  5) Run "mindc" to verify that it is correctly installed.

Running the MindC compiler
=============================

  The mindc compiler compiles a given architecture definition file.
  It is used with the following command-line arguments:

# mindc [OPTIONS] (<definition>[:<execname>])+

  where <definition> is the name of the component to be compiled, 
  and <execname> is the name of the output file to be created.

Available options are :
  -h, --help                      Print this help and exit
  -S=<path list>, --src-path      the search path of ADL,IDL and implementation 
                                  files (list of path separated by ';'). This 
                                  option may be specified several times.
  -o=<output path>, --out-path    the path where generated files will be put 
                                  (default is '.')
  -t=<name>, --target-descriptor  Specify the target descriptor
  --compiler-command=<path>       the command of the C compiler (default is 
                                  'gcc')
  -c=<flags>, --c-flags           the c-flags compiler directives. This option
                                  may be specified several times.
  -I=<path list>, --inc-path      the list of path to be added in compiler 
                                  include paths. This option may be specified 
                                  several times.
  --linker-command=<path>         the command of the linker (default is 'gcc')
  -l=<flags>, --ld-flags          the ld-flags compiler directives. This option 
                                  may be specified several times.
  -L=<path list>, --ld-path       the list of path to be added to linker library
                                  search path. This option may be specified 
                                  several times.
  -T=<path>, --linker-script      linker script to use (given path is resolved 
                                  in source path)
  -j=<number>, --jobs             The number of concurrent compilation jobs 
                                  (default is '1')
  -e                              Print error stack traces
  --check-adl                     Only check input ADL(s), do not compile
  -d, --def2c                     Only generate source code of the given 
                                  definitions
  -D, --def2o                     Generate and compile source code of the given
                                  definitions, do not link an executable 
                                  application
  -F, --force                     Force the regeneration and the recompilation 
                                  of every output files
  -K, --keep                      Keep temporary output files in default output
                                  directory
  -B, --no-bin                    Do not generate binary ADL/IDL ('.def', 
                                  '.itfdef' and '.idtdef' files).

Setting the verbosity level of the mindc compiler
====================================================

  Use the MIND_OPTS environment variable to specify verbosity level. 

  For instance 

# set MIND_OPTS=-Ddefault.console.level=FINE -Ddefault.file.level=FINER

  specifies the FINE level for console messages and FINER for messages dumped in 
  log file.

Addition of extension modules to the mindc compiler
========================================================

  Extensions JARs can be either copied in the 'ext' folder or added to the 
  MIND_CLASSPATH environment variable

# export MIND_CLASSPATH="MyModule1.jar:MyModule2.jar"
  
 
