#include <iostream>
#include <cstdlib>

#include <mpi.h>
#include <omp.h>

#include <unistd.h>
#include <stdint.h>

#include "imgutils.h"

char* fname = NULL;
int min = -1;
int max = -1;

using namespace imgnormalize;

inline void wrap_exit(int status)
{
    if(status == 0)
    {
        MPI_Finalize();
        exit(status);
    } else {
        MPI_Abort(MPI_COMM_WORLD, status);
    }
}

void print_usage(int argc, char** argv)
{
    std::cout << "Usage: " << argv[0] << " -b min:max -f filename" << std::endl;
}

void process_cli(int argc, char** argv)
{
    int c, err;
    while((c = getopt(argc, argv, "m:M:f:")) != -1)
    {
        switch(c) {
            case 'm':
                min = atoi(optarg);
                break;
            case 'M':
                max = atoi(optarg);
                break;
            case 'f':
                fname = optarg;
                break;
            case '?':
            default:
                err = 1;
                break;
        }
    }
    if(err || min == -1 || max == -1 || fname == NULL)
    {
        print_usage(argc, argv);
        wrap_exit(-1);
    }
}

int main(int argc, char** argv)
{
    MPI_Init(&argc, &argv);

    int rank;
    int size;

    uint8_t* img;

    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    if(rank == 0)
    {
        process_cli(argc, argv);
        img_t img = load(fname);
        // step 2 -> parallel computation of min and max
        // step 3 -> send results
        // step 4 -> generate normalized image
        // step 5 -> send normalized image
        // step 6 -> save
        imgfree(img);
    }

    wrap_exit(0);
}

