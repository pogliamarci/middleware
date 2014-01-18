#include "imgutils.h"

#include <fstream>
#include <string>

#include <iostream>
#include <cstdlib>

//TODO although the file is a C++ file, this is an horrible bunch of poorly written C code :-

namespace imgnormalize {

    const uint8_t CHANNELS = 3;

    img_t load(const char* filename)
    {
        std::ifstream f;
        f.open(filename);

        char m1, m2;

        img_t img;

        f >> m1 >> m2;

        if(m1 == 'P' && m2 == '6') //TODO implement more file formats...
        {
            std::string width, height;
            std::string maxval;
            f >> width >> height >> maxval >> m1;
            std::cout << atoi(width.c_str()) << atoi(height.c_str())
                << atoi(maxval.c_str()) << std::endl;
            img.width = atoi(width.c_str());
            img.height = atoi(height.c_str());
            img.maxval = atoi(maxval.c_str());
            img.channels = CHANNELS;
            if(img.maxval >= 256)
                img.depth = 2;
            else img.depth = 1;

            img.img = (void**) malloc(sizeof(void*) * CHANNELS);
            for(int k = 0; k < CHANNELS; ++k)
            {
                if(img.depth == 1)
                    img.img[k] = malloc(sizeof(uint8_t) * img.width * img.height * CHANNELS);
                else img.img[k] = malloc(sizeof(uint16_t) * img.width * img.height * CHANNELS);
            }

            uint8_t msb, lsb;
            for(int i = 0; i < img.height; ++i) {
                for(int j = 0; j < img.width; ++j) {
                    for(int k = 0; j < CHANNELS; ++k)
                    {
                        if(img.depth == 1)
                            f >> static_cast<uint8_t*>(img.img[k])[i * img.width + j]; //R
                        else
                        {
                            f >> msb >> lsb;
                            static_cast<uint8_t*>(img.img[k])[i * img.width + j] = (msb << 8) + lsb;
                        }
                    }
                }
            }
        } else {
            std::cerr << "File format not recognized!" << std::endl;
        }
        f.close();
        return img;
    }

    void save(img_t image, const char* filename)
    {
        std::ofstream f;
        f.open(filename);

        f << "P6\n";
        f << image.width << std::endl;
        f << image.height << std::endl;
        f << image.maxval << std::endl;
        for(int i = 0; i < image.height; i++)
        {
            for(int j = 0; j < image.width; j++)
            {
                for(int k = 0; k < CHANNELS; k++)
                {
                    if(image.depth == 1)
                        f << static_cast<uint8_t*>(image.img[k])[i * image.width + image.height];
                    else { // 2 bytes
                        uint16_t b = static_cast<uint16_t*>(image.img[k])[i * image.width + image.height];
                        f << (b & 0xF0 >> 8) << (b & 0x0F);
                    }
                }
            }
        }
    }

    void imgfree(img_t image)
    {
        for(int i = 0; i < image.channels; ++i)
        {
            free(image.img[i]);
        }
        free(image.img);
    }

}
