export LD_LIBRARY_PATH=/home/marcello/mpi/lib

for nth in {1,2,4,8}; do
	filename=data/earth-huge/runs_omp_${nth}thread.txt
	rm $filename
	export OMP_NUM_THREADS=$nth
	for run in {1..20}; do
		/usr/bin/time /home/marcello/mpi/bin/mpirun -np 1 ./imgnormalize -m 20 -M 50 -f $1 -o /tmp/out.ppm 2&>> $filename
	done
done

for nth in {1,2,4,8}; do
	echo "" > riepilogo_omp.txt
	echo "Threads: ${nth}" >> riepilogo_omp.txt
	echo "" >> riepilogo_omp.txt
	cat data/earth-huge/runs_omp_${nth}thread.txt >> riepilogo_omp.txt
done
cat riepilogo_omp.txt | mailx -s "script.sh: i risultati del test (OpenMP) sono pronti" marcello.pogliani@gmail.com

for nth in {1,2,4,8}; do
	filename=data/earth-huge/runs_MPI_${nth}thread.txt
	rm $filename
	export OMP_NUM_THREADS=1
	for run in {1..20}; do
		/usr/bin/time /home/marcello/mpi/bin/mpirun -np $nth ./imgnormalize -m 20 -M 50 -f $1 -o /tmp/out.ppm 2&>> $filename
	done
done

for nth in {1,2,4,8}; do
	echo "" > riepilogo_MPI.txt
	echo "Threads: ${nth}" >> riepilogo_MPI.txt
	echo "" >> riepilogo_MPI.txt
	cat data/earth-huge/runs_MPI_${nth}thread.txt >> riepilogo_MPI.txt
done
cat riepilogo_MPI.txt | mailx -s "script.sh: i risultati del test (OpenMP) sono pronti" marcello.pogliani@gmail.com
