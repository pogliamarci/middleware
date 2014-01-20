Middleware Technologies - Project
=================================

Project for the [Middleware Technologies for Distributed System](http://corsi.dei.polimi.it/distsys) course at Politecnico di Milano, academic year 2013-2014.

Parallel image normalization with MPI & OpenMP
----------------------------------------------

Use MPI (+OpenMP) to implement a parallel version of the [image normalization algorithm](http://en.wikipedia.org/wiki/Normalization_(image_processing\)). In practice, this means calculating the min and max color values of the submitted image (in parallel) and then perform the normalization (again in parallel). Use the image format you prefer (e.g., "plain ppm").

The code has to be demonstrated using at least two physical machines connected in a LAN (with or without additional virtual machnes to emulate a larger cluster).

