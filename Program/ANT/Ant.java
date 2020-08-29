package Program.ANT;
/*
Ant colony
*/

import java.util.*;
import java.lang.Math;
import Program.TSP.Solution;
import Program.TSP.TSP;

public class Ant {
	
	/*
	visited works 1...nCity
	pheromone and city number are drifted by 1
	path includes values from 1..nCity
	*/
	
	
	public static class ant{
		
		 double weight;
		
		 int cityIndex;
		
		//there is no city 0, cities go from 1...N
		 int[] path;
		
		//visited[0] represents city 1
		 boolean[] visited;
		
		public ant(int n){
			//n represents the number of cities
			this.path = new int[n];
			this.visited = new boolean[n];
			this.cityIndex=0;
			this.weight=0;
			for(int i = 0 ; i < n ; i++){
				this.visited[i]=false;
			}
			
		}
		
		public  boolean hasVisited(int c){
			return this.visited[c-1];
		}
		
		public  boolean hasTransition(int c1, int c2){
			if(this.path.length>cityIndex){
				throw new java.lang.Error("Ant has not visited all cities and we're asking if it has a transition");
			}
			for(int i = 1 ; i < this.path.length ; i++){
				if(( this.path[i-1]==c1 && this.path[i]==c2 ) || ( this.path[i-1]==c2 && this.path[i]==c1 )){
					return true;
				}
			}
			if(( this.path[this.path.length-1]==c1 && this.path[0]==c2 ) || ( this.path[this.path.length-1]==c2 && this.path[0]==c1 )){
				return true;
			}
			return false;
		}
		
		public  int getLatestCity(){
			if(this.cityIndex>0){
				return this.path[this.cityIndex-1];
			}else{
				return -1;
			}
		}
		
		//As you can see from this the weight of the solution is automatically added when we add a city to the ants path
		public  int addCity(int n, TSP tsp){
			//check if we can add city
			if(this.visited[n-1] || this.cityIndex>=this.path.length){
				return -1;
			}else{
				//add the city to the path
				this.path[this.cityIndex]=n;
				//add the weight of the ant
				if(this.cityIndex==this.path.length-1){//last city
					this.weight+=tsp.getDistance(this.path[this.cityIndex], path[0]);
				}
				//every city but the first
				if(this.cityIndex>0){
					this.weight+=tsp.getDistance(this.path[this.cityIndex-1], this.path[this.cityIndex]);
				}
				//set visited to true
				this.visited[n-1]=true;
				//add to the length
				this.cityIndex++;
				return 1;
			}
		}
		
	}
	
