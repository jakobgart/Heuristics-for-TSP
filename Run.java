package Program;
/*
Test file
*/
import java.io.*;
import java.util.*;
import java.time.*;
import java.text.*;
import Program.TSP.TSP;
import Program.TSP.Set;
import Program.TSP.NearestNeighbor;
import Program.TSP.Solution;
import Program.TSP.SolutionDLL;
import Program.LS.*;
import Program.SA.*;
import Program.ANT.*;
import Program.WP.*;
import Program.PSO.*;

/*
time stuff:
LK and NN are fine, they return the solution and it has the time it was created in there, meaning if you run it multiple times (which is how its made currently) if you hold the best solution here it should hold the time the solution was made

SA - has the temperature when the given solution was created (the best one) (this one is slightly rigged due to diversification at the start)
Ant - has the time of the end of a given iteration in which the best ant was found
Wolf - should be a decent indicator when a wolf was created in the algorithm
Particle - should be fine

*/

/*
ant:
-So Q should be lower or else it too strongly influences the chances to go to a city
-Start kinda dosent matter as long as Q is low, if Q is high start should be 0 so it can build up i suppose instead of already dominating distance at the first step
-increasing the amount of ants is always better it seems like, but a good compromise is 10~15 (seems like it keeps going down a decent amount every ant we get, more linear then exponential form my results, might test again)
- a decent evaporation rate seems to be 0.75
- iteration obv has upper limit and seems extreamly problem dependant so i wont minmax it

-alpha and beta seem to work like this: equal is fine at lower levels (1~10), but the best solutions come when alpha is higher then beta, so far:  10 & 7 and 20 & 10 seem fine. Also it seems like both have to be at a decent size
20 & 7 seems the best so far, 40 & 15 is fine too
winnares are 20-10, 30-10 and 10-7, and the final winner is 10-7 due to its low level (less calculations) and its good results


wolf:
- W seems like the best is W>0.5
- wolfs can be around 15~20, iters can be around 200~400 (for the bayg29, need to test on others) to get decent results
- need to test on bigger instances, but seems like not allowing leader to stagnate is a good decision (eps when W is high)

*/
public class Run {
	
