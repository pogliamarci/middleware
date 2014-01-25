#ifndef _DISTSYS_IMAGE_H
#define _DISTSYS_IMAGE_H

#include <stdint.h>
#include <stdlib.h>



/****** Data types *******/

typedef struct
{
	uint16_t bfType;	// specifies the file type
	uint32_t bfSize;	// specifies the size in bytes of the bitmap file
	uint16_t bfReserved1;	// reserved; must be 0
	uint16_t bfReserved2;	// reserved; must be 0
	uint32_t bfOffBits;	// species the offset in bytes from the bitmapfileheader to the bitmap bits
}BITMAPFILEHEADER;


typedef struct
{
	uint32_t biSize;		// specifies the number of bytes required by the struct
	uint64_t biWidth;		// specifies width in pixels
	uint64_t biHeight;		// species height in pixels
	uint16_t biPlanes;		// specifies the number of color planes, must be 1
	uint16_t biBitCount;		// specifies the number of bit per pixel
	uint32_t biCompression;		// spcifies the type of compression
	uint32_t biSizeImage;		// size of image in bytes
	uint64_t biXPelsPerMeter;	// number of pixels per meter in x axis
	uint64_t biYPelsPerMeter;	// number of pixels per meter in y axis
	uint32_t biClrUsed;		// number of colors used by the bitmap
	uint32_t biClrImportant;	// number of colors that are important
}BITMAPINFOHEADER;


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

int image_num_pixels(const img_header_t header);

#endif
