package Program.WP;
/*
WP
*/
//random for integers
import java.util.*;
//random for doubles
import java.lang.Math;
import Program.TSP.Solution;
import Program.TSP.TSP;
import Program.TSP.NearestNeighbor;

public class WP {
	
	/*
	The wolf pack algorithm
	Params:
	tsp 			- the tsp
	numWolf			- number of wolfs
	iter			- number of iterations
	W				- the chance of switching a random 
	stag			- how long can the leader stay unchanged before we attmpet to save him from a local optimum
	nChanges		- number of scouting behaviors to be done by the leader in case of stagnation
	*/
	public static Solution WolfPack(TSP tsp, int numWolf, int iter, double W, int stag, int nChanges){
		
		
		//stagnation counter
		int stCounter=0;
		
		//holds the index of the current wolf pack leader (the one which has the best solution)
		int lead=0;
		
		//holds the index of which wolf was the leader in the previous iteration of updating. If the leader updates so does the lLead
		int lLead;
		
		//this variable represents k
		int k1=-1;
		
		//this variable represents k'
		int k2=-1;
		
		//we'll need a random variable
		Random rand = new Random();
		
		//We'll use this as a list of the nearest neighbor for every city (helps us in soucting phase)
		NearestNeighbor NN = new NearestNeighbor(tsp, 1);
		
		
		//------------------------INITIALIZATION------------------
		//create n wolfs
		Solution[] wolfs = new Solution[numWolf];
		
		//initialize wolfs
		for(int i = 0 ; i < numWolf ; i++){
			wolfs[i] = new Solution(tsp.howManyCities());
			if(tsp.getFitness(wolfs[i])<tsp.getFitness(wolfs[lead])){
				lead=i;
			}
		}
		
		//set starting lLead
		lLead=lead;
		
		//this will hold the best leader since we potentially worsen the leader during updating
		Solution bestWolf = wolfs[lead];
		bestWolf.timeMS = wolfs[lead].timeMS;
		
		//The main loop
		for(int g = 0 ; g < iter ; g++){
			//------------------------SCOUTING------------------------
			
			//LOOP ALL WOLFS
			for(int a = 0 ; a < numWolf ; a++){
				
				
				//set k on undecided
				k1=-1;
				
				//LOOP FOR SCOUTING (we need this loop in case the first city we selected k and the other city k' are next to each other. Then we'll move onto the next k1)
				for(int k = 1 ; k <= tsp.howManyCities() ; k++){
					
					
					
					//select k', either random or closest
					if(Math.random()>W){
						
						//generating random k'
						k2=rand.nextInt(tsp.howManyCities())+1;
						
						//making sure they're not equal
						while(k==k2){
							k2=rand.nextInt(tsp.howManyCities())+1;
						}
						
						
					}else{
						
						//get closest k'
						k2=NN.getCityNN(k, 1);
						
						
					}
					
					//check if k and k' are not next to eachother
					if(wolfs[a].getNextCity(k)!=k2 && wolfs[a].getPrevCity(k)!=k2){
						
						//set k
						k1=k;
						
						//end loop
						k=tsp.howManyCities()+1;
					}
					
					
					
				}
				
				//here we have an appropriate k so we just have to switch positons from k+1 to k'
				
				//check if we set k1, if we havent that means that the selected k' was next to k
				if(k1!=-1){
					
					
					//check which k comes before and potentially switch them
					if(wolfs[a].getIndexOfCity(k2)<wolfs[a].getIndexOfCity(k1)){
						
						
						//check whenever the reverse gives us a better solution (if the change is positive)
						//[... (prev_k2) k2 ... (prev_k1) k1 ...]
						//So we 2opt with edges ((prev_k2), k2) ((prev_k1), k1)
						if(tsp.getDistance(k2, k1) + tsp.getDistance(wolfs[a].getPrevCity(k2), wolfs[a].getPrevCity(k1)) - tsp.getDistance(wolfs[a].getPrevCity(k2), k2) - tsp.getDistance(wolfs[a].getPrevCity(k1), k1) < 0){
							
							wolfs[a].reverseCityEdge(wolfs[a].getPrevCity(k2), k2, wolfs[a].getPrevCity(k1), k1);					
							
							//check if we change the leader of the pack
							if(tsp.getFitness(wolfs[lead]) > tsp.getFitness(wolfs[a])){
								
								lead=a;
								
								//check whenever the current leader is better the the best wolf found so far
								if(tsp.getFitness(bestWolf) > tsp.getFitness(wolfs[lead])){
									//clone() resets the timer, we do not want this.
									bestWolf = wolfs[lead].clone();
									bestWolf.timeMS=wolfs[lead].timeMS;
								}
								
							}
							
						}
						
					}else{
						
						//same as above but now k2 comes after k1 
						//[... k1 (next_k1) ... k2 (next_k2) ...]
						//So we 2opt with edges (k1, (next_k1)) (k2, (next_k2)
						if(tsp.getDistance(k1, k2) + tsp.getDistance(wolfs[a].getNextCity(k1), wolfs[a].getNextCity(k2)) - tsp.getDistance(k2, wolfs[a].getNextCity(k2)) - tsp.getDistance(k1, wolfs[a].getNextCity(k1)) < 0){
							
							wolfs[a].reverseCityEdge(k1, wolfs[a].getNextCity(k1), k2, wolfs[a].getNextCity(k2));					
							
							//check if we change the leader of the pack
							if(tsp.getFitness(wolfs[lead]) > tsp.getFitness(wolfs[a])){
								
								lead=a;
								
								//check whenever the current leader is better the the best wolf found so far
								if(tsp.getFitness(bestWolf) > tsp.getFitness(wolfs[lead])){
									//clone() resets the timer, we do not want this.
									bestWolf = wolfs[lead].clone();
									bestWolf.timeMS=wolfs[lead].timeMS;
								}
								
							}
							
						}
						
						
						
					}
					
				}
				
				//next wolf
				
			}
			
			//end loop for wolfes scouting here
			
			//------------------------SUMMONING------------------
			
			
			//we'll 3 variables to represent a, aL and aR (they hold cities, not indexies)
			int a, aL, aR;
			
			//randomly select a
			a = rand.nextInt(tsp.howManyCities())+1;
			
			//get aL and aR
			aL = wolfs[lead].getPrevCity(a);
			aR = wolfs[lead].getNextCity(a);
			
			//since the leader can change we need something to hold the current leader so he dosent summon himself
			int tempLeader=lead;
			
			//loop for summoning all wolves
			for(int i = 0 ; i < numWolf ; i++){
				
				//we need to hold 3 different changes to the solution (higher = better)
				double Va,Vb,Vc;
				
				
				//the pack leader when this phase starts does not summon himself
				if(i!=tempLeader){
					
					
					//Direction A
					if(wolfs[i].getPrevCity(a)==aL){
						//this is a trivial case, there is nothing to be done.
						Va = 0;
						
					}else if(wolfs[i].getIndexOfCity(aL) < wolfs[i].getIndexOfCity(a)){
						//old - new = the higher the better.
						Va = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(wolfs[i].getPrevCity(a), a) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getPrevCity(a)) - tsp.getDistance(aL, a);
						
					}else{
						//remove 3 connections and add 3 connections
						Va = tsp.getDistance(aL, wolfs[i].getPrevCity(aL)) + tsp.getDistance(aL, wolfs[i].getNextCity(aL)) + tsp.getDistance(a, wolfs[i].getPrevCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getNextCity(aL)) - tsp.getDistance(aL, a) - tsp.getDistance(wolfs[i].getPrevCity(a), aL);
					
					}
					
					
					//Direction B
					if(wolfs[i].getNextCity(a)==aR){
						//this is a trivial case, there is nothing to be done.
						Vb = 0;
						
					}else if(wolfs[i].getIndexOfCity(aR) > wolfs[i].getIndexOfCity(a)){
						//old - new = the higher the better
						Vb = tsp.getDistance(a, wolfs[i].getNextCity(a)) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) - tsp.getDistance(wolfs[i].getNextCity(a), wolfs[i].getNextCity(aR)) - tsp.getDistance(aR, a);
					
					}else{
						//remove 3 connections and add 3 connections
						Vb = tsp.getDistance(aR, wolfs[i].getPrevCity(aR)) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) + tsp.getDistance(a, wolfs[i].getNextCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aR), wolfs[i].getNextCity(aR)) - tsp.getDistance(a, aR) - tsp.getDistance(aR, wolfs[i].getNextCity(a));
					
					}
					
