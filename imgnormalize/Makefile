TARGET=imgnormalize

MPICC=mpicc
FLAGS=-Wall -O3 -g -D BENCHMARK
LIBS=-fopenmp -lm
INCL=-I.

OBJECTS = $(patsubst %.c, %.o, $(wildcard *.c))

all: $(TARGET)

$(TARGET): $(OBJECTS)
	$(MPICC) $(FLAGS) $(LIBS) $(INCL) -o $@ $^

%.o: %.c
	$(MPICC) $(FLAGS) $(LIBS) $(INCL) -c -o $@ $<

clean:
	rm -f $(OBJECTS) $(TARGET)
