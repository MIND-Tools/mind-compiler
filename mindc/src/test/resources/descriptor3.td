<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE target PUBLIC "-//mind.ow2.org//Target Descriptor//EN" 
  "classpath://org/ow2/mind/target/ast/targetDescriptor.dtd">

<target name="descriptor3">
	<compiler path="${TOOL_DIR}/gcc" />
	<linker path="${TOOL_DIR}/gcc" />
	<cFlag value="-I${TOOL_DIR}/lib" />
</target>