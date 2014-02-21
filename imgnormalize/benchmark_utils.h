#ifndef BENCHMARK_UTILS_H
#define BENCHMARK_UTILS_H

#include <sys/time.h>

void print_duration(struct timeval start, struct timeval end, char* why);

#endif
