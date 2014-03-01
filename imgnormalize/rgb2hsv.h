/*
 * Image normalization with MPI and OpenMP
 *
 * Middleware Technologies for Distributed Systems Project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */
#ifndef RGB2HSV_H
#define RGB2HSV_H

#include <stdint.h>

typedef struct {
    uint8_t r;
    uint8_t g;
    uint8_t b;
} rgb_point_t;

typedef struct {
    uint8_t h;
    uint8_t s;
    uint8_t v;
} hsv_point_t;

hsv_point_t rgb2hsv(const rgb_point_t* rgb);
rgb_point_t hsv2rgb(const hsv_point_t* hsv);

#endif
