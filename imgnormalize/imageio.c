#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>

#include "imageio.h"

#define BUF_LENGTH 71

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

    if(fhead[0] == 'P')
    {
        switch(fhead[1]) {
            case '2':
                header->channels = 1;
                header->format = PLAIN_PPM;
                break;
            case '3':
                header->channels = 3;
                header->format = PLAIN_PPM;
                break;
            case '5':
                header->channels = 1;
                header->format = PPM;
                break;
            case '6':
                header->channels = 3;
                header->format = PPM;
                break;
            default:
                return 0;
        }
    } else {
        return 0;
    }
    return 1;
}

img_error_t image_read(const char* path, image_t** image)
{
    FILE* fp;
    char buff[BUF_LENGTH];
    int i;

    fp = fopen(path, "rb");

    if(fp == NULL)
        return ENOTFOUND;

    fgets(buff, sizeof(buff), fp);

    /* check the image format */
    *image = (image_t*) malloc(sizeof(image_t));


    if(!parse_magic_number(buff, &((*image)->header)))
        goto img_err;

    /* eat up comments */
    do {
        fgets(buff, sizeof(buff), fp);
    } while(buff[0] == '#');

    /* parse the remainder of the header (width and height in pixels) */
    for(i = 0; buff[i] != '\0'; i++)
    {
        if(buff[i] == ' ')
        {
            buff[i] = '\0';
            break;
        }
    }
    ((*image)->header).width = atoi(buff);
    ((*image)->header).height = atoi(buff+i+1);

    /* check if maximum depth is less than a single byte */
    fgets(buff, sizeof(buff), fp);
    if(atoi(buff) > 255)
        goto img_err;

    (*image)->data = (uint8_t*) malloc(image_num_pixels((*image)->header) * sizeof(uint8_t));

    /* verify memory allocation */
    if (!(*image)->data)
        goto img_err;

    switch(((*image)->header).format)
    {
        case PLAIN_PPM:
            for(i = 0; !feof(fp); i++)
            {
                fgets(buff, sizeof(buff), fp);
                (*image)->data[i] = atoi(buff);
            }
            break;
        case PPM:
            for(i = 0; !feof(fp) && i < image_num_pixels((*image)->header); ++i)
            {
                (*image)->data[i] = fgetc(fp);
            }
            break;
        default:
            goto data_err;
            break;
    }

    /* make sure ppm image data was read */
    if ((*image)->data == NULL)
        goto data_err;

    fclose(fp);
    return OK;

data_err:
    free((*image)->data);
img_err:
    fclose(fp);
    free(*image);
    *image = NULL;
    return EIMGREAD;
}


const char* get_magic_number(const img_header_t* head)
{
    switch(head->format)
    {
        case PPM:
            if(head->channels == 1)
                return "P5";
            else
                return "P6";
            break;
        case PLAIN_PPM:
            if(head->channels == 1)
                return "P2";
            else
                return "P3";
        default:
            return "";
            break;
    }
}

img_error_t image_write(const char* path, image_t image)
{
    FILE* fp;
    char buff[BUF_LENGTH];
    int i;

    fp = fopen(path, "wb");

    if(fp == NULL)
        return ECREATEFILE;

    fputs(get_magic_number(&image.header), fp);
    fputc('\n', fp);

    fprintf(fp, "%d %d\n", image.header.width, image.header.height);
    fprintf(fp, "%d\n", 255);

    switch(image.header.format)
    {
        case PPM:
            for(i = 0; i < image_num_pixels(image.header); i++)
            {
                fputc(image.data[i], fp);
            }
            break;
        case PLAIN_PPM:
            for(i = 0; i < image_num_pixels(image.header); i++)
            {
                snprintf(buff, BUF_LENGTH, "%d\n", image.data[i]);
                fputs(buff, fp);
            }
            break;
    }

    fclose(fp);

    return OK;
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
