#ifndef DATA_H
#define DATA_H

struct s {
	int a, b;
};

struct {
	int a, b;
} PRIVATE;

// could also be declared "struct s {int a, b;} PRIVATE" but this is not supported.

#endif //DATA_H
