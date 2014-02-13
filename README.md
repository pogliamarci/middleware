Middleware Technologies - Project
=================================

Project for the [Middleware Technologies for Distributed
System](http://corsi.dei.polimi.it/distsys) course at Politecnico di Milano,
academic year 2013-2014.

Parallel image normalization with MPI & OpenMP
----------------------------------------------

Use MPI (+OpenMP) to implement a parallel version of the [image normalization
algorithm](http://en.wikipedia.org/wiki/Normalization_(image_processing\)). In
practice, this means calculating the min and max color values of the submitted
image (in parallel) and then perform the normalization (again in parallel). Use
the image format you prefer (e.g., "plain ppm").

The code has to be demonstrated using at least two physical machines connected
in a LAN (with or without additional virtual machnes to emulate a larger
cluster).

Running the code in a cluster

* Install the `ssh` daemon on all machines, and make sure all machines in the
  cluster can reach each other via `ssh`
* Create the same user on all machines (e.g., `mpiuser`) and generate a
  private-public key pair
* Deploy in all machines of the cluster all the public keys (put them in the
  `~/.ssh/authorized_keys`)
* Copy the executable of the project in the same folder on each machine
* On the cluster root, create a file (e.g., `~/.mpi_hostfile`) that contains a
  newline-separated list of the hostnames of all machines in the cluster
* Execute the program on the host machine, specifying `--hostfile
  <hostfile_path>' as an `mpirun` parameter. For example:
    mpirun -np <n> --hostfile <hostfile_path> ./imgnormalize -m <min> -M <max> -f <filename> -o <outputfilename>

If you are experiencing problems, check that the machines in the cluster are
not firewalled, and that each machine is able to connect via `ssh` to itself
and to any other machine.

Furthermore, it is advised that each machine run the same version of openMPI
and that it is installed in the same folder on each machine.
