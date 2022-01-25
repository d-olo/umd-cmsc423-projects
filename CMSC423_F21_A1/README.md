## Assignment 1: Random sampler and shortest common substring generator
This project will generate two Java executables, which are called using the bash commands `randsim` and `scsbler`. 

`randsim` reads a file in FASTA format and populates an output file with random reads from that genome. Additionally, it calculates statistics for the depth of the reads. This roughly simulates the process of randomly sampling reads from a genome. However, there is no potential for error being added as there would be in a real read.

`scsbler` implements the greedy algorithm for calculating the shortest common superstring. It implements a priority queue and several other techniques to skip unncessary comparisons, and run in less time than the expected quadratic runtime.
