package Program.TSP;
/*
This class holds a nearest neighbors of cities in a given TSP
Generally NN can be seen as a graph, but we're saving it as two arrays.
*/
import java.util.*;
import Program.TSP.Solution;
import Program.TSP.TSP;

public class NearestNeighbor{
	
	/*
	this double array holds the nearest neighbors of all cities
	
	It works like this
	cityNN[0][0] will give the city thats the 1st city's closest nearest neighbor
	cityNN[5][0] will give the city thats the 6th city's closest nearest neighbor
	cityNN[i][j] will give the city thats the (i+1)-th city's (j+1)-th nearest neighbor
	
	distanceNN[i][j] will return the distance between (i+1)-th city's (j+1)-th nearest neighbor
	*/
	public int[][] cityNN;
	
	public double[][] distanceNN;
	
	//how many NN we have
	public int howLong;
	
	/*
	In order to create a NN "graph" we'll need to know the distances between cities and also how big the NN "graph" should be.
	*/
	public NearestNeighbor(TSP tsp, int howMany){

		//set howLong
		this.howLong=howMany;
		
		//init the arrays
		this.cityNN = new int[tsp.howManyCities()][howMany];
		this.distanceNN = new double[tsp.howManyCities()][howMany];
		
		//fill the distance array 
		for(int i1 = 0 ; i1 < tsp.howManyCities() ; i1++){
			for(int i2 = 0 ; i2 < howMany ; i2++){
				this.distanceNN[i1][i2]=-1;
			}
		}
		
		//extra variable which holds the distance between two cities so we dont have to call getDistance multiple times
		double dist;
		
		//this loop goes through all the cities
		for(int i1 = 1 ; i1 <= tsp.howManyCities() ; i1++){
			
			//this loop checks all the cities and checks the distance between the previously selected city and this one
			for(int i2 = 1 ; i2 <= tsp.howManyCities() ; i2++){
				
				
				//we will ignore the connection to the city itself
				if(i1!=i2){
					
					//get the distance between the currently selected cities
					dist = tsp.getDistance(i1, i2);
					
					//check where in the NN the current distance fits and if it changed the NN list update it accordingly
					for(int n = howMany-1 ; n >= 0 && (dist < this.distanceNN[i1-1][n] || this.distanceNN[i1-1][n]==-1 ) ; n--){
						
						//if n is less then howMany-1 we push down the solution
						if(n<howMany-1){
							this.distanceNN[i1-1][n+1]=this.distanceNN[i1-1][n];
							this.cityNN[i1-1][n+1]=this.cityNN[i1-1][n];
						}
						
						//put the current distance in the current slot
						this.cityNN[i1-1][n]=i2;
						this.distanceNN[i1-1][n]=dist;
						
						
					}
					
				}
				
				
			}
			
		}
		
	}
	
	/*
	Return the city which is the i-th nearest neighbor of the given city
	We'll allow it to throw error when outside the bounds
	*/
	public int getCityNN(int c, int i){
		return this.cityNN[c-1][i-1];
	}
	
	/*
	Returns the distance between the given city and its i-th nearest neighbor
	*/
	public double getDistanceNN(int c, int i){
		return this.distanceNN[c-1][i-1];
	}
	
	//returns the length of the NN list
	public int length(){
		return this.howLong;
	}
	
	//City output
	public String toString(){
		String ret="";
		for(int i = 0 ; i < this.cityNN.length ; i++){
			ret+=String.format("%-3d: ", i+1);
			for(int n = 0 ; n < this.howLong ; n++){
				ret+=String.format("%2d, ", this.cityNN[i][n]);
			}
			ret+="\n";
		}
		return ret;
	}
	
	//Distance output
	public String toStringDist(){
		String ret="";
		for(int i = 0 ; i < this.cityNN.length ; i++){
			ret+=String.format("%-3d: ", i+1);
			for(int n = 0 ; n < this.howLong ; n++){
				ret+=String.format("%5.1f, ", this.distanceNN[i][n]);
			}
			ret+="\n";
		}
		return ret;
	}
	
}