/*
 * Image normalization with MPI and OpenMP
 *
 * Middleware Technologies for Distributed Systems Project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */
#ifndef _IMGNORMALIZE_CORE_H
#define _IMGNORMALIZE_CORE_H

#include "imageio.h"

void image_get_bounds(const img_header_t* head, const int slice_size,
        uint8_t* data, int* min, int* max);

void image_normalize(const img_header_t* head, const int slice_size,
        uint8_t* data, int min, int max, int newMin, int newMax);

#endif
