#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>

#include "distsys_image.h"


int image_read(const char* path, image_t** image)
{
	FILE* fp;
	char buff[71];
	int i;

	fp = fopen(path, "rb");

	if(fp == NULL)
		return -1;

	fgets(buff, sizeof(buff), fp);

	// check the image format
    	if (buff[0] != 'P' || (buff[1] != '2' && buff[1] != '3'))
         	return -1;

	*image = (image_t*) malloc(sizeof(image_t));

	if(buff[1] == '2')
		((*image)->header).channels = 1;
	else
		((*image)->header).channels = 3;

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
        	free((*image)->data);
        	fclose(fp);
        	return -1;
	}

	for(i = 0; !feof(fp); i++)
	{
		fgets(buff, sizeof(buff), fp);
		(*image)->data[i] = atoi(buff);
	}

	// make sure ppm image data was read
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
