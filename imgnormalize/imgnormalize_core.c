#include <omp.h>

#include "imgnormalize_core.h"
#include "rgb2hsv.h"

#define DISPLACEMENT(_h) ((_h->channels))
#define MIN(_x, _y) ((_x) < (_y) ? (_x) : (_y))
#define MAX(_x, _y) ((_x) > (_y) ? (_x) : (_y))
#define VALUE_DISPL(_i, _channels) (((_channels) == 1) ? (_i) : ((_i) + 2))

void image_get_bounds(const img_header_t* head, const int slice_size,
        uint8_t* data, int* min, int* max)
{
    int displ = DISPLACEMENT(head);
    int channels = head->channels;
    *min = 0;
    *max = 0;
#pragma omp parallel
    {
        int i;
        int loc_min = 0;
        int loc_max = 0;
#pragma omp for nowait
        for(i = 0; i < slice_size; i+=displ)
        {
            //TODO... (cleaner implementation for the two cases grayscale\rgb?)
            if(channels == 1)
            {
                loc_min = MIN(data[i], loc_min);
                loc_max = MAX(data[i], loc_max);
            } else {
                rgb_point_t rgb;
                rgb.r = data[i];
                rgb.g = data[i+1];
                rgb.b = data[i+2];
                hsv_point_t hsv = rgb2hsv(&rgb);
                loc_min = MIN(hsv.v, loc_min);
                loc_max = MAX(hsv.v, loc_max);
            }
        }
#pragma omp critical
        {
            *min = MIN(loc_min, *min);
            *max = MAX(loc_max, *max);
        }
    }
}

void image_normalize(const img_header_t* head, const int slice_size,
        uint8_t* data, int min, int max, int newMin, int newMax)
{
    int i;
    int displ = DISPLACEMENT(head);
    int channels = head->channels;

#pragma omp parallel for private(i)
    for(i = 0; i < slice_size; i+=displ)
    {
        uint8_t pixel = data[VALUE_DISPL(i, channels)];
        pixel = (pixel - min) * (newMax - newMin) / (max - min) + newMin;
        data[VALUE_DISPL(i, channels)] = pixel;
    }
}
