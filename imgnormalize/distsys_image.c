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
    int type;

    fp = fopen(path, "rb");

    if(fp == NULL)
    {
        perror("Can't open the image file");
        return -1;
    }

    fgets(buff, sizeof(buff), fp);

    // check the image format
    *image = (image_t*) malloc(sizeof(image_t));
    printf("Magic number: %c %c\n", buff[0], buff[1]);
    if(buff[0] == 'P')
    {
        switch(buff[1]) {
            case '2':
                ((*image)->header).channels = 1;
                type = PLAIN_PPM;
                break;
            case '3':
                ((*image)->header).channels = 3;
                type = PLAIN_PPM;
                break;
            case '5':
                ((*image)->header).channels = 1;
                type = PPM;
                break;
            case '6':
                ((*image)->header).channels = 3;
                type = PPM;
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

    // skip maximum value
    fgets(buff, sizeof(buff), fp);

    (*image)->data = (uint8_t*) malloc(image_num_pixels((*image)->header) * sizeof(uint8_t));

    // verify memory allocation
    if (!(*image)->data)
    {
        fprintf(stderr, "Can't allocate memory to store the image\n");
        goto img_err;
    }

    switch(type)
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

    if(image.header.channels == 1)
        fputs("P2\n", fp);
    else
        fputs("P3\n", fp);

    fputs("# middleware project\n", fp);
    fprintf(fp, "%d %d\n", image.header.width, image.header.height);
    fprintf(fp, "%d\n", 255);

    for(i = 0; i < image_num_pixels(image.header); i++)
    {
        snprintf(buff, 71, "%d\n", image.data[i]);
        fputs(buff, fp);
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
