#ifndef _DISTSYS_IMGNORMALIZE_CORE_H
#define _DISTSYS_IMGNORMALIZE_CORE_H

#include "distsys_image.h"

void image_get_bounds(const img_header_t* head, const int slice_size,
        uint8_t* data, int* min, int* max);

void image_normalize(const img_header_t* head, const int slice_size,
        uint8_t* data, int min, int max, int newMin, int newMax);

#endif
