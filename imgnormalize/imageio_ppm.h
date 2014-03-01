/*
 * Image normalization with MPI and OpenMP
 *
 * Middleware Technologies for Distributed Systems Project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */
#ifndef IMAGEIO_PPM_H
#define IMAGEIO_PPM_H

#include "imageio.h"

int ppm_format_init(const char* magic_number, img_header_t* header);

#endif