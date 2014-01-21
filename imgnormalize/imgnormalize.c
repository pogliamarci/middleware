#include <stdio.h>
#include <stdint.h>
#include <unistd.h>

#include <string.h>

#include <mpi.h>
#include <omp.h>

#include "distsys_image.h"
#include "imgnormalize_core.h"

char* fname = NULL;
int newMin = -1;
int newMax = -1;

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
    fprintf(stderr, "Usage: %s -b min:max -f filename\n", argv[0]);
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
            case '?':
            default:
                err = 1;
                break;
        }
    }
    if(err || newMin == -1 || newMax == -1 || fname == NULL)
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

    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    /* create a type for struct image_header_t */
    /* TODO move in another compilation unit, maybe near the definition of img_header_t... */
    const int nitems = 4;
    int blocklengths[4] = {1,1,1,1};
    MPI_Datatype types[4] = {MPI_INT, MPI_INT, MPI_INT, MPI_INT};
    MPI_Datatype img_header_mpi_t;
    MPI_Aint offset[4];
    offset[0] = offsetof(img_header_t, width);
    offset[1] = offsetof(img_header_t, height);
    offset[2] = offsetof(img_header_t, channels);
    offset[3] = offsetof(img_header_t, depth);
    MPI_Type_create_struct(nitems, blocklengths, offset, types, &img_header_mpi_t);
    MPI_Type_commit(&img_header_mpi_t);

    img_header_t* header;

    header = malloc(sizeof(img_header_t));

    int* sendcnts = NULL;
    int* displs = NULL;
    uint8_t* recvbuf = NULL;
    int recvcount;
    int elems_per_proc;
    int add_to_last;
    int i;

    image_t* img;

    if(rank == 0)
    {
        process_cli(argc, argv);
        image_read(fname, &img);
        /* Fill in header */
        memcpy(header, &(img->header), sizeof(img_header_t));
    }

    MPI_Bcast(header, 1, img_header_mpi_t, 0, MPI_COMM_WORLD);

    sendcnts = malloc(size*sizeof(int));
    displs   = malloc(size*sizeof(int));
    elems_per_proc = image_num_pixels(header) / size;
    add_to_last = image_num_pixels(header) % size;
    for(i=0; i<size; i++) {
        sendcnts[i] = elems_per_proc * header->channels;
        displs[i]   = elems_per_proc * header->channels * i;
    }
    sendcnts[size-1] += add_to_last;
    recvcount = sendcnts[rank];

    /* allocate buffers to accomodate data reception */
    recvbuf = malloc(sizeof(uint8_t) * header->channels * sendcnts[rank]);

    MPI_Scatterv(img->data, sendcnts, displs, MPI_UNSIGNED_CHAR,
            recvbuf, recvcount, MPI_UNSIGNED_CHAR, 0, MPI_COMM_WORLD);

    int* bounds = malloc(2*sizeof(int)*header->channels); // min and max
    int* min = malloc(sizeof(int)*header->channels);
    int* max = malloc(sizeof(int)*header->channels);
    image_get_bounds(header, recvcount, recvbuf, min, max);
    MPI_Reduce(min, &(bounds[0]), header->channels, MPI_INT, MPI_MIN, 0, MPI_COMM_WORLD);
    MPI_Reduce(max, &(bounds[header->channels]), header->channels, MPI_INT, MPI_MAX, 0, MPI_COMM_WORLD);
    MPI_Bcast(bounds, 2 * header->channels, MPI_INT, 0, MPI_COMM_WORLD);

    image_normalize(header, recvcount, recvbuf, &(bounds[0]),
            &(bounds[header->channels]), newMin, newMax);

    //TODO step 5 -> send normalized image
    //TODO step 6 -> save && free image

    if(rank == 0)
    {
        image_free(img);
    }
    free(sendcnts);
    free(displs);
    free(header);
    wrap_exit(0);
}

