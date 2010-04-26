# If the MIND_ROOT variable is defined, look for 'mindc' in this directory
# otherwise, expect that 'mindc' is in the PATH.
ifdef MIND_ROOT
  # If MIND_ROOT contains '\', replace them by '/' (for Windows)
  MINDC = $(subst \,/, $(MIND_ROOT)/bin/mindc)
else 
  MINDC = mindc
endif

all: simpleLCC

# Compile "lcc.SimpleLCC" application component wrapped in the 
# "boot.GenericLCCApplication" ADL. 
# Notes:
#  - It is highly recommended to specify an executable name when using a 
#    template as top-level ADL. Otherwise the executable name will be the name of
#    the template ADL followed by an arbitrary hash-code suffix 
#    (boot/GenericLCCApplication_tmpl_71ee0182 in this example)
#
#  - The argument boot.GenericLCCApplication<lcc.SimpleLCC>:simpleLCC must be 
#    surrounded by double quote in the command-line to avoid shell 
#    interpretation issue of '<' and '>'.
simpleLCC: build
	$(MINDC) -src-path=src -src-path=../common-src -o=build "boot.GenericLCCApplication<lcc.SimpleLCC>:simpleLCC"

build:
	mkdir -p build

clean:
	rm -Rf build

.PHONY: all simpleLCC clean
