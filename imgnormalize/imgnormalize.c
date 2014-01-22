#include <stdio.h>
#include <stdint.h>
#include <unistd.h>
#include <string.h>

#include <mpi.h>

#include "distsys_image.h"
#include "imgnormalize_core.h"

#define OUTFNAME_DEFAULT "out.ppm"

char* outfname = NULL;
char* fname = NULL;
int newMin = -1;
int newMax = -1;

inline void mpiabort(int status)
{
    MPI_Abort(MPI_COMM_WORLD, status);
}

void print_usage(int argc, char** argv)
{
    fprintf(stderr, "Usage: %s -m min -M max -f filename -o output\n", argv[0]);
}

void process_cli(int argc, char** argv)
{
    int c, err;
    while((c = getopt(argc, argv, "m:M:f:")) != -1)
    {
        switch(c) {
            case 'm':
                newMin = atoi(optarg);
                break;
            case 'M':
                newMax = atoi(optarg);
                break;
            case 'f':
                fname = optarg;
                break;
            case 'o':
                outfname = optarg;
            case '?':
            default:
                err = 1;
                break;
        }
    }
    if(err || newMin == -1 || newMax == -1 || fname == NULL)
    {
        print_usage(argc, argv);
        mpiabort(-1);
    }
    if(outfname == NULL)
    {
        outfname = OUTFNAME_DEFAULT;
    }
}

int main(int argc, char** argv)
{
    int rank;
    int size;

    const int nitems = 4;
    int blocklengths[4] = {1,1,1,1};

    int* sendcnts = NULL;
    int* displs = NULL;
    uint8_t* recvbuf = NULL;
    int recvcount;
    int elems_per_proc;
    int add_to_last;
    image_t* img;
    img_header_t* header;
    int i;
    int min, max;

    MPI_Datatype types[4] = {MPI_INT, MPI_INT, MPI_INT, MPI_INT};
    MPI_Datatype img_header_mpi_t;
    MPI_Aint offset[4];

    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    if(rank == 0)
    {
        process_cli(argc, argv);
    }

    /* create a type for struct image_header_t */
    /* TODO move in another compilation unit, maybe near the definition of img_header_t... */
    offset[0] = offsetof(img_header_t, width);
    offset[1] = offsetof(img_header_t, height);
    offset[2] = offsetof(img_header_t, channels);
    offset[3] = offsetof(img_header_t, depth);
    MPI_Type_create_struct(nitems, blocklengths, offset, types, &img_header_mpi_t);
    MPI_Type_commit(&img_header_mpi_t);

    if((header = malloc(sizeof(img_header_t))) == NULL)
    {
        fprintf(stderr, "Malloc can't allocate memory\n");
        mpiabort(-1);
    }

    if(rank == 0)
    {
        if(!image_read(fname, &img))
        {
            fprintf(stderr, "Malloc can't allocate memory\n");
            mpiabort(-1);
        }
        /* Fill in header */
        memcpy(header, &(img->header), sizeof(img_header_t));
    }

    MPI_Bcast(header, 1, img_header_mpi_t, 0, MPI_COMM_WORLD);

    sendcnts = malloc(size*sizeof(int));
    displs   = malloc(size*sizeof(int));
    if(sendcnts == NULL || displs == NULL)
    {
        fprintf(stderr, "Malloc can't allocate memory\n");
        mpiabort(-1);
    }
    elems_per_proc = image_num_pixels(header) / size;
    add_to_last = image_num_pixels(header) % size;
    for(i=0; i<size; i++)
    {
        sendcnts[i] = elems_per_proc * header->channels;
        displs[i]   = elems_per_proc * header->channels * i;
    }
    sendcnts[size-1] += add_to_last;
    recvcount = sendcnts[rank];

    /* allocate buffers to accomodate data reception */
    recvbuf = malloc(sizeof(uint8_t) * header->channels * sendcnts[rank]);

    MPI_Scatterv(img->data, sendcnts, displs, MPI_UNSIGNED_CHAR,
            recvbuf, recvcount, MPI_UNSIGNED_CHAR, 0, MPI_COMM_WORLD);


    // Assumption for now (to be eventually relaxed...): in case of an RGB color
    // image we normalize the V channel after converting it in the HSV color space.
    image_get_bounds(header, recvcount, recvbuf, &min, &max);
    MPI_Allreduce(MPI_IN_PLACE, &min, 1, MPI_INT, MPI_MIN, MPI_COMM_WORLD);
    MPI_Allreduce(MPI_IN_PLACE, &max, 1, MPI_INT, MPI_MAX, MPI_COMM_WORLD);

    image_normalize(header, recvcount, recvbuf, min, max, newMin, newMax);

    MPI_Gatherv(recvbuf, recvcount, MPI_UNSIGNED_CHAR, img->data,
            sendcnts, displs, MPI_UNSIGNED_CHAR, 0, MPI_COMM_WORLD);

    if(rank == 0)
    {
        image_write(outfname, *img);
        image_free(img);
    }
    free(sendcnts);
    free(displs);
    free(header);
    MPI_Finalize();
    return 0;
}

