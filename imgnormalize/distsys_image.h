#include <stdint.h>
#include <stdlib.h>


typedef struct {
	size_t width;
	size_t height;
	size_t channels;
}img_header_t;

int pixelSize(img_header_t header);

/*
 * Returned values
 * 	KO: -1
 *      OK: 0
 */
int read(char *path, img_header_t header, uint8_t** data);

/*
 * Returned values
 *      KO: -1
 *      OK: 0
 */
int write(char *path, img_header_t header, uint8_t** data);