	public static void main(String[] args){
		
		
		/*
		How test:
		2opt & 3opt total best 	- untill failure
		2opt & 3opt first best 	- untill failure but search 3/2*howManyCities()
		LK 						- untill 10 failures in a row with total backtracking and 10x on the first
		SA2opt & SA3opt 		- lower=0.99, iter=1400, iterAtTemp=10000
		Ant 					- ants=20, iter=1500, factor=0.95, A=2.0, B=2.0, Q=1.0, start=0.0 (only do first few)
		PSO2opt & PSOLK			- Particle=25, Iter=2000, stepF=0.6, inside=30, other=0.9, 0.9999, 0.4, 1.00063, 0.03, 1.003
		WP						- wolfs=80, iter=10000, W=0.75, changeLead=100, howManyTimes=300
		*/
		
		
		
		String path = "TSP\\TSPLIB\\";
		String resultPath = "Program\\Results\\";
		
		TSP tsp[] = new TSP[7];
		try{
			tsp[0] = new TSP(new FileReader(path + "bayg29.tsp"));
			tsp[1] = new TSP(new FileReader(path + "brg180.tsp"));
			tsp[2] = new TSP(new FileReader(path + "fl417.tsp"));
			tsp[3] = new TSP(new FileReader(path + "si1032.tsp"));
			tsp[4] = new TSP(new FileReader(path + "u1432.tsp"));
			tsp[5] = new TSP(new FileReader(path + "rl1889.tsp"));
			//tsp[6] = new TSP(new FileReader(path + "d2103.tsp"));
			
			//Represents which algorithms we run 
			boolean[] algo=new boolean[11];
			
			algo[0]=true;//2opt first best
			algo[1]=true;//2opt total best
			algo[2]=true;//3opt first best
			algo[3]=true;//3opt total best
			algo[4]=true;//LK
			algo[5]=true;//SA2opt
			algo[6]=true;//SA3opt
			algo[7]=true;//Ant
			algo[8]=true;//PSO2opt
			algo[9]=true;//PSOLK
			algo[10]=true;//WP
			
			//Just a string array to make things simpler and less repetitive
			String[] algoNames = {"2-opt first best", "2-opt total best", "3-opt first best", "3-opt total best", "Lin-Kernighan", "Simulated Annealing 2-opt", "Simulated Annealing 3-opt", "Ant colony", "Particle swarm 2-opt", "Particle swarm LK", "Wolf pack"};
			
			//Names of result text files
			String[] algoFileName = {"2optFB", "2optTB", "3optFB", "3optTB", "LK", "SA2opt", "SA3opt", "Ant", "PSO2opt", "PSOLK", "WP"};
			
			//How many TSPs will be tested (from the first one to the last. for example: 3 will test bayg29, brg180 and fl417)
			int numOfTSP=6;
			
			//How many times will the algorithm be run
			int howMany=10;
			
			//Staring time of an algorithm
			long StartTime;
			
			//The total time required for each run
			long TotalTime;
			
			//The time when the solution was most recently changed in the algorithm
			long LastChangeTime;
			
			//The current time, we use to get the 2nd time in here so we dont get it twice
			long CurrentTime;
			
			//the avrage values over all runs
			double AvrageValue;
			
			//Starting solution if its ever needed
			Solution start = null;
			
			//The output
			Solution result = null;
			
			//The lin kernighan strucutre, need this to run it
			LK linK;
			
			//File writer for our results
			FileWriter resultWriter;
			PrintWriter rWrite;
			
			//File writer for the sequence if we'll need it
			FileWriter solutionWriter;
			PrintWriter sWrite;
			
			
			
			
			//------------------------------------------2 OPT------------------------------------------
			
			
			
			//2opt first
			if(algo[0]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[0] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[0] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s(3*cities/2):\n", algoNames[0]);
				sWrite.printf("%s(3*cities/2):\n", algoNames[0]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[0]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//get the starting solution
						start = new Solution(tsp[whichTSP].howManyCities());
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						for(boolean works = true; works ;){
							//Call the first best
							result = kopt.firstBest2Opt(start, tsp[whichTSP], 3*tsp[whichTSP].howManyCities()/2);
							
							//figure out if we got a better solution or not
							if(result.equals(start)){
								//if not end
								works=false;
							}else{
								//if we did continue
								start=result;
							}
							
						}
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[0]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			
			
			//2opt total
			if(algo[1]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[1] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[1] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s:\n", algoNames[1]);
				sWrite.printf("%s:\n", algoNames[1]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[1]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//get the starting solution
						start = new Solution(tsp[whichTSP].howManyCities());
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						for(boolean works = true; works ;){
							//Call the total best
							result = kopt.totalBest2Opt(start, tsp[whichTSP]);
							
							//figure out if we got a better solution or not
							if(result.equals(start)){
								//if not end
								works=false;
							}else{
								//if we did continue
								start=result;
							}
							
						}
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[1]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			
			
			//------------------------------------------3 OPT------------------------------------------
			
			
			
			//3opt first
			if(algo[2]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[2] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[2] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s(3*cities/2):\n", algoNames[2]);
				sWrite.printf("%s(3*cities/2):\n", algoNames[2]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[2]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//get the starting solution
						start = new Solution(tsp[whichTSP].howManyCities());
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						for(boolean works = true; works ;){
							//Call the first best
							result = kopt.firstBest3Opt(start, tsp[whichTSP], 3*tsp[whichTSP].howManyCities()/2);
							
							//figure out if we got a better solution or not
							if(result.equals(start)){
								//if not end
								works=false;
							}else{
								//if we did continue
								start=result;
							}
							
						}
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[2]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			
			
			//3opt total
			if(algo[3]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[3] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[3] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s:\n", algoNames[3]);
				sWrite.printf("%s:\n", algoNames[3]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[3]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//get the starting solution
						start = new Solution(tsp[whichTSP].howManyCities());
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						for(boolean works = true; works ;){
							//Call the total best
							result = kopt.totalBest3Opt(start, tsp[whichTSP]);
							
							//figure out if we got a better solution or not
							if(result.equals(start)){
								//if not end
								works=false;
							}else{
								//if we did continue
								start=result;
							}
							
						}
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[3]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			
			
			//------------------------------------------Lin-Kernighan------------------------------------------
			
			
			
			
			if(algo[4]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[4] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[4] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s(15, 10):\n", algoNames[4]);
				sWrite.printf("%s(15, 10):\n", algoNames[4]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[4]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					//create the lin Kernighan structure
					linK = new LK(tsp[whichTSP], 10);
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//get the starting solution
						start = new Solution(tsp[whichTSP].howManyCities());
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						for(int failures = 0; failures < 10 ;){
							//Call the first best
							result = linK.Lin_Kernighan(start, 15, 10);
							
							//figure out if we got a better solution or not
							if(result.equals(start)){
								//if not increase failures
								failures++;
							}else{
								//if we did replace starting solution
								start=result;
								//and reset failures
								failures=0;
							}
							
						}
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[4]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			
			
			//------------------------------------------Simulated Annealing------------------------------------------
			
			
			
			//SA 2opt
			if(algo[5]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[5] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[5] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s(0.99, 1400, 10000):\n", algoNames[5]);
				sWrite.printf("%s(0.99, 1400, 10000):\n", algoNames[5]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[5]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//get the starting solution
						start = new Solution(tsp[whichTSP].howManyCities());
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						//run the algorithm
						result=SA.SA2Opt(tsp[whichTSP], start, 0.99, 1400, 10000);
						
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[5]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			//SA 3opt
			if(algo[6]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[6] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[6] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s(0.99, 1400, 10000):\n", algoNames[6]);
				sWrite.printf("%s(0.99, 1400, 10000):\n", algoNames[6]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[6]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//get the starting solution
						start = new Solution(tsp[whichTSP].howManyCities());
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						//run the algorithm
						result=SA.SA3Opt(tsp[whichTSP], start, 0.99, 1400, 10000);
						
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[6]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			
			
			//------------------------------------------Ant Colony------------------------------------------
			
			
			
			if(algo[7]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[7] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[7] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s(20, 1500, 0.95, 2.0, 2.0, 1.0, 0.0):\n", algoNames[7]);
				sWrite.printf("%s(20, 1500, 0.95, 2.0, 2.0, 1.0, 0.0):\n", algoNames[7]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[7]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						//run the algorithm
						result=Ant.AntColony(tsp[whichTSP], 20, 1500, 0.95, 2.0, 2.0, 1.0, 0.0);
						
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[7]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			
			
			//------------------------------------------Particle Swarm------------------------------------------
			
			
			
			//PSO 2opt
			if(algo[8]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[8] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[8] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s(25, 2000, 0.6, 30, 0.68, 0.999, 0.3, 1.0006, 0.02, 1.002):\n", algoNames[8]);
				sWrite.printf("%s(25, 2000, 0.6, 30, 0.68, 0.999, 0.3, 1.0006, 0.02, 1.002):\n", algoNames[8]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[8]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						//run the algorithm
						result=PSO.ParticleSwarm2Opt(tsp[whichTSP], 25, 2000, 0.6, 30, 0.68, 0.999, 0.3, 1.0006, 0.02, 1.002);
						
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[8]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			//PSO LK
			if(algo[9]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[9] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[9] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s(25, 2000, 0.6, 30, 0.68, 0.999, 0.3, 1.0006, 0.02, 1.002):\n", algoNames[9]);
				sWrite.printf("%s(25, 2000, 0.6, 30, 0.68, 0.999, 0.3, 1.0006, 0.02, 1.002):\n", algoNames[9]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[9]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						//run the algorithm
						result=PSO.ParticleSwarmLK(tsp[whichTSP], 25, 2000, 0.6, 30, 0.68, 0.999, 0.3, 1.0006, 0.02, 1.002);
						
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[9]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			
			
			//------------------------------------------Wolf Pack------------------------------------------
			
			
			if(algo[10]){
				
				//open file
				resultWriter = new FileWriter(resultPath + algoFileName[10] + ".txt", true);
				solutionWriter = new FileWriter(resultPath + algoFileName[10] + "Solution.txt", true);
				
				//create time writer
				rWrite = new PrintWriter(resultWriter);
				sWrite = new PrintWriter(solutionWriter);
				
				//Print out which problem
				rWrite.printf("%s(80, 10000, 0.75, 100, 300):\n", algoNames[10]);
				sWrite.printf("%s(80, 10000, 0.75, 100, 300):\n", algoNames[10]);
				
				//HELP PRINT
				System.out.printf("Starting algorithm %s ***************\n", algoNames[10]);
				
				//loop through all TSPs we'll use
				for(int whichTSP = 0 ; whichTSP < numOfTSP ; whichTSP++){
					
					//HELP PRINT
					System.out.printf("Starting algorithm on TSP %s ----------\n", tsp[whichTSP].name);
					
					//Print out which TSP
					rWrite.printf("Problem %s\n", tsp[whichTSP].name);
					sWrite.printf("Problem %s\n", tsp[whichTSP].name);
					
					//Reset the time total time and last change time
					TotalTime=0;
					LastChangeTime=0;
					//reset the avrage values
					AvrageValue=0;
					
					
					//loop that goes through how many times we repeat an algorith,
					for(int repeat = 0 ; repeat < howMany ; repeat++){
						
						//HELP PRINT
						System.out.printf("Starting run %d/%d\n", (repeat+1), howMany);
						
						//ALGORITHM STARTS HERE
						
						//Get the time
						StartTime=System.currentTimeMillis();
						
						//run the algorithm
						result=WP.WolfPack(tsp[whichTSP], 80, 10000, 0.75, 100, 300);
						
						
						//ALGORITHM ENDS HERE
						
						//get the current time
						CurrentTime=System.currentTimeMillis();
						
						//HELP PRINT
						System.out.printf("Finished run with time: %d seconds\n", (CurrentTime-StartTime)/1000);
						
						//Print the information regarding the current run
						rWrite.printf("%d. %.1f value, %d ms for best, %d total time\n", (repeat+1), tsp[whichTSP].getFitness(result), (result.timeMS-StartTime), (CurrentTime-StartTime));
						sWrite.printf("%d. %s\n", (repeat+1), result);
						
						//add to the avrages
						TotalTime+=(CurrentTime-StartTime);
						LastChangeTime+=(result.timeMS-StartTime);
						AvrageValue+=tsp[whichTSP].getFitness(result);
						
					}
					
					//HELP PRINT
					System.out.printf("Finished %s ----------\n", tsp[whichTSP].name);
					
					//Print avrages
					rWrite.printf("Avrage solution: %.1f\nAvrage time: %d\nAvrage time for best: %d\n", AvrageValue/howMany, TotalTime/howMany, LastChangeTime/howMany);
					
					//Print new line
					rWrite.print("\n");
					sWrite.print("\n");
					
				}
				
				//HELP PRINT
				System.out.printf("Finished algorithm %s ***************\n", algoNames[10]);
				
				//Close file writer
				rWrite.close();
				sWrite.close();
			}
			
			
			
		}catch(Exception e){
			System.out.println("Error: " + e + "\n" );
			e.printStackTrace();
		}	
		
		
	}
	
	
}
