#include <stdint.h>
#include <stdlib.h>

#include "distsys_image.h"

int image_read(const char* path, image_t** image)
{
    //TODO
    return -1;
}

int image_write(const char* path, image_t image)
{
    //TODO
    return -1;
}

void image_free(image_t* image)
{
    //TODO
    free(image->data);
    free(image);
}

int image_num_pixels(const img_header_t* header)
{
    //TODO
    return header->channels * header->width * header->height;
}
