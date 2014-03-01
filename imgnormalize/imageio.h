/*
 * Image normalization with MPI and OpenMP
 *
 * Middleware Technologies for Distributed Systems Project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */
#ifndef _IMAGEIO_H
#define _IMAGEIO_H

#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>

enum img_fileformat {
    PLAIN_PPM,
    PPM
};

typedef enum {
    OK, ENOTFOUND, EIMGREAD, ECREATEFILE
} img_error_t;


struct image_struct;
typedef struct image_struct image_t;

struct img_operations {
    img_error_t (*read)(image_t* image, FILE* fp);
    img_error_t (*write)(const image_t* image, FILE* fp);
    const char* (*magic_number)(const image_t* img);
};

/* NB: The fileformat field and the img_operations structure haven't been converted
 * in the MPI type. This doesn't matter due to how structures
 * are written in memory (i.e., it is the last field...)
 */
typedef struct {
	int width;
	int height;
	uint8_t channels;
	enum img_fileformat format;
	struct img_operations operations;
} img_header_t;


struct image_struct {
    img_header_t header;
    uint8_t* data;
};

const char* error_string(img_error_t err);

img_error_t image_read(const char* path, image_t** image);

img_error_t image_write(const char* path, image_t image);

void image_free(image_t* image);

int image_num_pixels(const img_header_t header);

const char* get_magic_number(const image_t* img);

#endif