					//Direction C
					if(wolfs[i].getIndexOfCity(aL) < wolfs[i].getIndexOfCity(a) && wolfs[i].getIndexOfCity(aR) > wolfs[i].getIndexOfCity(a)){
						//Case 1: aL is to the left of a, aR is to the right of a
						
						if(wolfs[i].getIndexOfCity(aL)==0 && wolfs[i].getIndexOfCity(aR)==wolfs[i].howManyCities()-1){
							//Special case 1: Both aL and aR are the first and final city respectivly
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(wolfs[i].getPrevCity(a), a) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getPrevCity(a)) - tsp.getDistance(aL, a) +
								tsp.getDistance(a, wolfs[i].getNextCity(a)) + tsp.getDistance(aR, wolfs[i].getPrevCity(a)) - tsp.getDistance(wolfs[i].getNextCity(a), wolfs[i].getPrevCity(a)) - tsp.getDistance(aR, a);
						}else{
							//Normal case
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(wolfs[i].getPrevCity(a), a) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getPrevCity(a)) - tsp.getDistance(aL, a) +
								tsp.getDistance(a, wolfs[i].getNextCity(a)) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) - tsp.getDistance(wolfs[i].getNextCity(a), wolfs[i].getNextCity(aR)) - tsp.getDistance(aR, a);
						}
						
					}else if(wolfs[i].getIndexOfCity(aL) > wolfs[i].getIndexOfCity(a) && wolfs[i].getIndexOfCity(aR) < wolfs[i].getIndexOfCity(a)){
						//Case 2: case 1 but opposite
						
						if(wolfs[i].getPrevCity(a)==aR && wolfs[i].getPrevCity(aR)==aL){
							//Special case 1: when aR is before a && aL is before aR (only way this can happen is if aR is the first element)
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(aL, aR) + tsp.getDistance(a, aR) - tsp.getDistance(wolfs[i].getPrevCity(aL), aR) - tsp.getDistance(aL, a) - tsp.getDistance(aR, aL) +
								tsp.getDistance(wolfs[i].getPrevCity(aL), aR) + tsp.getDistance(aR, aL) + tsp.getDistance(a, wolfs[i].getNextCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aL), aL) - tsp.getDistance(a, aR) - tsp.getDistance(aR, wolfs[i].getNextCity(a));
						
						}else if(wolfs[i].getNextCity(a)==aL && wolfs[i].getNextCity(aL)==aR){
							//Special case 2: when aL is after a && aR is after aL (can only happen if aR is the last element)
							Vc = tsp.getDistance(a, aL) + tsp.getDistance(aL, aR) + tsp.getDistance(a, wolfs[i].getPrevCity(a)) - tsp.getDistance(a, aR) - tsp.getDistance(aL, a) - tsp.getDistance(wolfs[i].getPrevCity(a), aL);
						
						}else if(wolfs[i].getNextCity(aL)==aR/*wolfs[i].getIndexOfCity(aR)==0 && wolfs[i].getIndexOfCity(aL)==wolfs[i].howManyCities()-1*/){
							//Special case 3: next to eachother at the ends
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(aL, aR) + tsp.getDistance(a, wolfs[i].getPrevCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aL), aR) - tsp.getDistance(aL, a) - tsp.getDistance(wolfs[i].getPrevCity(a), aL) +
								tsp.getDistance(aR, wolfs[i].getPrevCity(aL)) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) + tsp.getDistance(a, wolfs[i].getNextCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getNextCity(aR)) - tsp.getDistance(a, aR) - tsp.getDistance(aR, wolfs[i].getNextCity(a));
						
						}else if(wolfs[i].getNextCity(a)==aL && wolfs[i].getPrevCity(a)==aR){
							//Special case 4: when they're both next to a
							Vc = tsp.getDistance(a, aL) + tsp.getDistance(aL, wolfs[i].getNextCity(aL)) + tsp.getDistance(a, aR) - tsp.getDistance(a, wolfs[i].getNextCity(aL)) - tsp.getDistance(aL, a) - tsp.getDistance(aR, aL) +
								tsp.getDistance(wolfs[i].getPrevCity(aR), aR) + tsp.getDistance(aR, aL) + tsp.getDistance(a, wolfs[i].getNextCity(aL)) - tsp.getDistance(wolfs[i].getPrevCity(aR), aL) - tsp.getDistance(a, aR) - tsp.getDistance(aR, wolfs[i].getNextCity(aL));
						
						}else if(wolfs[i].getPrevCity(a)==aR){
							//Special case 5: aR is before a 
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(aL, wolfs[i].getNextCity(aL)) + tsp.getDistance(a, aR) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getNextCity(aL)) - tsp.getDistance(aL, a) - tsp.getDistance(aR, aL) +
								tsp.getDistance(wolfs[i].getPrevCity(aR), aR) + tsp.getDistance(aR, aL) + tsp.getDistance(a, wolfs[i].getNextCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aR), aL) - tsp.getDistance(a, aR) - tsp.getDistance(aR, wolfs[i].getNextCity(a));
												
						}else if(wolfs[i].getNextCity(a)==aL){
							//Special case 6: aL is after a
							Vc = tsp.getDistance(a, aL) + tsp.getDistance(aL, wolfs[i].getNextCity(aL)) + tsp.getDistance(a, wolfs[i].getPrevCity(a)) - tsp.getDistance(a, wolfs[i].getNextCity(aL)) - tsp.getDistance(aL, a) - tsp.getDistance(wolfs[i].getPrevCity(a), aL) +
								tsp.getDistance(wolfs[i].getPrevCity(aR), aR) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) + tsp.getDistance(a, wolfs[i].getNextCity(aL)) - tsp.getDistance(wolfs[i].getPrevCity(aR), wolfs[i].getNextCity(aR)) - tsp.getDistance(a, aR) - tsp.getDistance(aR, wolfs[i].getNextCity(aL));
						
						}else{
							//Normal case
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(aL, wolfs[i].getNextCity(aL)) + tsp.getDistance(a, wolfs[i].getPrevCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getNextCity(aL)) - tsp.getDistance(aL, a) - tsp.getDistance(wolfs[i].getPrevCity(a), aL) +
								tsp.getDistance(wolfs[i].getPrevCity(aR), aR) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) + tsp.getDistance(a, wolfs[i].getNextCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aR), wolfs[i].getNextCity(aR)) - tsp.getDistance(a, aR) - tsp.getDistance(aR, wolfs[i].getNextCity(a));
						}
					}else if(wolfs[i].getIndexOfCity(aL) > wolfs[i].getIndexOfCity(a) && wolfs[i].getIndexOfCity(aR) > wolfs[i].getIndexOfCity(a)){
						//Case 3: both are on the right of a
						if(wolfs[i].getPrevCity(a)==aL && wolfs[i].getPrevCity(aL)==aR){
							//Special case 1: This only happens when the first city is a, the second to last city is aR and the last city is aL
							Vc = tsp.getDistance(a, wolfs[i].getNextCity(a)) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) - tsp.getDistance(wolfs[i].getNextCity(a), wolfs[i].getNextCity(aR)) - tsp.getDistance(a, aR);
							
						}else if(wolfs[i].getNextCity(a)==aL){
							//Special case 1: aL is next to a
							Vc = tsp.getDistance(a, aL) + tsp.getDistance(aL, wolfs[i].getNextCity(aL)) + tsp.getDistance(a, wolfs[i].getPrevCity(a)) - tsp.getDistance(a, wolfs[i].getNextCity(aL)) - tsp.getDistance(aL, a) - tsp.getDistance(wolfs[i].getPrevCity(a), aL) +
								tsp.getDistance(a, wolfs[i].getNextCity(aL)) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) - tsp.getDistance(wolfs[i].getNextCity(aL), wolfs[i].getNextCity(aR)) - tsp.getDistance(a, aR);
							
						}else if(wolfs[i].getNextCity(aR)==aL){
							//Special case 2: aL is after aR
							Vc = tsp.getDistance(aR, aL) + tsp.getDistance(aL, wolfs[i].getNextCity(aL)) + tsp.getDistance(a, wolfs[i].getPrevCity(a)) - tsp.getDistance(aR, wolfs[i].getNextCity(aL)) - tsp.getDistance(aL, a) - tsp.getDistance(wolfs[i].getPrevCity(a), aL) +
								tsp.getDistance(a, wolfs[i].getNextCity(a)) + tsp.getDistance(aR, wolfs[i].getNextCity(aL)) - tsp.getDistance(wolfs[i].getNextCity(a), wolfs[i].getNextCity(aL)) - tsp.getDistance(a, aR);
						
						}else if(wolfs[i].getPrevCity(a)==aL){
							//Special case 3: Al is already the city before a (when a is first)
							Vc = tsp.getDistance(a, wolfs[i].getNextCity(a)) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) - tsp.getDistance(wolfs[i].getNextCity(a), wolfs[i].getNextCity(aR)) - tsp.getDistance(a, aR);
							
						}else{
							//Normal case
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(aL, wolfs[i].getNextCity(aL)) + tsp.getDistance(a, wolfs[i].getPrevCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getNextCity(aL)) - tsp.getDistance(aL, a) - tsp.getDistance(wolfs[i].getPrevCity(a), aL) +
								tsp.getDistance(a, wolfs[i].getNextCity(a)) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) - tsp.getDistance(wolfs[i].getNextCity(a), wolfs[i].getNextCity(aR)) - tsp.getDistance(a, aR);
						}
					}else{
						//Case 4: both are on the left of a
						if(wolfs[i].getNextCity(a)==aR && wolfs[i].getNextCity(aR)==aL){
							//Special case 1: This only happens when aL is the first city, aR is the 2nd city and a is the last city
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(wolfs[i].getPrevCity(a), a) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getPrevCity(a)) - tsp.getDistance(aL, a);
							
						}else if(wolfs[i].getPrevCity(a)==aR){
							//Special case 1: aR is next to a
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(aR, a) - tsp.getDistance(wolfs[i].getPrevCity(aL), aR) - tsp.getDistance(aL, a) +
								tsp.getDistance(aR, wolfs[i].getPrevCity(aR)) + tsp.getDistance(aR, wolfs[i].getPrevCity(aL)) + tsp.getDistance(a, wolfs[i].getNextCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aR), wolfs[i].getPrevCity(aL)) - tsp.getDistance(a, aR) - tsp.getDistance(aR, wolfs[i].getNextCity(a));
						
						}else if(wolfs[i].getPrevCity(aL)==aR){
							//Special case 2: aR is before aL
							Vc = tsp.getDistance(aR, aL) + tsp.getDistance(wolfs[i].getPrevCity(a), a) - tsp.getDistance(aR, wolfs[i].getPrevCity(a)) - tsp.getDistance(aL, a) +
								tsp.getDistance(aR, wolfs[i].getPrevCity(aR)) + tsp.getDistance(aR, wolfs[i].getPrevCity(a)) + tsp.getDistance(a, wolfs[i].getNextCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aR), wolfs[i].getPrevCity(a)) - tsp.getDistance(a, aR) - tsp.getDistance(aR, wolfs[i].getNextCity(a));
						
						}else if(wolfs[i].getNextCity(a)==aR){
							//Special case 3: aR is already the next city after a (when a is last)
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(wolfs[i].getPrevCity(a), a) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getPrevCity(a)) - tsp.getDistance(aL, a);
							
						}else{
							//Normal case
							Vc = tsp.getDistance(wolfs[i].getPrevCity(aL), aL) + tsp.getDistance(wolfs[i].getPrevCity(a), a) - tsp.getDistance(wolfs[i].getPrevCity(aL), wolfs[i].getPrevCity(a)) - tsp.getDistance(aL, a) +
								tsp.getDistance(aR, wolfs[i].getPrevCity(aR)) + tsp.getDistance(aR, wolfs[i].getNextCity(aR)) + tsp.getDistance(a, wolfs[i].getNextCity(a)) - tsp.getDistance(wolfs[i].getPrevCity(aR), wolfs[i].getNextCity(aR)) - tsp.getDistance(a, aR) - tsp.getDistance(aR, wolfs[i].getNextCity(a));
						}
					}
					
					
					
					//we figure out which one of the 3 is the biggest
					if(Va > 0 || Vb > 0 || Vc > 0){
						
						if(Vc > Vb){
							
							if(Vc > Va){
								
								//Vc highest
								if(wolfs[i].getIndexOfCity(aL) < wolfs[i].getIndexOfCity(a)){
									if(wolfs[i].getIndexOfCity(aR) > wolfs[i].getIndexOfCity(a)){
										//reverse aL reverse aR
										wolfs[i].reverseCities(aL, wolfs[i].getPrevCity(a));
										wolfs[i].reverseCities(wolfs[i].getNextCity(a), aR);
										
									}else{
										//reverse aL insert aR
										wolfs[i].reverseCities(aL, wolfs[i].getPrevCity(a));
										wolfs[i].insertOnTheRight(a, aR);
										
									}
								}else{
									if(wolfs[i].getIndexOfCity(aR) > wolfs[i].getIndexOfCity(a)){
										//insert aL reverse aR
										wolfs[i].insertOnTheLeft(a, aL);
										wolfs[i].reverseCities(wolfs[i].getNextCity(a), aR);
										
									}else{
										//insert aL inesrt aR
										wolfs[i].insertOnTheLeft(a, aL);
										wolfs[i].insertOnTheRight(a, aR);
										
									}
								}
								
							}else{
								
								//Va highest
								if(wolfs[i].getIndexOfCity(aL) < wolfs[i].getIndexOfCity(a)){
									wolfs[i].reverseCities(aL, wolfs[i].getPrevCity(a));
									
								}else{
									wolfs[i].insertOnTheLeft(a, aL);
									
								}
							}
							
						}else{
							
							if(Vb > Va){
								
								//Vb highest
								if(wolfs[i].getIndexOfCity(aR) > wolfs[i].getIndexOfCity(a)){
									wolfs[i].reverseCities(wolfs[i].getNextCity(a), aR);
									
								}else{
									wolfs[i].insertOnTheRight(a, aR);
									
								}
								
							}else{
								
								//Va highest
								if(wolfs[i].getIndexOfCity(aL) < wolfs[i].getIndexOfCity(a)){
									wolfs[i].reverseCities(aL, wolfs[i].getPrevCity(a));
									
								}else{
									wolfs[i].insertOnTheLeft(a, aL);
									
								}
								
							}
							
						}
						
						//update lead wolf if this wolf is better
						if(tsp.getFitness(wolfs[lead]) > tsp.getFitness(wolfs[i])){
							lead=i;
							//update best wolf if the wolf is better
							if(tsp.getFitness(bestWolf) > tsp.getFitness(wolfs[lead])){
								//clone() resets the timer, we do not want this.
								bestWolf = wolfs[lead].clone();
								bestWolf.timeMS=wolfs[lead].timeMS;
							}
						}
						
					}
					
					
					
					
				}
			}
			
			//------------------------UPDATING------------------
			//check if we increase the stagnation counter or do we have a new leader
			if(lLead==lead){
				stCounter++;
			}else{
				//new leader, set new leader
				lLead=lead;
				//check if my chance this new leader is better than the previous best solution
				if(tsp.getFitness(bestWolf) > tsp.getFitness(wolfs[lead])){
					//clone() resets the timer, we do not want this.
					bestWolf = wolfs[lead].clone();
					bestWolf.timeMS=wolfs[lead].timeMS;
				}
			}
			
			//check if we do scouting behavior on wolf leader
			if(stCounter >= stag){
				
				//reset counter
				stCounter=0;
				
				//loop for how many times we have to scout with the leader
				for(int sc = 0 ; sc < nChanges; sc++){
					
					//get the k
					for(int k = 1 ; k <= tsp.howManyCities() ; k++){
						
						//select k', either random or closest
						if(Math.random()>W){
							
							//generating random k'
							k2=rand.nextInt(tsp.howManyCities())+1;
							
							//making sure they're not equal
							while(k==k2){
								k2=rand.nextInt(tsp.howManyCities())+1;
							}
							
						}else{
							
							//get closest k'
							k2=NN.getCityNN(k, 1);
							
						}
						
						//check if k and k' are not next to eachother
						if(wolfs[lead].getNextCity(k)!=k2 && wolfs[lead].getPrevCity(k)!=k2){
							
							//set k
							k1=k;
							
							//end loop
							k=tsp.howManyCities()+1;
							
						}
						
					}
					
					//accept the k
					if(wolfs[lead].getIndexOfCity(k2) < wolfs[lead].getIndexOfCity(k1)){
						
						wolfs[lead].reverseCities(k2, wolfs[lead].getPrevCity(k1));
						
					}else{
						
						wolfs[lead].reverseCities(wolfs[lead].getNextCity(k1), k2);
						
					}
					
					
				}
			}
			
			
			//------------------------LOCAL SEARCH------------------
			
			//the selected city (a)
			int sC;
			
			//the city thats on the right of the selected city in the 2nd wolf
			int RsC;
			
			//We'll do an 2-opt move. We will remove a connection from city RsC to another city. This variable holds this "another city"
			int oth;
			
			//this variable tells us if we found a better solution in the first exhcange
			boolean isImp;
			
			//each wolf once in a pre defined order atm
			for(int w1 = 0 ; w1 < numWolf ; w1++){
				for(int w2 = w1+1 ; w2 < numWolf ; w2++){
					
					//reset variable
					isImp=false;
					
					//Get a random city
					sC=rand.nextInt(tsp.howManyCities())+1;
					//make sure its not the last city in either solutions. If it is try again.
					while( wolfs[w1].getIndexOfCity(sC)==tsp.howManyCities()-1 || wolfs[w2].getIndexOfCity(sC)==tsp.howManyCities()-1 ){
						sC=rand.nextInt(tsp.howManyCities())+1;
					}
					
					//get the "another city" in the 2nd wolf
					RsC=wolfs[w2].getNextCity(sC);
					
					//check whenever the other city and the selected city are also adjecant in the first wolf. If so, we do nothing
					if(wolfs[w1].getNextCity(sC)!=RsC && wolfs[w1].getPrevCity(sC)!=RsC){
						
						//check whenever RsC is before or after sC in the first wolf
						if(wolfs[w1].getIndexOfCity(sC) > wolfs[w1].getIndexOfCity(RsC)){
							//RsC is before of sC in wolf 1
							
							//get oth (which is the city before of RsC)
							oth=wolfs[w1].getPrevCity(RsC);
							
							//check if the solution would be improved by this reversal (the more positive the better)
							if(tsp.getDistance(oth, RsC) + tsp.getDistance(wolfs[w1].getPrevCity(sC), sC) - tsp.getDistance(oth, wolfs[w1].getPrevCity(sC)) - tsp.getDistance(RsC, sC) > 0){
								
								//do the reversal
								wolfs[w1].reverseCityEdge(oth, RsC, wolfs[w1].getPrevCity(sC), sC);
								
								//improvment was found!
								isImp=true;
							}
							
						}else{
							//RsC is after of sC in wolf 1
							
							//get oth (which is the city after of RsC)
							oth=wolfs[w1].getNextCity(RsC);
							
							//check if the solution would be improved by this reversal (the more positive the better)
							if(tsp.getDistance(sC, wolfs[w1].getNextCity(sC)) + tsp.getDistance(RsC, oth) - tsp.getDistance(sC, RsC) - tsp.getDistance(wolfs[w1].getNextCity(sC), oth) > 0){
								
								//do the reversal
								wolfs[w1].reverseCityEdge(sC, wolfs[w1].getNextCity(sC), RsC, oth);
								
								//improvment was found!
								isImp=true;
							}
							
						}
						
						//If we havent found an improvment for wolf 1 we have to try to improve wolf 2
						if(!isImp){
							//check whenever oth is before or after RsC in the 2nd wolf
							if(wolfs[w2].getIndexOfCity(oth) > wolfs[w2].getIndexOfCity(RsC)){
								//oth is after RsC in wolf 2
								
								//check whenever the move would improve the solution
								if(tsp.getDistance(RsC, wolfs[w2].getNextCity(RsC)) + tsp.getDistance(oth, wolfs[w2].getNextCity(oth)) - tsp.getDistance(RsC, oth) - tsp.getDistance(wolfs[w2].getNextCity(RsC), wolfs[w2].getNextCity(oth)) > 0){
									
									wolfs[w2].reverseCityEdge(RsC, wolfs[w2].getNextCity(RsC), oth, wolfs[w2].getNextCity(oth));
									
								}
								
							}else{
								//oth is before RsC in wolf 2
								
								//check whenever the move would improve the solution
								if(tsp.getDistance(wolfs[w2].getPrevCity(oth), oth) + tsp.getDistance(wolfs[w2].getPrevCity(RsC), RsC) +  - tsp.getDistance(wolfs[w2].getPrevCity(oth), wolfs[w2].getPrevCity(RsC)) - tsp.getDistance(oth, RsC) > 0){
									
									wolfs[w2].reverseCityEdge(wolfs[w2].getPrevCity(oth), oth, wolfs[w2].getPrevCity(RsC), RsC);
									
								}
								
							}
							
						}
						
					}
					
					
					
				}
			}
			
			//figure out the lead again after local search (and updating, though lead dosent matter in local search)
			for(int b = 0 ; b < numWolf ; b++){
				if(tsp.getFitness(wolfs[lead])>tsp.getFitness(wolfs[b])){
					lead=b;
					if(tsp.getFitness(bestWolf) > tsp.getFitness(wolfs[lead])){
						//clone() resets the timer, we do not want this.
						bestWolf = wolfs[lead].clone();
						bestWolf.timeMS=wolfs[lead].timeMS;
					}
				}
			}
		}
		
		
		//return best wolf found
		return bestWolf;
		
		
	}




}









