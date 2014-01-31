#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>

#include "distsys_image.h"

#define PLAIN_PPM 0
#define PPM 1

int image_read(const char* path, image_t** image)
{
    FILE* fp;
    char buff[71];
    int i;

    fp = fopen(path, "rb");

    if(fp == NULL)
    {
        perror("Can't open the image file");
        return -1;
    }

    fgets(buff, sizeof(buff), fp);

    // check the image format
    *image = (image_t*) malloc(sizeof(image_t));
    if(buff[0] == 'P')
    {
        switch(buff[1]) {
            case '2':
                ((*image)->header).channels = 1;
                ((*image)->header).format = PLAIN_PPM;
                break;
            case '3':
                ((*image)->header).channels = 3;
                ((*image)->header).format = PLAIN_PPM;
                break;
            case '5':
                ((*image)->header).channels = 1;
                ((*image)->header).format = PPM;
                break;
            case '6':
                ((*image)->header).channels = 3;
                ((*image)->header).format = PPM;
                break;
            default:
                fprintf(stderr, "Unknown file format\n");
                goto img_err;
        }
    } else {
        fprintf(stderr, "Unknown file format\n");
        goto img_err;
    }

    // skip comments
    do
    {
        fgets(buff, sizeof(buff), fp);
    } while(buff[0] == '#');

    for(i = 0; buff[i] != '\0'; i++)
        if(buff[i] == ' ')
        {
            buff[i] = '\0';
            break;
        }
    ((*image)->header).width = atoi(buff);
    ((*image)->header).height = atoi(buff+i+1);

    // check if maximum depth is less than a single byte
    fgets(buff, sizeof(buff), fp);
    if(atoi(buff) > 255)
    {
        fprintf(stderr, "This program supports 8-bit grayscale and 24-bit RGB images\n");
        goto img_err;
    }
    printf("%d\n", atoi(buff));

    (*image)->data = (uint8_t*) malloc(image_num_pixels((*image)->header) * sizeof(uint8_t));

    // verify memory allocation
    if (!(*image)->data)
    {
        fprintf(stderr, "Can't allocate memory to store the image\n");
        goto img_err;
    }

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
            fprintf(stderr, "WTF???\n");
            goto data_err;
            break;
    }

    // make sure ppm image data was read
    if ((*image)->data == NULL)
    {
        fprintf(stderr, "Error reading image data\n");
        goto data_err;
    }

    fclose(fp);
    return 1;

data_err:
    free((*image)->data);
img_err:
    fclose(fp);
    free(*image);
    *image = NULL;
    return -1;
}


int image_write(const char* path, image_t image)
{
    FILE* fp;
    char buff[71];
    int i;

    fp = fopen(path, "wb");

    if(fp == NULL)
        return -1;

    switch(image.header.format)
    {
        case PPM:
            if(image.header.channels == 1)
                fputs("P5\n", fp);
            else fputs("P6\n", fp);
            break;
        case PLAIN_PPM:
            if(image.header.channels == 1)
                fputs("P2\n", fp);
            else
                fputs("P3\n", fp);
        default:
            break;
    }

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
                snprintf(buff, 71, "%d\n", image.data[i]); //TODO cosa diavolo e' 71??
                fputs(buff, fp);
            }
            break;
    }

    fclose(fp);

    return 1;
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
