# If the MIND_ROOT variable is defined, look for 'mindc' in this directory
# otherwise, expect that 'mindc' is in the PATH.
ifdef MIND_ROOT
  # If MIND_ROOT contains '\', replace them by '/' (for Windows)
  MINDC = $(subst \,/, $(MIND_ROOT)/bin/mindc)
else 
  MINDC = mindc
endif

all: helloworld

helloworld: build
	$(MINDC) -src-path=src -o=build helloworld.HelloworldApplication:hw

build:
	mkdir -p build

clean:
	rm -Rf build

.PHONY: all helloworld clean
