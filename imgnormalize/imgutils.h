#ifndef MW_IMGUTILS_H
#define MW_IMGUTILS_H

#include <stdint.h>

namespace imgnormalize {

    typedef struct {
        uint8_t channels;
        uint8_t depth;
        void** img; // one matrix per channel, row major order
        int width;
        int height;
        int maxval;
    } img_t;

    img_t load(const char* filename);
    void save(img_t image, const char* filename);
    void imgfree(img_t image);

}

#endif
