package Program.WP;

//random for integers
import java.util.*;
//random for doubles
import java.lang.Math;
import Program.TSP.Solution;
import Program.TSP.TSP;
import Program.TSP.NearestNeighbor;

public class WP {
	/*
	tsp 			- the tsp 
	REMOVED, we'll just create random solution//s 				- the solution with which we start
	//REMOVED HAVE A FUNCTION FOR THIS NOW sTemp			- starting temperature
	coolingFactor	- how much we multiply sTemp whenever we lower the temperature (0~1)
	iter			- amount of iterations
	iterAtTemp		- amount of iterations at a temperature
	COULD ADD LOWER LIMIT AS AN ALTERNATIVE ENDING CONDITON, BUT YOU CAN CALCULATE IT
	*/
	
	
	
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
		
		//System.out.println("tsp has " + tsp.howManyCities() + " cities");
		
		//stagnation counter
		int stCounter=0;
		
		//alternative soltuion, used when we reverse
		int[] n;
		
		//holds the index of the current wolf pack leader
		int lead=0;
		
		//hold the index of the next leader (summoning behavior)
		int nLead;
		
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
		//this will hold the best leader since we potentially worsen the leader during updating
		
		Solution bestWolf = wolfs[lead];
		
		//System.out.println("WOLF (we'll follow wolf number 1 (index 0)):\n\n");
		//System.out.println("Wolf 1 is : " + wolfs[0]);
		for(int g = 0 ; g < iter ; g++){
			//------------------------SCOUTING------------------------
			
			//LOOP ALL WOLFS (use a)
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
						
						//System.out.printf("random. k=%d, k'=%d\n", k, k2);
						
					}else{
						
						/*
						OLD CODE (JUST MAKES SURE THRE IS A k2 SO NO ERROR LATER (i think))
						//setting starting k'
						if(k==0){
							k2=1;
						}else{
							k2=0;
						}
						*/
						
						//get closest k'
						k2=NN.getCityNN(k, 1);
						//System.out.printf("NN. k=%d, k'=%d\n", k, k2);
						//OLD CODE FOR CLOSEST k'
						/*for(int kk = 0 ; kk < tsp.howManyCities() ; kk++){
							if( k!=kk && tsp.getDistance(wolfs[a].getCity(k), wolfs[a].getCity(kk)) < tsp.getDistance(wolfs[a].getCity(k), wolfs[a].getCity(k2))){
								k2=kk;
							}
						}*/
						
						
						
					}
					//check if k and k' are not next to eachother
					if(wolfs[a].getNextCity(k)!=k2 && wolfs[a].getPrevCity(k)!=k2){
						
						//set k
						k1=k;
						
						//end loop
						k=tsp.howManyCities()+1;
						//System.out.println("k accepted!");
					}
					
