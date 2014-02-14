#include <stdio.h>
#include <stdint.h>
#include <unistd.h>
#include <string.h>

#include <mpi.h>

#ifdef BENCHMARK
#include <sys/time.h>
#endif

#include "imageio.h"
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
    int c, err = 0;
    while((c = getopt(argc, argv, "m:M:f:o:")) != -1)
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
        mpiabort(-1);
    }
    if(outfname == NULL)
    {
        outfname = OUTFNAME_DEFAULT;
    }
}

#ifdef BENCHMARK
void print_duration(struct timeval start, struct timeval end, char* why)
{
	double duration = ((end.tv_sec-start.tv_sec)*1000000
			+ end.tv_usec - start.tv_usec)/1000.0;
	printf("%s duration = %lf\n", why, duration);
}
#endif

int main(int argc, char** argv)
{
    int rank;
    int size;

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
    MPI_Datatype img_header_mpi_t;

#ifdef BENCHMARK
    struct timeval tStart, tEnd, tBoundsStart, tBoundsEnd, tNormStart, tNormEnd;
    struct timeval tOpenStart, tOpenEnd, tSaveStart, tSaveEnd;
#endif

    const int nitems = 3;
    int blocklengths[] = {1,1,1};
    MPI_Datatype types[] = {MPI_INT, MPI_INT, MPI_INT};
    MPI_Aint offset[] = {
        offsetof(img_header_t, width),
        offsetof(img_header_t, height),
        offsetof(img_header_t, channels)
    };

#ifdef BENCHMARK
    gettimeofday(&tStart, NULL);
#endif

    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Type_create_struct(nitems, blocklengths, offset, types, &img_header_mpi_t);
    MPI_Type_commit(&img_header_mpi_t);

    process_cli(argc, argv);

    header = malloc(sizeof(img_header_t));

    if(header == NULL)
    {
        fprintf(stderr, "Error: malloc can't allocate memory\n");
        mpiabort(-1);
    }

#ifdef BENCHMARK
    gettimeofday(&tOpenStart, NULL);
#endif
    if(rank == 0)
    {
        img_error_t err = image_read(fname, &img);
        if(err != OK)
        {
            fprintf(stderr, "Error reading image: %s\n", error_string(err));
            mpiabort(-1);
        }
        /* Fill in header */
        memcpy(header, &(img->header), sizeof(img_header_t));
    }
#ifdef BENCHMARK
    gettimeofday(&tOpenEnd, NULL);
#endif

    /* Broadcast the header to every process */
    MPI_Bcast(header, 1, img_header_mpi_t, 0, MPI_COMM_WORLD);

    sendcnts = malloc(size*sizeof(int));
    displs   = malloc(size*sizeof(int));
    if(sendcnts == NULL || displs == NULL)
    {
        fprintf(stderr, "Error: malloc can't allocate memory\n");
        mpiabort(-1);
    }

    /* Build the array with the size and the displacement of each process's task */
    elems_per_proc = image_num_pixels(*header) / size;
    add_to_last = image_num_pixels(*header) % size;
    for(i=0; i<size; i++)
    {
        sendcnts[i] = elems_per_proc;
        displs[i]   = elems_per_proc * i;
    }
    sendcnts[size-1] += add_to_last;

    recvcount = sendcnts[rank];
    recvbuf = malloc(sendcnts[rank] * sizeof(uint8_t));
    if(!recvbuf) {
        fprintf(stderr, "Error: malloc can't allocate memory\n");
        mpiabort(-1);
    }

    MPI_Scatterv(rank == 0 ? img->data : NULL, sendcnts, displs, MPI_UNSIGNED_CHAR,
            recvbuf, recvcount, MPI_UNSIGNED_CHAR, 0, MPI_COMM_WORLD);

#ifdef BENCHMARK
    gettimeofday(&tBoundsStart, NULL);
#endif

    /**
     * In case of an RGB color image, the normalization happens ONLY on the
     * channel V, so the reduction is performed on single bytes...
     */
    image_get_bounds(header, recvcount, recvbuf, &min, &max);

#ifdef BENCHMARK
    gettimeofday(&tBoundsEnd, NULL);
#endif

    MPI_Allreduce(MPI_IN_PLACE, &min, 1, MPI_INT, MPI_MIN, MPI_COMM_WORLD);
    MPI_Allreduce(MPI_IN_PLACE, &max, 1, MPI_INT, MPI_MAX, MPI_COMM_WORLD);

#ifdef BENCHMARK
    gettimeofday(&tNormStart, NULL);
#endif

    image_normalize(header, recvcount, recvbuf, min, max, newMin, newMax);

#ifdef BENCHMARK
    gettimeofday(&tNormEnd, NULL);
#endif

    MPI_Gatherv(recvbuf, recvcount, MPI_UNSIGNED_CHAR, rank == 0 ? img->data : NULL,
            sendcnts, displs, MPI_UNSIGNED_CHAR, 0, MPI_COMM_WORLD);

#ifdef BENCHMARK
    gettimeofday(&tSaveStart, NULL);
#endif
    if(rank == 0)
    {
        img_error_t err;
        if((err = image_write(outfname, *img)) != OK)
            fprintf(stderr, "Error: %s. Output image not generated.\n", error_string(err));
        image_free(img);
    }
#ifdef BENCHMARK
    gettimeofday(&tSaveEnd, NULL);
#endif

    free(sendcnts);
    free(displs);
    free(header);
    MPI_Finalize();

#ifdef BENCHMARK
    gettimeofday(&tEnd, NULL);
    print_duration(tStart, tEnd, "total");
    print_duration(tBoundsStart, tBoundsEnd, "bounds");
    print_duration(tNormStart, tNormEnd, "normalization");
    print_duration(tOpenStart, tOpenEnd, "open");
    print_duration(tSaveStart, tSaveEnd, "save");
#endif

    return EXIT_SUCCESS;
}

