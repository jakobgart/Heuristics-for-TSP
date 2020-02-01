package Program.LS;
/*
k-opt

Solution is a synonim for tour.
*/
import java.util.*;
import Program.TSP.Solution;
import Program.TSP.TSP;


public class kopt {
	
	
	//-------------------------------------2 OPT-------------------------------------
	/*
	Searches randomly in the 2-opt neighborhood <searches> times in attempt to find a better solution.
	If a better solution is found during the run it is instantly returned.
	When the <searches> run out the current solution is returned.
	
	Params:
	s			- the solution
	tsp			- the TSP definition
	searches	- the number of searches
	
	Note:
	The same combination of edges could be checked multiple times since having an updating a backlog would by itself be too expensive.
	a=city1 and b=city2 is the same thing as a=city2 and b=city1, unlike in 3-opt lower due to having to be ordered. Should not change the function anyhow as the chance for us to get two specific edges is the same.
	*/
	public static Solution firstBest2Opt(Solution s, TSP tsp, int searches){
		
		/*
		a 2-opt move requres two edges (a1,a2) and (b1,b2). We remove (a1,a2) and (b1,b2) then add (a1,b1) and (b2,a2)
		a[0] represents <a1> and b[0] represents <b1>
		<a2> and <b2> are the cities which come after <a1> and <b1> respectivly
		*/
		int[] a = new int[2];
		int[] b = new int[2];
		
		Random rand = new Random();
		
		
		
		while(true){
			//get the random cities
			a[0]=rand.nextInt(s.howManyCities())+1;
			a[1]=s.getNextCity(a[0]);
			b[0]=rand.nextInt(s.howManyCities())+1;
			b[1]=s.getNextCity(b[0]);
			
			
			//checking if the move makes sense (the cities are different && not next to eachother)
			if( (a[0]!=b[0]) && (a[0]!=b[1] && b[0]!=a[1]) ){
				searches--;
				
				//check if the 2opt move is better then the current one 
				if(tsp.getDistance(a[0], b[0]) + tsp.getDistance(a[1], b[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]) < 0){
					
					//create a new solution, reverse it then return it
					Solution nSol = s.clone();
					//we check which city is first in the solution in order to reverse the correct pair of cities
					if(nSol.getIndexOfCity(a[0])<nSol.getIndexOfCity(b[0])){
						nSol.reverse(nSol.getIndexOfCity(a[1]), nSol.getIndexOfCity(b[0]));
					}else{
						nSol.reverse(nSol.getIndexOfCity(b[1]), nSol.getIndexOfCity(a[0]));
					}
					return nSol;
				}
				
			}
			
			if(searches<=0){
				//return null;
				return s;
			}
			
		}
		
		
		
	}
	
	
	/*
	Searches the whole 2-opt neighborhood of a solution and returns the best one.
	If no imporving one was found it returns the same solution
	
	Params:
	s	- the solution
	tsp	- the TSP
	
	Note:
	Unlike firstBest2Opt this serach goes through the list of cities based on their position in the current solution.
	*/
	public static Solution totalBest2Opt(Solution s, TSP tsp){
		
		
		//the best two edges
		int[] besta={-1,-1};
		int[] bestb={-1,-1};
		
		//the best change (the lesser the better, starts with the tour length)
		double bestChange=tsp.getFitness(s);
		
		//the current change (so we dont calculate it twice)
		double currChange=0;
		
		//extra variables that allow us to go through all the edges
		int[] a = new int[2];
		int[] b = new int[2];
		
		//We go through all the cities in the solution in order
		for(int i1=0 ; i1<s.howManyCities() ; i1++){
			for(int i2=i1+2 ; i2<s.howManyCities() ; i2++){
				
				//get the cities at the given index in the solution
				a[0]=s.getCity(i1);
				b[0]=s.getCity(i2);
				
				//get other part of the edge
				a[1]=s.getCity(i1+1);
				b[1]=s.getCity(i2+1);
				
				//check if the exchange makes sense (same as firstBest2Opt)
				if( (a[0]!=b[0]) && (a[0]!=b[1] && b[0]!=a[1]) ){ 
					
					
					//calculate the 2-opt exchange
					currChange=tsp.getDistance(a[0], b[0]) + tsp.getDistance(a[1], b[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]);
					
					//check if the exchange is better then the current best exchange
					if( currChange < bestChange ){
						
						//update best cities
						besta=a.clone();
						bestb=b.clone();
						
						//update best change
						bestChange = currChange;
						
					}
				}
				
			}
		}
		
		//check if the best exchange results in a better tour, if not return the same tour
		if(bestChange < 0){
			
			Solution nSol = s.clone();
			if(nSol.getIndexOfCity(besta[0])<nSol.getIndexOfCity(bestb[0])){
				nSol.reverse(nSol.getIndexOfCity(besta[1]), nSol.getIndexOfCity(bestb[0]));
			}else{
				nSol.reverse(nSol.getIndexOfCity(bestb[1]), nSol.getIndexOfCity(besta[0]));
			}
			return nSol;
			
		}else{
			return s;
		}
		
	}
	
	
	//-------------------------------------3 OPT-------------------------------------
	
