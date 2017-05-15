# PoWorK
Implementation of Indistinguishable Proof of Work or Knowledge protocol.
Use the Proof of Work and Proof of Knowledge test cases to check the PoW and PoK generation and verification. The execution is quite verbose and prints the steps in the generation annd verification of the proofs.

To run a full set-up kindly change the following lines in the code,
Miner.java: lines 187 and 208
	Kindly enter the IP address of the system in use.
MiningManager.java: line 22
	Kindly enter the IP of the system in use.


Run two instances of the application on your system to run a two note network. The application has been programmed to run nodes at two different ports in the same system to simulate the network.
For one of the nodes make the following changes to the MiningManager.java file,
	uncomment lines 38 to 41.

The another miner will wait for the genesis block to be generated and will start generating blocks on top of it.


Miner type can be changed from MiningManager.java line number 13.
	Change the instantiation to type.KNOWLEDGE or type.WORK as need be.
