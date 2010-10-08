
// allocate an integer in .data section.
.data

// This variable is exported under two names, so as to accomodate the
// leading underscore automatically added by some platforms.
.globl myGlobalInt
.globl _myGlobalInt

.align 4
myGlobalInt:
_myGlobalInt:
.byte 0xaa
.byte 0xaa
.byte 0xaa
.byte 0xaa
