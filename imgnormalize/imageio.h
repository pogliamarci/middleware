#ifndef _DISTSYS_IMAGE_H
#define _DISTSYS_IMAGE_H

#include <stdint.h>
#include <stdlib.h>

enum img_fileformat {
    PLAIN_PPM,
    PPM
};

typedef enum {
    OK, ENOTFOUND, EIMGREAD, ECREATEFILE
} img_error_t;

const char* error_string(img_error_t err);

/* TODO the fileformat field hasn't been converted
 * in the MPI type. This doesn't matter due to how structures
 * are written in memory (i.e., it is the last field...)
 */
typedef struct {
	int width;
	int height;
	uint8_t channels;
    enum img_fileformat format;
} img_header_t;


typedef struct {
    img_header_t header;
    uint8_t* data;
} image_t;


img_error_t image_read(const char* path, image_t** image);

img_error_t image_write(const char* path, image_t image);

void image_free(image_t* image);

int image_num_pixels(const img_header_t header);

#endif