					/*
					OLD CODE
					if( ( k!=k2+1 && k!=k2-1 ) && !( k==tsp.howManyCities()-1 && k2==0 ) && !( k2==tsp.howManyCities()-1 && k==0 ) ){
						if(a==0){
							//REMOVE
							//System.out.println("found k1 and k2! " + k + " & " + k2);
						}
						//set k
						k1=k;
						
						//make the loop stop
						k=tsp.howManyCities();
					}*/
					
					
				}
				
				//REMOVE
				if(a==0){
					//System.out.printf("wolf:\n%s\nvalue:%.1f\n(k, k') = (%d, %d)\n", wolfs[a], tsp.getFitness(wolfs[a]), k1, k2);
					//REMOVE
					//System.out.println(String.format("Scouting selects k1:%d and k2:%d\nwolf:\n%s", k1, k2, wolfs[0]));
				}
				//here we have an appropriate k so we just have to switch positons from k+1 to k'
				
				//create new solution representation
				//n = new int[tsp.howManyCities()];
				
				//check if we set k1, if we havent that means we were trying to find a better edge but could not
				if(k1!=-1){
					//check which k comes before and potentially switch them
					if(wolfs[a].getIndexOfCity(k2)<wolfs[a].getIndexOfCity(k1)){
						//System.out.printf("calculation:\n");
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
									bestWolf = wolfs[lead].clone();
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
									bestWolf = wolfs[lead].clone();
								}
								
							}
							
						}
						
						
						
					}
					if(a==0){
						//REMOVE
						//System.out.println(String.format("reversed wolf:\n%s",new Solution(n)));
					}
					/*
				for(int i = 0 ; i <= k1 ; i++){
					n[i]=wolfs[a].getCity(i);
				}
		
				//add the reversed portion
				for(int i = k2 ; i > k1 ; i--){
					n[k1+k2+1-i]=wolfs[a].getCity(i);
				}
		
				//add the final portion
				for(int i = k2+1 ; i < tsp.howManyCities(); i++){
					n[i]=wolfs[a].getCity(i);
				}
				*/
					
					//check if the solution is better, if it is switch the new and the current one
					/*
					OLD CODE, now included in above if
					
					if(tsp.getFitness(wolfs[a]) > tsp.getFitness(new Solution(n))){
						wolfs[a] = new Solution(n);
						
						//check if the newest best is the leader of the pack, if it is set new leader
						if(tsp.getFitness(wolfs[lead]) > tsp.getFitness(wolfs[a])){
							lead=a;
							if(tsp.getFitness(bestWolf) > tsp.getFitness(wolfs[lead])){
								bestWolf = wolfs[lead];
							}
						}
					}*/
				}
			}
			//end loop for wolfes scouting here
			//------------------------SUMMONING------------------
			//REMOVE
			//System.out.println("\n-----------------------SUMMONING-----------------------\n\nwolf at the start of summoning:\n" + wolfs[0]);
			
			//we'll 3 variables to represent a, aL and aR (they hold cities, not indexies)
			int a, aL, aR;
			
			//we initialize nLead to the current leader
			nLead=lead;
			
			//randomly select a
			a = rand.nextInt(tsp.howManyCities())+1;
			
			//get aL and aR
			aL = wolfs[lead].getPrevCity(a);
			aR = wolfs[lead].getNextCity(a);
			//we have to be carefull in case a might be first/last element
			/*
			OLD CODE
			if(wolfs[lead].getIndexOfCity(a)==0){
				aL=wolfs[lead].getCity(tsp.howManyCities()-1);
			}else{
				aL=wolfs[lead].getCity(wolfs[lead].getIndexOfCity(a)-1);
			}
			
			if(wolfs[lead].getIndexOfCity(a)==tsp.howManyCities()-1){
				aR=wolfs[lead].getCity(0);
			}else{
				aR=wolfs[lead].getCity(wolfs[lead].getIndexOfCity(a)+1);
			}*/
			//REMOVE
			//System.out.println(String.format("Leader is %dth:\n%s\na:%d aL:%d aR:%d\n", lead, wolfs[lead], a, aL, aR));
			
			//summoning the wolfs loop
			
			for(int i = 0 ; i < numWolf ; i++){
				
				//we need to hold 3 different solutions here so we'll have 3 extra variables
				Solution p1, p2, p3;
				/*
				int[] p1 = new int[tsp.howManyCities()];
				int[] p2 = new int[tsp.howManyCities()];
				int[] p3 = new int[tsp.howManyCities()];
				*/
				
				//helps with direction C and later on when figuring out which the best solution is and if its better then the current leader
				//OLD CODE Solution intermediate;
				
				//the pack leader does not summon himself
				if(i!=lead){
					//REMOVE
					//System.out.println("\nSelected wolf " + i +  "th is \nW: " + wolfs[i]);
					
					//direction A
					if(wolfs[i].getIndexOfCity(aL) < wolfs[i].getIndexOfCity(a)){//aL is on the left of A in the current wolf
						//REMOVE
						//System.out.println("A reversed!");
						p1 = wolfs[i].clone();
						p1.reverseCityEdge(wolfs[i].getPrevCity(aL), aL, wolfs[i].getPrevCity(a), a);
						
						//p1 = reverse(wolfs[i], wolfs[i].getIndexOfCity(aL), wolfs[i].getIndexOfCity(a)-1); OLD CODE
						
						/*
						for(int j = 0 ; j < wolfs[i].getIndexOfCity(aL) ; j++){
							p1[j]=wolfs[i].getCity(j);
						}
						
						//add the reversed portion
						for(int j = wolfs[i].getIndexOfCity(a) ; j >= wolfs[i].getIndexOfCity(aL) ; j--){
							p1[wolfs[i].getIndexOfCity(a)+wolfs[i].getIndexOfCity(aL)+1-j]=wolfs[i].getCity(j);
						}
						
						//add the final portion
						for(int j = wolfs[i].getIndexOfCity(a) ; j < tsp.howManyCities(); j++){
							p1[j]=wolfs[i].getCity(j);
						}*/
						
					}else{
						
						//else we just force put the aL on the left of a
						p1 = wolfs[i].clone();
						p1.insertOnTheLeft(a, aL);
					}
					
					//REMOVE
					//System.out.println("A: " + new Solution(p1));
					//direction B
					if(wolfs[i].getIndexOfCity(aR) > wolfs[i].getIndexOfCity(a)){//aL is on the left of A in the current wolf
						//REMOVE
						//System.out.println("B reversed!");
						//p2 = reverse(wolfs[i], wolfs[i].getIndexOfCity(a)+1, wolfs[i].getIndexOfCity(aR));OLD CODE
						
						p2 = wolfs[i].clone();
						p2.reverseCityEdge(a, wolfs[i].getNextCity(a), aR, wolfs[i].getNextCity(aR));
						
						/*
					for(int j = 0 ; j <= wolfs[i].getIndexOfCity(a) ; j++){
						p1[j]=wolfs[i].getCity(j);
					}
					
					//add the reversed portion
					for(int j = wolfs[i].getIndexOfCity(aR) ; j > wolfs[i].getIndexOfCity(a) ; j--){
						p1[wolfs[i].getIndexOfCity(aR)+wolfs[i].getIndexOfCity(a)+1-j]=wolfs[i].getCity(j);
					}
					
					//add the final portion
					for(int j = wolfs[i].getIndexOfCity(aR)+1 ; j < tsp.howManyCities(); j++){
						p1[j]=wolfs[i].getCity(j);
					}*/
						
					}else{
						
						//else we just force put the aR on the right of a
						
						p2 = wolfs[i].clone();
						p2.insertOnTheRight(a, aR);
						
					}
					
					//REMOVE
					//System.out.println("B: " + new Solution(p2));
					//direction C (both A and B together)  intermediate
					//------------direction A in C
					/*
					This whole thing can be ignored, since A in C is already done by direction A
					if(wolfs[i].getIndexOfCity(aL) < wolfs[i].getIndexOfCity(a)){//aL is on the left of A in the current wolf
						
						System.out.println("CA reversed!");
						p3 = reverse(wolfs[i], wolfs[i].getIndexOfCity(aL), wolfs[i].getIndexOfCity(a)-1);
						
						
					}else{
						
						
						for(int j = 0 ; j < wolfs[i].getIndexOfCity(a) ; j++){
							p3[j]=wolfs[i].getCity(j);
						}
						
						p3[wolfs[i].getIndexOfCity(a)] = aL;
						p3[wolfs[i].getIndexOfCity(a)+1] = a;
						
						int wh=wolfs[i].getIndexOfCity(a)+2;
						for(int j = wolfs[i].getIndexOfCity(a)+1 ; j < tsp.howManyCities() ; j++){
							if(wolfs[i].getCity(j)!=aL){
								p3[wh]=wolfs[i].getCity(j);
								wh++;
							}
						}
					}*/
					
					
					
					//save intermediate solution
					//OLD CODEintermediate = new Solution(p1);
					
					//REMOVE
					//System.out.println("C1:" + intermediate);
					//OLD CODEp3 = new int[tsp.howManyCities()];
					p3=p1.clone();
					//------------direction B in C
					if(p3.getIndexOfCity(aR) > p3.getIndexOfCity(a)){//aL is on the left of A in the current wolf
						//REMOVE
						//System.out.println("CB reversed!");
						p3.reverseCityEdge(a, p3.getNextCity(a), aR, p3.getNextCity(aR));
						
						
					}else{
						
						
						p3.insertOnTheRight(a, aR);
						
						
					}
					//REMOVE
					//System.out.println("C2:" + new Solution(p3));
					
					//we have p1, p2 and p3 for ith wolf here
					if(i==0){
						//REMOVE
						//System.out.println(String.format("\nSUMMONIN:\np1:%s\np2:%s\np3:%s\n",(new Solution(p1)).toString(), (new Solution(p2)).toString(), (new Solution(p3)).toString() ));
					}
					//we figure out which one of the 3 is the biggest
					if(tsp.getFitness(p1)>tsp.getFitness(p2)){
						if(tsp.getFitness(p1)>tsp.getFitness(p3)){
							//p1 best
							p3 = p1;
						}//else p3 best
						
					}else{
						if(tsp.getFitness(p2)>tsp.getFitness(p3)){
							//p2 best
							p3 = p2;
						}//else p3 best
						
					}
					
					//we have the best newly created solution saved in p3 right now
					
					if(tsp.getFitness(wolfs[i]) > tsp.getFitness(p3)){
						wolfs[i]=p3;
						if(tsp.getFitness(wolfs[i]) > tsp.getFitness(wolfs[nLead])){
							nLead=i;
						}
					}
					
					
				}
			}
			
			//------------------------UPDATING------------------
			//check if we add to the stagnation counter or do we have a new leader
			if(nLead==lead){
				stCounter++;
			}else{
				lead=nLead;
				if(tsp.getFitness(bestWolf) > tsp.getFitness(wolfs[lead])){
					bestWolf = wolfs[lead].clone();
				}
			}
			
			//check if we do scouting behavior on wolf leader
			if(stCounter >= stag){
				stCounter=0;
				//System.out.println("Stagnation on iteration " + g + "!");
				
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
						wolfs[lead].reverseCityEdge(wolfs[lead].getPrevCity(k2), k2, wolfs[lead].getPrevCity(k1), k1);
					}else{
						wolfs[lead].reverseCityEdge(k1, wolfs[lead].getNextCity(k1), k2, wolfs[lead].getNextCity(k2));
					}
					
					
					
					//check if the solution is better, if it is switch the new and the current one
					/*if(tsp.getFitness(wolfs[lead]) > tsp.getFitness(new Solution(n))){
				wolfs[lead] = new Solution(n);
				
				}*/
					
				}
			}
			
			
			//------------------------LOCAL SEARCH------------------
			//REMOVE
			//System.out.println("------------------------LOCAL SEARCH------------------\n\n");
			//the random city we'll select
			int rCity;
			
			//the next city
			int nextCity;
			
			//the selected city
			int LSa;
			
			//the cities on the right for both wolfes
			int p1aR, p2aR;
			
			//we need to save this city so we dont have to search for it later if the first solution wasnt improved
			int otherPart;
			
			
			
			//each wolf once in a pre defined order atm
			for(int w1 = 0 ; w1 < numWolf ; w1++){
				for(int w2 = w1+1 ; w2 < numWolf ; w2++){
					
					//the extra solutions
					Solution p1, p2;
					
					//REMOVE
					//System.out.println("-------------------");
					//selecting the random city
					LSa=rand.nextInt(tsp.howManyCities())+1;
					while( wolfs[w1].getIndexOfCity(LSa)==tsp.howManyCities()-1 || wolfs[w2].getIndexOfCity(LSa)==tsp.howManyCities()-1 ){
						LSa=rand.nextInt(tsp.howManyCities())+1;
					}
					
					//get the next city in wolf 1
					p1aR=wolfs[w1].getNextCity(LSa);
					//get the next city in wolf 2
					p2aR=wolfs[w2].getNextCity(LSa);
					
					//REMOVE
					//System.out.println(String.format("\nw1:%s\nw2:%s\n\nrCity: %d \nnextCity: %d", wolfs[w1], wolfs[w2], rCity, nextCity));
					
					//if both rCity and nextCity are adjecant in both solutions there is no local search to be had
					if(wolfs[w2].getNextCity(LSa)!=p1aR && wolfs[w2].getPrevCity(LSa)!=p1aR){
						
						//check which one comes earlier then reverse the order approprietly
						if(wolfs[w2].getIndexOfCity(LSa) > wolfs[w2].getIndexOfCity(p1aR)){
							otherPart = wolfs[w2].getPrevCity(p1aR);
							p2 = wolfs[w2].clone();
							p2.reverseCityEdge(otherPart, p1aR, wolfs[w2].getPrevCity(LSa), LSa);
							
						}else{
							otherPart = wolfs[w2].getNextCity(p1aR);
							p2 = wolfs[w2].clone();
							p2.reverseCityEdge(LSa, wolfs[w2].getNextCity(LSa), p1aR, otherPart);
							
						}
						
						//REMOVE
						//System.out.println("\nNw:" + new Solution(n) + "\n");
						
						//check if its better, if it is switch w1 else switch w2 and check if w2 is better
						if(tsp.getFitness(wolfs[w2]) > tsp.getFitness(p2)){
							//REMOVE
							//System.out.println(String.format("w2 learns from w1!"));
							wolfs[w2]= p2;
						}else{
							
							//connect otherPart and p1aR in the first solution
							if(wolfs[w1].getIndexOfCity(otherPart) > wolfs[w1].getIndexOfCity(p1aR)){
								p1 = wolfs[w1].clone();
								p1.reverseCityEdge(wolfs[w1].getPrevCity(p1aR), p1aR, wolfs[w1].getPrevCity(otherPart), otherPart);
								
							}else{
								p1 = wolfs[w1].clone();
								p1.reverseCityEdge(otherPart, wolfs[w1].getNextCity(otherPart), p1aR, wolfs[w1].getPrevCity(p1aR));
								
							}
							
							//check which is better
							if(tsp.getFitness(wolfs[w1]) > tsp.getFitness(p1)){
								wolfs[w1]=p1;
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
						bestWolf = wolfs[lead].clone();
					}
				}
			}
		}
		
		
		
		//return best wolf found
		return bestWolf;
		
		//calculate the distances of other positions and kth position
		//implicidly done in tsp
		
		
	}




}









