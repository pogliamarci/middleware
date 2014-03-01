/*
 * Image normalization with MPI and OpenMP
 *
 * Middleware Technologies for Distributed Systems Project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */
#include "imageio_ppm.h"
#include "imageio.h"

#include <string.h>

#define BUF_LENGTH 100
#define LINE_LENGTH 19

/* Declaration of the structures with the file format-specific operations */

img_error_t ppm_read(image_t* image, FILE* fp);
img_error_t ppm_write(const image_t* image, FILE* fp);
const char* ppm_magic_number(const image_t* img);

struct img_operations ppmops = {
    .read = &ppm_read,
    .write = &ppm_write,
    .magic_number = &ppm_magic_number
};

img_error_t plain_read(image_t* image, FILE* fp);
img_error_t plain_write(const image_t* image, FILE* fp);
const char* plain_magic_number(const image_t* img);

struct img_operations plainops = {
    .read = &plain_read,
    .write = &plain_write,
    .magic_number = &plain_magic_number
};

int ppm_format_init(const char* magic_number, img_header_t* header)
{
  if(magic_number[0] != 'P')
    return 0;
  switch(magic_number[1])
  {
    case '2':
	header->channels = 1;
	header->format = PLAIN_PPM;
	header->operations = plainops;
	break;
    case '3':
	header->channels = 3;
	header->format = PLAIN_PPM;
	header->operations = plainops;
	break;
    case '5':
	header->channels = 1;
	header->format = PPM;
	header->operations = ppmops;
	break;
    case '6':
	header->channels = 3;
	header->format = PPM;
	header->operations = ppmops;
	break;
    default:
	return 0;
    }
    return 1;
}

/*** Functions common to both PPM and PLAIN PPM + helper stuff ***/

img_error_t ppm_common_read(image_t* image, FILE* fp)
{
    int i;
    char buff[BUF_LENGTH];
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
    (image->header).width = atoi(buff);
    (image->header).height = atoi(buff+i+1);

    /* check if maximum depth is less than a single byte */
    fgets(buff, sizeof(buff), fp);
    if(atoi(buff) > 255)
        return EIMGREAD;


    image->data = (uint8_t*) malloc(image_num_pixels(image->header) * sizeof(uint8_t));
    /* verify memory allocation */
    if (!(image->data))
        return EIMGREAD;
    return OK;
}

/*** Binary PPM implementation ***/

img_error_t ppm_read(image_t* image, FILE* fp)
{
    img_error_t ret = ppm_common_read(image, fp);
    if(ret != OK)
      return ret;
    fread(image->data, sizeof(uint8_t), image_num_pixels(image->header), fp);
    return OK;
}

img_error_t ppm_write(const image_t* image, FILE* fp)
{
    char buff[BUF_LENGTH];
    snprintf(buff, BUF_LENGTH, "%s\n", get_magic_number(image));
    fwrite(buff, sizeof(char), strlen(buff), fp);
    snprintf(buff, BUF_LENGTH, "%d %d\n", image->header.width, image->header.height);
    fwrite(buff, sizeof(char), strlen(buff), fp);
    snprintf(buff, BUF_LENGTH, "%d\n", 255);
    fwrite(buff, sizeof(char), strlen(buff), fp);
    fwrite(image->data, sizeof(uint8_t), image_num_pixels(image->header), fp);
    return OK;
}

const char* ppm_magic_number(const image_t* img)
{
    return (img->header.channels == 1) ? "P5" : "P6";
}


/*** Plain PPM implementation ***/

img_error_t plain_read(image_t* image, FILE* fp)
{
    int i = 0;
    const char delims[] = " \n\r\t";
    char* line = NULL;

    img_error_t ret = ppm_common_read(image, fp);
    if(ret != OK)
      return ret;
    size_t n = 0;
    while(getline(&line, &n, fp) != -1)
    {
        char* str = strtok(line, delims);
        while(str != NULL)
        {
            if(i >= image_num_pixels(image->header))
            {
                free(line);
                return EIMGREAD;
            }
            image->data[i++] = atoi(str);
            str = strtok(NULL, delims);
        }
    }
    free(line);
    return OK;
}

const char* plain_magic_number(const image_t* img)
{
    return (img->header.channels == 1) ? "P2" : "P3";
}

// We use a custom integer to ascii implementation for efficiency purposes.
// Given our constraints, using this function instead of using the C standard
// library improves execution time of about a magnitude order.
// CRITICAL: buffer needs to be sized at least nchar*sizeof(char)*4+2
// and should be already allocated when calling this function.
char* line2string(uint8_t* chararray, int nchar, char* buffer)
{
    // we populate the string starting from the end of the buffer...
    char* bufend = buffer+(4*nchar+2);
    *--bufend = '\0';
    *--bufend = '\n';
    int i;
    for(i=nchar-1; i>=0;i--)
    {
        int c = chararray[i];
        if(i != nchar-1)
            *--bufend = ' ';
        while(c >= 10)
        {
            *--bufend = c % 10 + '0';
            c/=10;
        }
        *--bufend = c + '0';
    }
    return bufend;
}

img_error_t plain_write(const image_t* image, FILE* fp)
{
    char buff[BUF_LENGTH];
    int i;
    int np = image_num_pixels(image->header);

    fputs(get_magic_number(image), fp);
    fputc('\n', fp);
    fprintf(fp, "%d %d\n", image->header.width, image->header.height);
    fprintf(fp, "%d\n", 255);
    for(i = 0; i < np;)
    {
        int length = (np - i) < LINE_LENGTH ? (np - i) : LINE_LENGTH;
        char* ptr = line2string(&(image->data[i]), LINE_LENGTH, buff);
        fputs(ptr, fp);
        i += length;
    }
    return OK;
}
