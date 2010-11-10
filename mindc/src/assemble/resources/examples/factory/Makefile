# If the MIND_ROOT variable is defined, look for 'mindc' in this directory
# otherwise, expect that 'mindc' is in the PATH.
ifdef MIND_ROOT
  # If MIND_ROOT contains '\', replace them by '/' (for Windows)
  MINDC = $(subst \,/, $(MIND_ROOT)/bin/mindc)
else 
  MINDC = mindc
endif

all: factoryExample

# Compile "factory.FactoryExample" application component wrapped in the 
# "boot.GenericApplication" ADL template.
# Notes:
#  - It is highly recommended to specify an executable name when using a 
#    template as top-level ADL (i.e. add ":<exec name>" on the command line
#    after the name of the top-level ADL). Otherwise the executable name will be
#    the name of the ADL template followed by an arbitrary hash-code suffix 
#    (boot/GenericLCCApplication_tmpl_71ee0182 in this example)
#
#  - The argument boot.GenericApplication<factory.FactoryExample>:factoryExample
#    must be surrounded by double quote in the command-line to avoid shell 
#    interpretation issue of '<' and '>'.
factoryExample: build
	$(MINDC) -src-path=src -o=build "boot.GenericApplication<factory.FactoryExample>:factoryExample"

build:
	mkdir -p build

clean:
	rm -Rf build

.PHONY: all factoryExample clean
