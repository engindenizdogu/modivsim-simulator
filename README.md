# modivsim-simulator
COMP416 - Project 3, Routing Algorithms

#### Setup
Set the compiler output first to run the project.

Create a folder called 'target' in the project directory.

In IntelliJ,
File -> Project Structure...

Within the 'Project' tab, set the destination for the 'Project Compiler Output' to the 'target' folder in the project folder. Then build and run.

**Node files**\
Node*.txt structure:\
<nodeID, (neighborId, linkCostToNeighbor, bandwidthToNeighbor), (...), (...)>

node0.txt example:\
0,(1,x,10),(2,3,15)

Cost x indicates a dynamic link. A dynamic link's cost changes each iteration with a probability of (0.5). Random cost
range lies between [0,10]

**Simulating flows**\
Flow file structure,
(Flow name, start, destination, file size)
(... additional flows ...)
     
flow.txt example:\
A,0,3,100\
B,0,3,200\
C,1,2,100