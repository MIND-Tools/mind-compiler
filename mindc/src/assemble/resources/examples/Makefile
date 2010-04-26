EXAMPLES = helloworld lifecycle

all: $(EXAMPLES:%=%.all)
clean: $(EXAMPLES:%=%.clean)

%.all:
	$(MAKE) -C $(@:%.all=%) all

%.clean:
	$(MAKE) -C $(@:%.clean=%) clean
