cmake_minimum_required (VERSION 2.6)
project(imgnormalize)

file(GLOB imgnormalize_C "*.c")
file(GLOB imgnormalize_CPP "*.cc")

find_package(MPI REQUIRED)
find_package(OpenMP REQUIRED)

set(CMAKE_CXX_FLAGS "${CMAKE_C_FLAGS} -Wall -pedantic")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -O0 -g")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -O3")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -pedantic -std=c++11")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -O0 -g")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3")

if(OPENMP_FOUND)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${OpenMP_CXX_FLAGS}")
    set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} ${OpenMP_EXE_LINKER_FLAGS}")
endif()

include_directories("${PROJECT_BINARY_DIR} ${MPI_COMPILE_FLAGS}")

add_executable(imgnormalize ${imgnormalize_C} ${imgnormalize_CPP})
target_link_libraries(imgnormalize ${MPI_LIBRARIES})

if(UNIX)
    target_link_libraries(imgnormalize m)
endif()

if(MPI_COMPILE_FLAGS)
    set_target_properties(imgnormalize PROPERTIES COMPILE_FLAGS "${MPI_COMPILE_FLAGS}")
endif()

if(MPI_LINK_FLAGS)
    set_target_properties(imgnormalize PROPERTIES LINK_FLAGS "${MPI_LINK_FLAGS}")
endif()

