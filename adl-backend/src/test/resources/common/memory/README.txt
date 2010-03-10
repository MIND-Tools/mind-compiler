WARNING : this folder contains a copy of the fractal-runtime module sources.
This is used by adl-backend tests.

It has been chosen as a before solution compared to declaring a dependency to 
the fractal-runtime module and unpack-it at "generate-test-resource" phase. 
Since it is causing troubles while developing in Eclipse, and since these 
sources are not supposed to evolve frequently.