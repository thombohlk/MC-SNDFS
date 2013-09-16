README file for the 2011-2012 Concurrency and Multithreading assignment



File structure

bin/ndfs
    script to start the application
build.xml
    ant script
doc
    javadoc for the graph package
input
    input files
lib 
    jar files needed for the application
README.txt
    this file
src
    The source code for this project.
    


File structure source code
    driver/Main.java
	The Main class that drives the application. 
    ndfs/nndfs
	The package for the sequential nndfs version.
    ndfs/mcndfs_1_naive
	The package for the naive multi-core version. Initially, this is a copy
	of the ndfs/nndfs package.



Building the application

Run ant from the root directory (ndfs)
$ ant



Running the application

From the root directory run 
$ bin/ndfs

This will give the usage. An example of a correct run of the application is:
$ bin/ndfs input/accept-cycle.prom seq 1
The first argument is the input file, the second which version, the third
argument is the number of workers (threads).



Reading the documentation of the graph package.

Open doc/index.html in a browser.



Programming the mcndfs_1_naive version

Your first programming task is to create a multi-core NDFS version that uses
a naive approach to solve the concurrency problems. Initially, the package
ndfs.mcndfs_1_naive contains the same code as the package ndfs.nndfs. Modify
the source to create the multi-core version and add the version to the
driver.Main.dispatch() method. 



Programming other multi-core versions (master students)

The master students then continue and create more multicore versions in the
same manner. Please add packages mcndfs_2_<your_name_here>,
mcndfs_3_<different_name>, etc and add them to ndfs.NDFSFactory and the
driver.Main.dispatch() methods.



Warnings

Please make sure that every process only runs one version as the timing
information you get may become skewed otherwise because of unexpected
optimizations and garbage collection performed by Java.


