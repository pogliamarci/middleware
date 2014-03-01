/*
 * Image normalization with MPI and OpenMP
 *
 * Middleware Technologies for Distributed Systems Project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */
#ifndef BENCHMARK_UTILS_H
#define BENCHMARK_UTILS_H

#ifdef BENCHMARK
#define BENCH_GETTIME(x) do {		\
	gettimeofday((x), NULL);	\
      } while(0)
#else
#define BENCH_GETTIME(x)
#endif

#include <sys/time.h>

void print_duration(struct timeval start, struct timeval end, char* why, int pid);

#endif