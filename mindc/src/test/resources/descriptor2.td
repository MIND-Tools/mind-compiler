<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE target PUBLIC "-//mind.ow2.org//Target Descriptor//EN" 
  "classpath://org/ow2/mind/target/ast/targetDescriptor.dtd">

<target name="descriptor2">
	<extends name="descriptor1" />
	<adlMapping mapping="unix.boot.genericBootstrap(${inputADL})" />
	<compiler path="gcc" />
	<linker path="ld" />
	<cFlag id="warning" value="-Wall" />
	<cFlag id="optimization" value="-O1" />
</target>
