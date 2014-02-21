#include <stdio.h>

#include "benchmark_utils.h"

void print_duration(struct timeval start, struct timeval end, char* why)
{
    double duration = ((end.tv_sec-start.tv_sec)*1000000
            + end.tv_usec - start.tv_usec)/1000.0;
    fprintf(stderr, "%s duration = %lf\n", why, duration);
}
