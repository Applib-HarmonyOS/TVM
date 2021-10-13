cmake_minimum_required(VERSION 3.4.1)
project(TVMLib)

file(GLOB jni "*.cc")
file(GLOB runtime "./runtime/*.cc")
file(GLOB runtimegraph "./runtime/graph_executor/*.cc")
#Set header file search path (header files in the same path as this txt do not need to be set), optional
include_directories(tvm/include
                    tvm/3rdparty/dlpack/include
                    tvm/3rdparty/dmlc-core/include
					tvm/3rdparty/compiler-rt/
					)

set(CMAKE_THREAD_PREFER_PTHREAD ON)
set(THREADS_PREFER_PTHREAD_FLAG ON)
find_package(Threads REQUIRED)

if(CMAKE_USE_PTHREADS_INIT)
        set(HAVE_PTHREADS TRUE)
        add_definitions(-DHAVE_PTHREADS)
endif()

#link_directories(lib/)
#set(CMAKE_SHARED_LINKER_FLAGS "-Wl,--export-all-symbols")
add_library(tvm4j_runtime_packed SHARED ${jni} ${runtime} ${runtimegraph})
#add_library(tvm4j_runtime_packed SHARED JaiHanuman.cpp)
#configure_shared_stl("llvm-libc++" "c++_shared")
#include(stl.cmake)

#link_directories(lib/)

target_link_libraries(tvm4j_runtime_packed libhilog_ndk.z.so)
#set_target_properties(tvm_packed_runtime PROPERTIES LINK_FLAGS "-Wl,--whole-archive")
#set_target_properties(tvm4j_runtime_packed PROPERTIES LINK_FLAGS "-Wl,--whole-archive")