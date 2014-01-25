#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>

#include "distsys_image.h"

int image_read(const char* path, image_t** image)
{
	FILE* fp;
	BITMAPFILEHEADER bitmapFileHeader;
	BITMAPINFOHEADER bitmapInfoHeader;

	fp = fopen(path, "rb");
	
	if(fp == NULL)
		return -1;

	fread(&bitmapFileHeader, sizeof(BITMAPFILEHEADER), 1, fp);

	//verify that this is a bmp file by check bitmap id
    	if (bitmapFileHeader.bfType != 0x4D42)
    	{
        	fclose(fp);
        	return -1;
    	}

	// read the bitmap info header
	fread(&bitmapInfoHeader, sizeof(BITMAPINFOHEADER), 1, fp);

	(*image)->header.width = bitmapInfoHeader.biWidth;
	(*image)->header.height = bitmapInfoHeader.biHeight;
	(*image)->header.channels = 1; // bitmapInfoHeader.boh;

	// move file point to the begging of bitmap data
	fseek(fp, bitmapFileHeader.bfOffBits, SEEK_SET);

	*image = (image_t*) malloc(sizeof(image_t));

	(*image)->data = (uint8_t*) malloc(image_num_pixels((*image)->header) * sizeof(uint8_t));

	// verify memory allocation
    	if (!(*image)->data)
	{
        	free((*image)->data);
        	fclose(fp);
        	return -1;
	}

	fread((*image)->data, sizeof(uint8_t), image_num_pixels((*image)->header), fp);

	// make sure bitmap image data was read
    	if ((*image)->data == NULL)
    	{
        	fclose(fp);
        	return -1;
    	}

	fclose(fp);

	return 1;
}

int image_write(const char* path, image_t image)
{
	FILE* fp;

        fp = fopen(path, "wb");

        if(fp == NULL)
                return -1;

	//fwrite();

	fwrite(image.data, sizeof(uint8_t), image_num_pixels(image.header), fp);

        fclose(fp);

        return 1;
}

void image_free(image_t* image)
{
    //TODO
    free(image->data);
    free(image);
}

int image_num_pixels(const img_header_t header)
{
    return header.channels * header.width * header.height;
}
