EXAMPLES = helloworld lifecycle factory

all: $(EXAMPLES:%=%.all)
clean: $(EXAMPLES:%=%.clean)

%.all:
	$(MAKE) -C $(@:%.all=%) all

%.clean:
	$(MAKE) -C $(@:%.clean=%) clean
