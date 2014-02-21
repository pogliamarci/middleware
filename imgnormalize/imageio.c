#include <stdint.h>
#include <stdlib.h>

#include <string.h>

#include "imageio.h"
#include "imageio_ppm.h"

#define BUF_LENGTH 100

const char* error_string(img_error_t err)
{
    const char* str[] =
    {
        "Ok",
        "File not found",
        "Error reading file: maybe the file is corrupted or the file format is not supported",
        "Error creating file"
    };
    return str[err];
}

int parse_magic_number(const char* fhead, img_header_t* header)
{
    char* mn = malloc(3*sizeof(char));
    strncpy(mn, fhead, 2);
    if(ppm_format_init(mn, header))
      return 1;
    return 0;
}

img_error_t image_read(const char* path, image_t** image)
{
    FILE* fp;
    char buff[BUF_LENGTH];

    fp = fopen(path, "rb");

    if(fp == NULL)
        return ENOTFOUND;

    fgets(buff, sizeof(buff), fp);

    /* check the image format */
    *image = (image_t*) malloc(sizeof(image_t));

    if(!parse_magic_number(buff, &((*image)->header)))
        goto img_err;


    img_error_t ret = (*image)->header.operations.read(*image, fp);

    /* make sure ppm image data was read */
    if ((*image)->data == NULL || ret != OK)
        goto data_err;

    fclose(fp);
    return ret;

data_err:
    free((*image)->data);
img_err:
    fclose(fp);
    free(*image);
    *image = NULL;
    return EIMGREAD;
}

const char* get_magic_number(const image_t* img)
{
    return img->header.operations.magic_number(img);
}

img_error_t image_write(const char* path, image_t image)
{
    FILE* fp;
    fp = fopen(path, "wb");
    if(fp == NULL)
        return ECREATEFILE;

    img_error_t ret = OK;
    image.header.operations.write(&image, fp);

    fclose(fp);
    return ret;
}

void image_free(image_t* image)
{
    free(image->data);
    free(image);
}

int image_num_pixels(const img_header_t header)
{
    return header.channels * header.width * header.height;
}
