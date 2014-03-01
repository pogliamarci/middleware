#!/bin/bash

# syntax: ./script.sh image_file output-dir

export LD_LIBRARY_PATH=/home/marcello/mpi/lib
dir=data/$2

for nth in {1,2,4,8}; do
	filename=$dir/runs_omp_${nth}thread.txt
	rm $filename
	export OMP_NUM_THREADS=$nth
	for run in {1..20}; do
		/usr/bin/time /home/marcello/mpi/bin/mpirun -np 1 ./imgnormalize -m 20 -M 50 -f $1 -o /tmp/out.ppm 2&>> $filename
	done
done

for nth in {1,2,4,8}; do
	filename=$dir/runs_MPI_${nth}thread.txt
	rm $filename
	export OMP_NUM_THREADS=1
	for run in {1..20}; do
		/usr/bin/time /home/marcello/mpi/bin/mpirun -np $nth ./imgnormalize -m 20 -M 50 -f $1 -o /tmp/out.ppm 2&>> $filename
	done
done