################################################################################
#                OVERVIEW          
################################################################################
This directory contains Mind compiler modules:

The organization of the Mind compiler sources is the following (maven 
coordinates are given between '<' and '>'. "o.o.m" stands for "org.ow2.mind"):

mind-compiler <o.o.m:mind-compiler> : the directory containing this file.
 |
 |- adl-parser <o.o.m:adl-parser> : Contains the parser of the ADL language of
 |                        the Mind programming model.
 |    
 |- adl-frontend <o.o.m:adl-frontend> : Front-end components for the ADL
 |                        language of the Mind programming model.
 |
 |- adl-backend <o.o.m:adl-backend> : Back-end components for generation of 
 |                        source code and compilation of ADL definitions
 |
 |- idl-parser <o.o.m:idl-parser> : Contains the parser of the IDL language of
 |                        the Mind programming model.
 |    
 |- idl-frontend <o.o.m:idl-frontend> : Front-end components for the IDL
 |                        language of the Mind programming model.
 |
 |- idl-backend <o.o.m:idl-backend> : Back-end components for generation of 
 |                        source code of IDL definitions
 |
 |- cpl-preproc <o.o.m:cpl-preproc> : The preprocessor of the CPL language of
 |                        the Mind programming model.
 |
 |- common-frontend <o.o.m:common-frontend> : Contains front-end components 
 |                        shared by ADL and IDL frontends.
 |
 |- common-backend <o.o.m:common-backend> : Contains back-end components 
 |                        shared by ADL and IDL frontends.
 |
 |- mindc <o.o.m:mindc> : Integration module. Contains the main class and the
 |                        'mindc' script. Build binary distributions.

################################################################################
#                 BUILD NOTES
################################################################################

To build Mind compiler and install produced artifact in your local maven 
repository perform the following command:

$ mvn install

Binary distributions can then be found in the 'mindc/target' sub directory.

To build and install the Mind compiler in a local directory perform the 
following command:

$ mvn install -Dlocal-install=<installation directory>

################################################################################

To deploy new artifacts on the OW2 maven repository perform the following 
command:

$ mvn clean deploy


################################################################################

To generate the Javadoc, perform the following command:

$ mvn javadoc:javadoc

The Javadoc can then be found in the <module-dir>/target/site/apidocs directory.


################################################################################

To generate the User and Developer Guides, perform the following command in the
mind-compiler directory :

$ mvn -N docbkx:generate-html docbkx:generate-pdf

The PDF and HTML documents can then be found in the <module-dir>/target/site
directory.

When editing docbooks, it is recommended to "touch" top-level docbook files 
before executing the maven-docbkx-plugin. Indeed, docbook sources are split in
various XML files which is not correctly handle by the plugin (if the top-level
file has not been updated, the plugin will not re-compile the doc). So when 
editing the documentation the command should be :

$ touch src/docbkx/*.xml; mvn  -N docbkx:generate-html docbkx:generate-pdf


################################################################################

To release a new stable version perform the two following commands:

$ mvn release:prepare -Dow2.username=<your OW2 login>
$ mvn release:perform -Dow2.username=<your OW2 login>

Where "<your OW2 login>" is your login on the OW2 forge. This is necessary only 
if your local login does not match the OW2 one (note that this property can also
be specified in your settings.xml file).
These commands will update version information in POMs, tag the repository, 
build and deploy new artifacts.

The two previous commands can be shortened into the following one:

$ mvn release:prepare release:perform -Dow2.username=<your OW2 login>

