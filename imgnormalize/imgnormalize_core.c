/*
 * Image normalization with MPI and OpenMP
 *
 * Middleware Technologies for Distributed Systems Project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */
#include <omp.h>

#include "imgnormalize_core.h"
#include "rgb2hsv.h"

#define DISPLACEMENT(_h) ((_h->channels))
#define MIN(_x, _y) ((_x) < (_y) ? (_x) : (_y))
#define MAX(_x, _y) ((_x) > (_y) ? (_x) : (_y))

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
            if(channels == 1)
            {
                loc_min = MIN(data[i], loc_min);
                loc_max = MAX(data[i], loc_max);
            } else {
                rgb_point_t rgb;
                hsv_point_t hsv;
                rgb.r = data[i];
                rgb.g = data[i+1];
                rgb.b = data[i+2];
                hsv = rgb2hsv(&rgb);
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
        if(channels == 1)
            data[i] = (data[i] - min) * (newMax - newMin) / (max - min) + newMin;
        else {
            rgb_point_t rgb;
            hsv_point_t hsv;
            rgb.r = data[i];
            rgb.g = data[i+1];
            rgb.b = data[i+2];
            hsv = rgb2hsv(&rgb);
            hsv.v = (hsv.v - min) * (newMax - newMin) / (max - min) + newMin;
            rgb = hsv2rgb(&hsv);
            data[i] = rgb.r;
            data[i+1] = rgb.g;
            data[i+2] = rgb.b;
        }
    }
}