	/*
	Parameters:
	tsp 		- the TSP
	numAnts 	- the number of ants
	numIter 	- the number of iterations
	p 			- the evaporation factor (0~1)
	alpha 		- the weight of pheromone on the city selection for each ant
	beta 		- the weight of distance on the city selection for each ant
	Q 			- a constant used to calculate how much pheromone is added considering the solution of the ant to an arc (1 default)
	start		- the starting value of the pheromone table
	*/
	public static Solution AntColony(TSP tsp, int numAnts, int numIter, double p, double alpha, double beta, double Q, double start){
		
		//pheromone matrix
		double[][] pheromone;
		
		//the ants
		ant[] ants;
		
		//the best solution that we'll return
		Solution bSol = new Solution(tsp.howManyCities());
		
		//the best ant
		ant best=null;
		
		//the best ant in a given iteration
		ant curBest;
		
		//the extra pheromone added to each edge is calculated with this variable
		double addPher;
		
		//the random chance of each ant to visit a city
		double r;
		
		//the bottom part of the equation for transition rule
		double bot;
		
		//the current range that a city has to be visited
		double cur;
		
		//the next citiy an ant should visit
		int next;
		
		//we'll make a variable whic holds how many cities the TSP has
		int nCity = tsp.howManyCities();
		
		//we'll need a random variable
		Random rand = new Random();
		
		//we'll initialize the pheromone matrix (if pheromone is 0 that would cause problems in the random transition rule as it would give 0/0)
		pheromone = new double[nCity][nCity];
		for(int i = 0 ; i < nCity ; i++){
			for(int j = 0 ; j < nCity ; j++){
				pheromone[i][j]=start;
			}
		}
		
		//we'll initialize the ants
		ants = new ant[numAnts];
		
		//--------Main loop--------
		for(int it = 0 ; it < numIter ; it++){
			
			
			//first we create ants and give them their first city
			for(int i = 0 ; i < numAnts; i++){
				ants[i] = new ant(nCity);
				ants[i].addCity(rand.nextInt(nCity)+1, tsp);
			}
			
			
			//loop that lets every ant visit every city
			for(int a = 0 ; a < nCity-1 ; a++){
				
				
				//now we'll go through each ant
				for(int i = 0 ; i < numAnts; i++){
					//we get a random number for the transition probability
					r=Math.random();
					
					//reset the bot, cur and the next city of the ant
					cur=0;
					bot=0;
					next=0;
					
					//we figure out bot (which is the bottom part of the transition equation)
					for(int x = 1 ; x <= nCity ; x++){
						
						//if the ant can visit a city x+1 (city 0 does not exist)
						if(!ants[i].hasVisited(x)){
							
							//we add the pheromone an dlength of a given city that can be visited to the sum
							bot+=Math.pow(pheromone[ (ants[i].getLatestCity())-1 ][x-1], alpha) + Math.pow(1/(tsp.getDistance(ants[i].getLatestCity(), x)), beta);
						}
						
					}
					
					//here we figure out which city should be visited
					//we go through all the cities and stop whenever we reached the cities percentage, cities are orderd from 1 to nCity (not avilable cities are obviously not represented)
					for(next = 1 ; next <= nCity && cur<r ; next++){
						
						//if the ant can visit a city
						if(!ants[i].hasVisited(next)){
							
							//we add the percentages to the sum of them
							cur+=( Math.pow(pheromone[ (ants[i].getLatestCity())-1 ][next-1], alpha) + Math.pow(1/(tsp.getDistance(ants[i].getLatestCity(), next)), beta) ) / bot;
						}
					}
					
					//we have to reduce next by 1 since its actual city -> index conversion
					next--;
					
					//the loop above will stop with the next city to visit for ant i
					//now we just add the city to the ant, if there is an error we'll print out the information in this IF statment
					if(ants[i].addCity(next, tsp)==-1){
						for(int ab = 0 ; ab < ants[i].cityIndex; ab++){
							System.out.print(ants[i].path[ab] + " ");
						}
						System.out.println("\n" + next + "\n");
						
						for(int ab = 0 ; ab < nCity ; ab++){
							System.out.print(ants[i].hasVisited(ab+1) + " ");
						}
						System.out.println("\na is " + a +  " \n");
						throw new java.lang.Error("Added city in ACO when the city was already in the list of visited cities for the given ant");
					}
					//LOCAL_1 after every ant step
				}
				//LOCAL_2 when every ant has done a step
			}
			//Every ant has just finished their path
			
			//global rule here, meaning evaporation and extra pheromone
			
			//we'll calculate the added pheromone for each connection and set the new pheromone
			for(int x = 0 ; x < nCity ; x++){
				
				for(int y = x+1 ; y < nCity ; y++){
					//reset the added pheromone
					addPher=0;
					//we go through all the ants and calculate the added pheromone
					for(int i=0 ; i < numAnts ; i++){
						//if an ant has visited the cities we add to the pheromone change
						if(ants[i].hasTransition(x+1,y+1)){
							
							addPher+=(Q/ants[i].weight);
						}
					}
					
					//evaporate and add pheromone change
					pheromone[x][y]=p*pheromone[x][y] + addPher;
					
					//This limits the upper level of pheromone if we wanted it to (currently set to 1.0)
					/*if(pheromone[x][y]>=1.0){
						pheromone[x][y]=1.0;
					}*/
					
					//Since its symetrical
					pheromone[y][x]=pheromone[x][y];
				}
			}
			
			//reset the current best to the first ant
			curBest=ants[0];
			
			for(int i = 1 ; i < numAnts ; i++){
				//check if given ant is better the current best in the iteration
				if(curBest.weight > ants[i].weight){
					curBest=ants[i];
					
				}
			}
			
			//check if the current ant is better then the best ant found till now
			if(best==null || best.weight > curBest.weight){
				best=curBest;
				bSol = new Solution(curBest.path);
			}
			
		}
		
		
		return bSol;
		
	}
	
	//Extra prints for those who care (left in from the development phase)
	private static void printDisMatrix(TSP tsp){
		for(int x = 0 ; x < tsp.howManyCities() ; x++){
			for(int y = 0 ; y < tsp.howManyCities() ; y++){
				System.out.print(String.format("%4.1f ", (1/tsp.edgeWeight[x][y])));
			}
			System.out.print("\n");
		}
	}
	
	//Extra prints for those who care (left in from the development phase)
	private static void printPhMatrix(int n, double[][] pheromone){
		for(int x = 0 ; x < n ; x++){
			for(int y = 0 ; y < n ; y++){
				System.out.print(String.format("%5.2f ", pheromone[x][y]));
			}
			System.out.print("\n");
		}
	}
	
}
