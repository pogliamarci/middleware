#ifndef _DISTSYS_IMAGE_H
#define _DISTSYS_IMAGE_H

#include <stdint.h>
#include <stdlib.h>


/****** Data types *******/

typedef struct {
	size_t width;
	size_t height;
	size_t channels;
}img_header_t;


typedef struct {
	img_header_t header;
	uint8_t* data;
}image_t;


/****** Functions ******/

/*
 * Returned values
 * 	KO: -1
 *      OK: 1
 */
int image_read(const char* path, image_t** image);

/*
 * Returned values
 *      KO: -1
 *      OK: 1
 */
int image_write(const char* path, image_t image);

void image_free(image_t* image);

int image_num_pixels(const img_header_t* header);

#endif
