Parallel image normalization
============================

Use MPI (+OpenMP) to implement a parallel version of the [image normalization
algorithm](http://en.wikipedia.org/wiki/Normalization_(image_processing\)). In
practice, this means calculating the min and max color values of the submitted
image (in parallel) and then perform the normalization (again in parallel). Use
the image format you prefer (e.g., "plain ppm").

The code has to be demonstrated using at least two physical machines connected
in a LAN (with or without additional virtual machnes to emulate a larger
cluster).

### Run the code locally

* Compile: `make`
* Run: `./imgnormalize -f <path> -m <min> -M <max> -o <outputpath>`

### Running the code in a cluster

* Install the `ssh` daemon on all machines, and make sure all machines in the
  cluster can reach each other via `ssh` (or at least that the root can
  reach all the machines in the cluster via `ssh`)
* Create the same user on all machines (e.g., `mpiuser`) and generate a
  private-public key pair
* Deploy in all machines of the cluster all the public keys (put them in the
  `~/.ssh/authorized_keys`)
* Copy the executable of the project in the same folder on each machine
* On the cluster root, create a file (e.g., `~/.mpi_hostfile`) that contains a
  newline-separated list of the hostnames of all machines in the cluster
* Execute the program on the host machine, specifying `--hostfile  <hostfile_path>`
  as a parameter for `mpirun`. For example:

      mpirun -np <n> --hostfile <hostfile_path> ./imgnormalize -m <min> -M <max> -f <filename> -o <outputfilename>

If you are experiencing problems, check that the machines in the cluster are
not firewalled.

Each machine must run the same version of OpenMPI, and the OpenMPI executables should be installed in the same folder on each machine. The project has been tested under GNU/Linux (Debian Wheezy and ArchLinux) and 
Mac OSX, using OpenMPI 1.6.4 compiled from sources.