	/*
	Searches randomly in the 3-opt neighborhood <searches> times in attempt to find a better solution.
	Each search includes all the 7 different combinations of edges that give a different solution then the starting one.
	If at the end of a search a better solution is found then the better solution is returned.
	When the <searches> run out the function returns the current solution.
	
	Params:
	s			- the solution
	tsp			- the TSP definition
	searches	- the number of searches
	
	Note:
	The same combinations of edges could be checked multiple times since having an updating a backlog would by itself be too expensive.
	*/
	public static Solution firstBest3Opt(Solution s, TSP tsp, int searches){
		
		/*
		a 3-opt move requres three edges (a1,a2), (b1,b2) and (c1,c2). We remove (a1,a2), (b1,b2) and (c1,c2) then add the remaining in 6 different ways (+1 if you count combining them the same way)
		a[0] represents <a1>, b[0] represents <b1> and c[0] represents <c1>
		<a2>, <b2> and <c2> are the cities which come after <a1>, <b1> and <c1> respectivly
		
		The 6 different ways to combine them are:
		1. same as 2-opt move on (a1,a2) and (b1,b2)
		2. same as 2-opt move on (b1,b2) and (c1,c2)
		3. same as 2-opt move on (c1,c2) and (b1,b2)
		4. 3-opt move. Disconnect all 3 edges and combine them like this: (a1,b1), (a2,c1), (b2,c2)
		5. 3-opt move. Disconnect all 3 edges and combine them like this: (a1,c1), (a2,b2), (b1,c2)
		6. 3-opt move. Disconnect all 3 edges and combine them like this: (a1,b2), (a2,c2), (b1,c1)
		7. 3.opt move. Disconnect all 3 edges and combine them like this: (a1,b2), (b1,c2), (c1,a2)
		
		*/
		int[] a = new int[2];
		int[] b = new int[2];
		int[] c = new int[2];
		
		//temporary variable for sorting cities
		int temp;
		
		//we have to know which way of combining them is the best
		int bestComb;
		
		//this array holds the changes in tour length for each possible way of combining the edges (rather have an array then calculate it twice as many times)
		double[] change = new double[7];
		
		Random rand = new Random();
		
		while(true){
			//get the random cities
			a[0]=rand.nextInt(s.howManyCities())+1;
			b[0]=rand.nextInt(s.howManyCities())+1;
			c[0]=rand.nextInt(s.howManyCities())+1;
			
			
			//sort the cities
			if(s.getIndexOfCity(a[0])>s.getIndexOfCity(b[0])){
				temp=a[0];
				a[0]=b[0];
				b[0]=temp;
			}
			if(s.getIndexOfCity(a[0])>s.getIndexOfCity(c[0])){
				temp=a[0];
				a[0]=c[0];
				c[0]=temp;
			}
			if(s.getIndexOfCity(b[0])>s.getIndexOfCity(c[0])){
				temp=b[0];
				b[0]=c[0];
				c[0]=temp;
			}
			
			//get the second cities so we know the two cities that connect the edges we'll be manipulating
			a[1]=s.getNextCity(a[0]);
			b[1]=s.getNextCity(b[0]);
			c[1]=s.getNextCity(c[0]);
			
			
			//checking if the move makes sense (the cities are different && not next to eachother)
			if( (a[0]!=b[0] && a[0]!=c[0] && b[0]!=c[0]) && ( (a[0]!=b[1] && b[0]!=a[1]) && (a[0]!=c[1] && c[0]!=a[1]) && (c[0]!=b[1] && b[0]!=c[1]) ) ){
				searches--;
				
				//Copied of all the different combinations for convenience
				
				//same as 2-opt move on (a1,a2) and (b1,b2)
				change[0]=tsp.getDistance(a[0], b[0]) + tsp.getDistance(a[1], b[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]);
				
				//same as 2-opt move on (a1,a2) and (c1,c2)
				change[1]=tsp.getDistance(a[0], c[0]) + tsp.getDistance(a[1], c[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(c[0], c[1]);
				
				//same as 2-opt move on (b1,b2) and (c1,c2)
				change[2]=tsp.getDistance(c[0], b[0]) + tsp.getDistance(c[1], b[1]) - tsp.getDistance(c[0], c[1]) - tsp.getDistance(b[0], b[1]);
				
				//3-opt move. Disconnect all 3 edges and combine them like this: (a1,b1), (a2,c1), (b2,c2)
				change[3]=tsp.getDistance(a[0], b[0]) + tsp.getDistance(a[1], c[0]) + tsp.getDistance(b[1], c[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]) - tsp.getDistance(c[0], c[1]);
				
				//3-opt move. Disconnect all 3 edges and combine them like this: (a1,c1), (a2,b2), (b1,c2)
				change[4]=tsp.getDistance(a[0], c[0]) + tsp.getDistance(a[1], b[1]) + tsp.getDistance(b[0], c[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]) - tsp.getDistance(c[0], c[1]);
				
				//3-opt move. Disconnect all 3 edges and combine them like this: (a1,b2), (a2,c2), (b1,c1)
				change[5]=tsp.getDistance(a[0], b[1]) + tsp.getDistance(a[1], c[1]) + tsp.getDistance(b[0], c[0]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]) - tsp.getDistance(c[0], c[1]);
				
				//3.opt move. Disconnect all 3 edges and combine them like this: (a1,b2), (b1,c2), (c1,a2)
				change[6]=tsp.getDistance(a[0], b[1]) + tsp.getDistance(b[0], c[1]) + tsp.getDistance(c[0], a[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]) - tsp.getDistance(c[0], c[1]);
				
				
				//now we figure out which one is the cheapest combination
				
				//the first one as currently the best one
				bestComb=0;
				
				//the other combinations
				for(int i = 1 ; i < 7 ; i++){
					if(change[i] < change[bestComb]){
						bestComb=i;
					}
				}
				
				//check if the best change gives a better solution, if it does create a new solution with that change and return it
				if(change[bestComb]<0){
					
					Solution nSol = s.clone();
					
					switch(bestComb){
						
						//Combination 1
						case 0:
							nSol.reverseCityEdge(a[0], a[1], b[0], b[1]);
							return nSol;
							
						//Combination 2
						case 1:
							nSol.reverseCityEdge(a[0], a[1], c[0], c[1]);
							return nSol;
							
						//Combination 3
						case 2:
							nSol.reverseCityEdge(b[0], b[1], c[0], c[1]);
							return nSol;
							
						//Combination 4
						case 3:
							nSol.reverseCityEdge(a[0], a[1], b[0], b[1]);
							nSol.reverseCityEdge(a[1], b[1], c[0], c[1]);
							return nSol;
							
						//Combination 5
						case 4:
							nSol.reverseCityEdge(a[0], a[1], b[0], b[1]);
							nSol.reverseCityEdge(a[0], b[0], c[0], c[1]);
							return nSol;
							
						//Combination 6
						case 5:
							nSol.reverseCityEdge(a[0], a[1], c[0], c[1]);
							nSol.reverseCityEdge(a[0], c[0], b[1], b[0]);
							return nSol;
							
						//Combination 7
						case 6:
							nSol.reverseCityEdge(a[0], a[1], b[0], b[1]);
							nSol.reverseCityEdge(a[1], b[1], c[0], c[1]);
							nSol.reverseCityEdge(a[0], b[0], b[1], c[1]);
							return nSol;
						
						default:
						//should never happen, should give an error out of array before hand
						
					}
					
				}
				
				
			}
			
			if(searches<=0){
				//return null;
				return s;
			}
			
		}
		
		
		
	}
	
	
	
	
	
	
	
	/*
	Searches the whole 3-opt neighborhood of a solution and returns the best one.
	If no imporving one was found it returns the same solution
	Mostly a copy of totalBest2Opt but for 3opt.
	
	Params:
	s	- the solution
	tsp	- the TSP
	
	*/
	public static Solution totalBest3Opt(Solution s, TSP tsp){
		
		/*
		a 3-opt move requres three edges (a1,a2), (b1,b2) and (c1,c2). We remove (a1,a2), (b1,b2) and (c1,c2) then add the remaining in 6 different ways (+1 if you count combining them the same way)
		a[0] represents <a1>, b[0] represents <b1> and c[0] represents <c1>
		<a2>, <b2> and <c2> are the cities which come after <a1>, <b1> and <c1> respectivly
		
		The 6 different ways to combine them are:
		1. same as 2-opt move on (a1,a2) and (b1,b2)
		2. same as 2-opt move on (b1,b2) and (c1,c2)
		3. same as 2-opt move on (c1,c2) and (b1,b2)
		4. 3-opt move. Disconnect all 3 edges and combine them like this: (a1,b1), (a2,c1), (b2,c2)
		5. 3-opt move. Disconnect all 3 edges and combine them like this: (a1,c1), (a2,b2), (b1,c2)
		6. 3-opt move. Disconnect all 3 edges and combine them like this: (a1,b2), (a2,c2), (b1,c1)
		7. 3.opt move. Disconnect all 3 edges and combine them like this: (a1,b2), (b1,c2), (c1,a2)
		
		
		*/
		
		//Read the commented section of combinations 1,2 and 3 inside the loop to understand what we're doing here
		Solution b2opt = totalBest2Opt(s, tsp);
		
		
		
		//best cities to change overall
		int[] bestA ={-1,-1};
		int[] bestB ={-1,-1};
		int[] bestC ={-1,-1};
		
		//best combination for the above cities
		int bestCombGlobal=-1;
		
		//the change in solution for the above cities and combination
		double bestChangeGlobal=tsp.getFitness(s);
		
		
		
		
		//local cities through which we iterate
		int[] a = new int[2];
		int[] b = new int[2];
		int[] c = new int[2];
		
		//temporary variable for sorting the local cities
		int temp;
		
		//local bestcombination
		int bestComb;
		
		//this array holds the local changes in tour length for each possible way of combining the edges (rather have an array then calculate it twice as many times)
		double[] change = new double[4];
		
		
		
		
		
		
		Random rand = new Random();
		
		//loop that iterates through all the indexes
		for(int i1=0 ; i1 < tsp.howManyCities() ; i1++ ){
			for(int i2=i1+2 ; i2 < tsp.howManyCities() ; i2++ ){
				for(int i3=i2+4 ; i3 < tsp.howManyCities() ; i3++ ){
					
					//get the cities at the given index in the solution
					a[0]=s.getCity(i1);
					b[0]=s.getCity(i2);
					c[0]=s.getCity(i3);
					
					//get other part of the edge
					a[1]=s.getCity(i1+1);
					b[1]=s.getCity(i2+1);
					c[1]=s.getCity(i3+1);
					
					
					//calculate all the different combinations of the 3 edges
					if( (a[0]!=b[0] && a[0]!=c[0] && b[0]!=c[0]) && ( (a[0]!=b[1] && b[0]!=a[1]) && (a[0]!=c[1] && c[0]!=a[1]) && (c[0]!=b[1] && b[0]!=c[1]) ) ){
						
						/*
						Some 2-opt moves are redundant for example:
						lets say in a TSP with 10 cities we select these edges:
						1. (1,2) (3,4) (5,6)
						2. (1,2) (3,4) (7,8)
						Both will check the 2-opt move between (1,2) and (3,4) therefor making a 2-opt calculation redundant.
						
						In order to not check 2-opt moves redundantly we'll run 2-opt at the start of this function, save its best result and at the end compare the best 2-opt moves with the best 3-opt pure move and return the better one.
						
						
						change[0]=tsp.getDistance(a[0], b[0]) + tsp.getDistance(a[1], b[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]);
						
						change[1]=tsp.getDistance(a[0], c[0]) + tsp.getDistance(a[1], c[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(c[0], c[1]);
						
						change[2]=tsp.getDistance(c[0], b[0]) + tsp.getDistance(c[1], b[1]) - tsp.getDistance(c[0], c[1]) - tsp.getDistance(b[0], b[1]);
						*/
						change[0]=tsp.getDistance(a[0], b[0]) + tsp.getDistance(a[1], c[0]) + tsp.getDistance(b[1], c[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]) - tsp.getDistance(c[0], c[1]);
						
						change[1]=tsp.getDistance(a[0], c[0]) + tsp.getDistance(a[1], b[1]) + tsp.getDistance(b[0], c[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]) - tsp.getDistance(c[0], c[1]);
						
						change[2]=tsp.getDistance(a[0], b[1]) + tsp.getDistance(a[1], c[1]) + tsp.getDistance(b[0], c[0]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]) - tsp.getDistance(c[0], c[1]);
						
						change[3]=tsp.getDistance(a[0], b[1]) + tsp.getDistance(b[0], c[1]) + tsp.getDistance(c[0], a[1]) - tsp.getDistance(a[0], a[1]) - tsp.getDistance(b[0], b[1]) - tsp.getDistance(c[0], c[1]);
						
						
						//save the best change
						
						//the first one as currently the best one
						bestComb=0;
						
						//the other combinations
						for(int i = 1 ; i < 4 ; i++){
							if(change[i] < change[bestComb]){
								bestComb=i;
							}
						}
						
						//figure out if the best change is better then the current best change
						if(change[bestComb] < bestChangeGlobal){
							//update the best accordingly
							bestCombGlobal=bestComb;
							bestChangeGlobal=change[bestComb];
							bestA=a.clone();
							bestB=b.clone();
							bestC=c.clone();
						}
					
					}
				}
			}
		}
		
		//here we check if a 2opt move was better then the best 3opt move, if so return it
		if(tsp.getFitness(b2opt) < (tsp.getFitness(s)+bestChangeGlobal)){
			return b2opt;
		}
		
		//check if we found a better solution, else return the current one
		if(bestChangeGlobal < 0){
			
			Solution nSol = s.clone();
			switch(bestCombGlobal){
				
				/* Read above in the loop when calculation of these move is done.
				case 0:
					nSol.reverse(nSol.getIndexOfCity(bestA[1]), nSol.getIndexOfCity(bestB[0]));
					return nSol;
					
				case 1:
					nSol.reverse(nSol.getIndexOfCity(bestA[1]), nSol.getIndexOfCity(bestC[0]));
					return nSol;
					
				case 2:
					nSol.reverse(nSol.getIndexOfCity(bestB[1]), nSol.getIndexOfCity(bestC[0]));
					return nSol;
					*/
					
				case 0:
					nSol.reverseCityEdge(bestA[0], bestA[1], bestB[0], bestB[1]);
					nSol.reverseCityEdge(bestA[1], bestB[1], bestC[0], bestC[1]);
					return nSol;
					
					
				case 1:
					nSol.reverseCityEdge(bestA[0], bestA[1], bestB[0], bestB[1]);
					nSol.reverseCityEdge(bestA[0], bestB[0], bestC[0], bestC[1]);
					return nSol;
					
					
				case 2:
					nSol.reverseCityEdge(bestA[0], bestA[1], bestC[0], bestC[1]);
					nSol.reverseCityEdge(bestA[0], bestC[0], bestB[1], bestB[0]);
					return nSol;
					
					
				case 3:
					nSol.reverseCityEdge(bestA[0], bestA[1], bestB[0], bestB[1]);
					nSol.reverseCityEdge(bestA[1], bestB[1], bestC[0], bestC[1]);
					nSol.reverseCityEdge(bestA[0], bestB[0], bestB[1], bestC[1]);
					return nSol;
				
				default:
				
				}
			return nSol;
			
		}else{
			return s;
		}
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//generates a random 2opt move and returns it (could be better or worse)
	public static Solution random2OptStep(Solution s){
		//new solution
		Solution nSol = s.clone();
		
		//random generator
		Random rand = new Random();
		
		//indexies of the cities to be switched
		int[] a = new int[2];
		int[] b = new int[2];
		
		
		//this is only here in the possibility we dont create appropriate indexies
		while(true){
			
			//generate the indexies
			a[0]=rand.nextInt(s.howManyCities())+1;
			a[1]=s.getNextCity(a[0]);
			b[0]=rand.nextInt(s.howManyCities())+1;
			b[1]=s.getNextCity(b[0]);
			
			//checking if the move makes sense (the cities are different && not next to eachother)
			if( (a[0]!=b[0]) && (a[0]!=b[1] && b[0]!=a[1]) ){
				
				nSol.reverseCityEdge(a[0], a[1], b[0], b[1]);
				
				
				//return the new solution
				return nSol;
				
			}
			
			
		}
		
	}
	
	
	
	
	
	
	//Generates a random 3opt move, could be better or worse (very similart to first best)
	public static Solution random3OptStep(Solution s){
		
		int[] a = new int[2];
		int[] b = new int[2];
		int[] c = new int[2];
		
		//temporary variable for sorting cities
		int temp;
		
		Random rand = new Random();
		
		while(true){
			//get the random cities
			a[0]=rand.nextInt(s.howManyCities())+1;
			b[0]=rand.nextInt(s.howManyCities())+1;
			c[0]=rand.nextInt(s.howManyCities())+1;
			
			
			//sort the cities
			if(s.getIndexOfCity(a[0])>s.getIndexOfCity(b[0])){
				temp=a[0];
				a[0]=b[0];
				b[0]=temp;
			}
			if(s.getIndexOfCity(a[0])>s.getIndexOfCity(c[0])){
				temp=a[0];
				a[0]=c[0];
				c[0]=temp;
			}
			if(s.getIndexOfCity(b[0])>s.getIndexOfCity(c[0])){
				temp=b[0];
				b[0]=c[0];
				c[0]=temp;
			}
			
			//get the second cities so we know the two cities that connect the edges we'll be manipulating
			a[1]=s.getNextCity(a[0]);
			b[1]=s.getNextCity(b[0]);
			c[1]=s.getNextCity(c[0]);
			
			
			//checking if the move makes sense (the cities are different && not next to eachother)
			if( (a[0]!=b[0] && a[0]!=c[0] && b[0]!=c[0]) && ( (a[0]!=b[1] && b[0]!=a[1]) && (a[0]!=c[1] && c[0]!=a[1]) && (c[0]!=b[1] && b[0]!=c[1]) ) ){
				
				Solution nSol = s.clone();
				
				//Select a random combination (without the starting one)
				switch(rand.nextInt(7)){
					
					//Combination 1
					case 0:
						nSol.reverseCityEdge(a[0], a[1], b[0], b[1]);
						return nSol;
						
					//Combination 2
					case 1:
						nSol.reverseCityEdge(a[0], a[1], c[0], c[1]);
						return nSol;
						
					//Combination 3
					case 2:
						nSol.reverseCityEdge(b[0], b[1], c[0], c[1]);
						return nSol;
						
					//Combination 4
					case 3:
						nSol.reverseCityEdge(a[0], a[1], b[0], b[1]);
						nSol.reverseCityEdge(a[1], b[1], c[0], c[1]);
						return nSol;
						
					//Combination 5
					case 4:
						nSol.reverseCityEdge(a[0], a[1], b[0], b[1]);
						nSol.reverseCityEdge(a[0], b[0], c[0], c[1]);
						return nSol;
						
					//Combination 6
					case 5:
						nSol.reverseCityEdge(a[0], a[1], c[0], c[1]);
						nSol.reverseCityEdge(a[0], c[0], b[1], b[0]);
						return nSol;
						
					//Combination 7
					case 6:
						nSol.reverseCityEdge(a[0], a[1], b[0], b[1]);
						nSol.reverseCityEdge(a[1], b[1], c[0], c[1]);
						nSol.reverseCityEdge(a[0], b[0], b[1], c[1]);
						return nSol;
					
					
					default:
					//should never happen, should give an error out of array before hand
					
				}
					
				
			}
			
			
		}
		
		
		
	}
	
	
	
	
	
	
}
