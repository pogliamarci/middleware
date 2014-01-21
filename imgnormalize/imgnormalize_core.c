#include <omp.h>

#include "imgnormalize_core.h"

#define DISPLACEMENT(_h) ((_h->channels))
#define MIN(_x, _y) ((_x) < (_y) ? (_x) : (_y))
#define MAX(_x, _y) ((_x) > (_y) ? (_x) : (_y))

void image_get_bounds(const img_header_t* head, const int slice_size,
        uint8_t* data, int* min, int* max)
{
    *min = 0;
    *max = 0;
#pragma omp parallel
    {
        int i;
        int displ = DISPLACEMENT(head);
        int channels = head->channels;
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
                //TODO... (cleaner implementation for the two cases grayscale\rgb?)
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
    // TODO TODO TODO
}
