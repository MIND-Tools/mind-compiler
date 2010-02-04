Use a pointer to PRIVATE.
currently this example requires a cast to be able to assign a pointer to RPIVATE
to a variable but this may not be necessary if PRIVATE can be declared as :

struct s {
  int a, b
} PRIVATE;