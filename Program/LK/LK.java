package Program.LK;
/*
LK

EXTRA NOTE:
This one will be hard to figure out from code.
I doubt i'll update it, so good luck if you are attempting to figure stuff out!
*/
//random for integers
import java.util.*;
import Program.TSP.Solution;
import Program.TSP.TSP;
import Program.TSP.Set;
import Program.TSP.NearestNeighbor;

/*
I'll explain how this algorithm works here so it'll be easier to follow it later.
I'll assume the reader understands the basics of Lin-Kernighan.

We have two sets, X and Y. To represent an edge we'll use (c1,c2) where c1 and c2 are cities. x1=(x11,x12) represents the first edge in the X set, 
x2=(x21,x22) the second, x3=(x31,x32), ...
Same goes for Y


The algorithm:
1. Have a random solution (we'll get it through a parameter)
2. Select x1 in some way
	Have no idea how to select this one
3. Remove x1 from the solution
	From now on lets assume that on the y stage we'll be using x12.
	We put x11 as the first element of the solution and x12 as the last element. If we cannot do that we reverse the whole solution then we should be able to do it.
	We'll act as if the first element of the solution and the last one are not connected. We'll have to be very careful with this though.
4. Select y12 in some way
	We'll most likely use either Nearest neighbor or total
5. Calculate the gain from x1 and y1
	This is simple enough, calculate it and save it.
		Although if it is not positive we have to select another y12 (potentially even x1)
6. Now we can also get x2.
	This comes from the (a) cirterion which says that the next x has to be selected as the second part of previous y ( xi1=y(i-1)2 ) and to the side which if we reconnect with x11 will give a tour.
	This xi2 can only be the one on the right of y(i-1)2, so we can easily just say xi2=s.getNextCity(y(i-1)2) of the current solution
7. Now we flip the solution from x22 till y11.
	This from the solution standpoint allready connects the solution with a 2-opt move, but we'll still act as if the first and the last city are NOT connected
8. From here on out its a loop from 4 till here.


Specifics:
One cannot add an edge that was removed in this given step and vice versa (|X| + |Y| = |X U Y|)
y(i-1) must be made so xi is valid (to the above criterion)
The total gain must allways be positive (the total gain is all the gains combined)
Before we construct any yi i>1 we have to check how much the gain would be if we finished at this step
	G* is calculated which is: 
		g*=|yi*|+|xi| (yi* is the edge that connects the solution back together)
		G*=G(i-1)+g*
	if the G* is better then the previous G* replace it and update the best solution (k)


Stop when:
We can no longer connect any things or when Gi<=G*
	If G*>0 at the end replace the tour with the G* tour
Backtracking?


Backtracking:
Selecting y2
Select alternative x2 (probably wont do this one as we'll have to reconnect it weirdly) (actually i can, allow one swap to the left then have to swap right)
Selecting y1
Select alternative x1


Will not have reduciton

Might include non sequential exchanges?

The set for selecting y is nearest neighbor which is of size 5, but is sorted not only from the smallest but also how much x will be at the next stage

		
1. Have a solution on hand
2. Select x1 in some way
3. Remove x1
4. Select y12, since y11 is x12, in some way
5. Connect y1
6. Calculate gain (g1) for x1 and y1
7. Move to the next x and y with some limitations to which we can choose

Its worthwhile noting that we'll be reusing Solution and using it to represent the solution at every submove in LK. Although
solution itself does not allow a city to be connected to 3 other cities we dont need to update the solution in every submove 
in LK, we just need to update


*/
public class LK {
	
	/*
	unlike other heuristics this one will actually be a class, meaning it will hold some data.
	The data that we will hold is the NN list so we dont have to remake it every run.
	Since we're holding things we'll also hold the TSP instance
	*/
	
	public NearestNeighbor NN;
	
	public TSP tsp;
	
	//the constructor, just creates a NN
	public LK(TSP ts, int howManyNN){
		this.NN = new NearestNeighbor(ts, howManyNN);
		this.tsp = ts;
	}
	
	/*
	Given a solution and the backtrack level runs the LK algorithm and returns the best solution found.
	If no improvment was found returns the starting solution.
	Selecting Ys is done by nearest neighbor that has to be initialized beforehand.
	
	Params:
	s			- The starting solution
	backtrack 	- Tells us to which level we'll backtrack
	firstBack	- Tells us how many times we'll backtrack on edge x1 
	
	backtrack levels:
	0001	- backtracking on x1
	0010	- backtracking on alternative pick for x1
	0100	- backtracking on y1
	1000	- backtracking on alternative pick for x2
	backtracking levels can be combined.
	
	*/
	public Solution Lin_Kernighan(Solution s, int backtrack, int firstBack){
		
		//we'll need an alternative solution to keep track of everything and we dont want to change the starting one
		Solution nSol;
		
		//random generator
		Random rand = new Random();
		
		//set for X and Y
		Set set = new Set();
		
		//holds the best gain so far
		double bGain=-1;
		
		//holds the best solution so far
		Solution bSol=s;
		
		//holds the current gain
		double gain;
		
		//extra variables to hold xi1, xi2, yi1 and yi2 at every stage
		int x1,x2,y1,y2=-1;
		
		//variable which holds how many cities the tsp has so we dont have to call the functions over and over
		int numCity=this.tsp.howManyCities();
		
		//this variable keeps track of the stopping condition of not finding any y to add
		boolean yFound;
		
		
		Solution prep;//first stage, need this one so whenever we select alt x12 we need a reference to the first solution
		Solution prep2;//second stage, need this one so whenever we stard selecting y1 we allways start with the same thing
		
		//backup gain for whenever we backtrack
		double backUpGain;
		
		//hold the backtracking levels information in sepearte variables
		boolean bt1=((backtrack&0b0001)==1);
		boolean bt2=((backtrack&0b0010)==2);
		boolean bt3=((backtrack&0b0100)==4);
		boolean bt4=((backtrack&0b1000)==8);
		
		//we need to have a seperate variable for backtracking on atlernative x12 since we have to know at which stage we are if we were to backtrack
		int bt2Inf;
		
		//we need to have a seperate variable for backtracking on atlernative x22 since we have to know at which stage we are if we were to backtrack and we allow an extra move left so it gets more complicated
		int bt4Inf;
		
		//need this variable in case of backtracking 4. This variable represents a kind of a "buffer" which basically allows one move that breaks the sequential rule (the sequential rule is that if the xi2 and x11 are put together the result should be a tour)
		int buff;
		
		//we need another boolean in order to check if we can continue our LK after the 4th backtrack possabilities
		boolean canContinue=true;
		
		//we need this variable to know when we've exhousted all the possabilities for y2. Will also let us know if we backtrack or not
		int yInd;
		
		
		
		//we'll be using do whiles since even if backtracking is off we still have to go through the insides of the loop once
		
		//selecting x11----------------------------
		do{
			//set up backtracking for x12 if its set
			if(bt2){
				bt2Inf=0;
			}else{
				bt2Inf=1;
			}
			
			//decrease firstBack
			firstBack--;
			
			//get x11
			x1=rand.nextInt(numCity)+1;
			
			//set up the solution 
			nSol = s.clone();
			nSol.setFirstCity(x1);
			
			//save this one in second
			prep=nSol.clone();
			
			
			
			//selecting x12/y21----------------------------
			do{
				
				//reset the "loop through y2" possabilities variable
				yInd=1;
				
				
				//the x2 is the last city in the solution
				x2=nSol.getCity(numCity-1);
				
				
				//put the X into our set
				set.addToSet(x1, x2, true);
				
				//save the solution so we can retrive it later
				prep2=nSol.clone();
				
				//selecting y12/x21----------------------------
				do{
					
					//reset solution in case it was dirtied below
					nSol=prep2.clone();
					
					//set up the starting gain
					gain=0;
					
					//set y1 which is last added x 2nd element (its safer this way)
					y1=set.getElementAt(set.getSetSize(true), true).n2;
					
					//get and y2 (NN list from best to worst)
					for(yFound=false; yInd <= this.NN.length() && !yFound; yInd++){
						//if we found a city that is not inside the set yet, allows us to break the next x and has a positive gain we add it to the set
						if( !set.doesSetInclude(y1, this.NN.getCityNN(y1, yInd), true) && ( !set.doesSetInclude(this.NN.getCityNN(y1, yInd), nSol.getNextCity(this.NN.getCityNN(y1, yInd)), true) && !set.doesSetInclude(this.NN.getCityNN(y1, yInd), nSol.getNextCity(this.NN.getCityNN(y1, yInd)), false) && ( nSol.getNextCity(this.NN.getCityNN(y1, yInd))!=y1 ) ) && (this.tsp.getDistance(x1, x2) - this.tsp.getDistance(y1,this.NN.getCityNN(y1, yInd)) >0) ){
							
							//set y2
							y2=this.NN.getCityNN(y1, yInd);
							
							//get the gain
							gain=this.tsp.getDistance(set.getElementAt(1, true).n1, set.getElementAt(1, true).n2) - this.tsp.getDistance(y1,this.NN.getCityNN(y1, yInd));
							
							//check if we do not backtrack
							if(!bt3){
								//if we do not backtrack we make sure we'll never enter this loop
								yInd=this.NN.length()+1;
							}
							//make sure we exit the current loop
							yFound=true;
						}
					}
					
					//we continue here only if we found Y in the previous loop
					if(yFound){
						
						//save gain so we can retrive it later since we'll overwrite it inside the algorithm
						backUpGain=gain;
						
						
						
						//we add to the Y set
						set.addToSet(y1, y2, false);
						//we do not change the solution as of yet since we have one more layer of backtracking
						
						
						
						//set up backtracking for x22 if its set
						if(bt4){
							bt4Inf=0;
						}else{
							bt4Inf=1;
						}
						buff=0;
						
						//selecting x22/y31----------------------------
						do{
							//set up x1 which is the same as the last added y 
							x1=set.getElementAt(set.getSetSize(false), false).n2;
							
							//idk if has to be here, think so
							canContinue=true;
							
							//reset solution in case of backtracking
							nSol=prep2.clone();
							
							
							//if buffer is 1 then we're selecting the alternative x22 which breaks the sequential rule
							if(buff==1){
								
								//if we changed the solution below we have to pick the previous gain we had at the previous level
								gain=backUpGain;
								
								x2=nSol.getPrevCity(x1);
								
								//we'll do the rest of the stuff that happens whenever we go this path in this if statment so the function is smoother
								
								//first we'll check if we can even add this to our set list
								if(!set.doesSetInclude(x1, x2, false) && !set.doesSetInclude(x1, x2, true)){
									//selecting y32--------------------------
									
									
									//add to the set
									set.addToSet(x1, x2, true);
									
									//set y1 as the last added X
									y1=set.getElementAt(set.getSetSize(true), true).n2;
									
									//set up yFound
									yFound=false;
									
									for(int yIndex = 1; yIndex <= this.NN.length() && !yFound; yIndex++){
										
										//if we found a city that is not inside the set yet, allows us to break the next x and has a positive gain we add it to the set
										if( !set.doesSetInclude(y1, this.NN.getCityNN(y1, yIndex), true) && ( !set.doesSetInclude(this.NN.getCityNN(y1, yIndex), nSol.getNextCity(this.NN.getCityNN(y1, yIndex)), true) && !set.doesSetInclude(this.NN.getCityNN(y1, yIndex), nSol.getNextCity(this.NN.getCityNN(y1, yIndex)), false) && ( nSol.getNextCity(this.NN.getCityNN(y1, yIndex))!=y1 ) ) && (gain + this.tsp.getDistance(x1, x2) - this.tsp.getDistance(y1,this.NN.getCityNN(y1, yIndex)) >0) ){
											
											//set y2
											y2=this.NN.getCityNN(y1, yIndex);
											
											//calculate gain
											gain+=this.tsp.getDistance(x1, x2) - this.tsp.getDistance(y1,this.NN.getCityNN(y1, yIndex));
											
											//get out of the loop
											yFound=true;
										}
									}
									
									//check whenever we found an Y
									if(yFound){
										
										//add to set
										set.addToSet(y1, y2, false);
										
										//check where the city is compared to what we currently have in x1
										if(nSol.getIndexOfCity(x1) > nSol.getIndexOfCity(y2)){
											//if its before then we've used up our buffer and we have to select another city, this one has to come after x1
											
											//we can easily combine this code with the next one if we just add to the set some things
											
											//first we'll add to the set the upcomming X
											x1=y2;
											x2=nSol.getNextCity(x1);
											
											
												
												set.addToSet(x1, x2, true);
												
												//set y1 as the 2nd element of the last added X
												y1=set.getElementAt(set.getSetSize(true), true).n2;
												
												//reset yFound
												yFound=false;
												
												//get another Y
												for(int yIndex = 1; yIndex <= this.NN.length() && !yFound; yIndex++){
													//if we found a city that is not inside the set yet, allows us to break the next x, is at the correct location and has a positive gain we add it to the set
													if( !set.doesSetInclude(y1, this.NN.getCityNN(y1, yIndex), true) && ( !set.doesSetInclude(this.NN.getCityNN(y1, yIndex), nSol.getNextCity(this.NN.getCityNN(y1, yIndex)), true) && !set.doesSetInclude(this.NN.getCityNN(y1, yIndex), nSol.getNextCity(this.NN.getCityNN(y1, yIndex)), false) && ( nSol.getNextCity(this.NN.getCityNN(y1, yIndex))!=y1 ) ) && ( nSol.getIndexOfCity(set.getElementAt(2, true).n1) <= nSol.getIndexOfCity(this.NN.getCityNN(y1, yIndex)) ) && (gain + this.tsp.getDistance(x1, x2) - this.tsp.getDistance(y1,this.NN.getCityNN(y1, yIndex)) >0) ){
														
														//set y2
														y2=this.NN.getCityNN(y1, yIndex);
														
														//calculate gain
														gain+=this.tsp.getDistance(x1, x2) - this.tsp.getDistance(y1,this.NN.getCityNN(y1, yIndex));
														
														//get out of the loop
														yFound=true;
													}
												}
												
												if(yFound){
													
													//we add the Y we just found
													set.addToSet(y1, y2, false);
													
													//set up next x1 and x2
													x1=set.getElementAt(set.getSetSize(false), false).n2;
													x2=nSol.getNextCity(x1);
													
													//add X to the set
													set.addToSet(x1, x2, true);
													
													//now we have to "fix" our solution (do the rotations)
													//reverse x12, x42
													nSol.reverse(nSol.getIndexOfCity(set.getElementAt(1 ,true).n2), nSol.getIndexOfCity(set.getElementAt(4 ,true).n2));
													
													//reverse x41, x21
													nSol.reverse(nSol.getIndexOfCity(set.getElementAt(4 ,true).n1), nSol.getIndexOfCity(set.getElementAt(2 ,true).n1));
													
													//reverse x22, x32
													nSol.reverse(nSol.getIndexOfCity(set.getElementAt(2 ,true).n2), nSol.getIndexOfCity(set.getElementAt(3 ,true).n2));
													
													
												}else{//yFound was not found, this is the 2nd edge to add to fix the alternative x2 pick
													
													//the algorithm cannot continue anymore
													canContinue=false;
													//reduce the gain the last added Y
													gain+=this.tsp.getDistance(set.getElementAt(set.getSetSize(false), false).n1, set.getElementAt(set.getSetSize(false), false).n2) - this.tsp.getDistance(set.getElementAt(set.getSetSize(true), true).n1, set.getElementAt(set.getSetSize(true), true).n2);
													//remove from set the last addex X and Y
													set.removeLastFewElement(1, true);
													set.removeLastFewElement(1, false);
													
													
													
												}
												
											
										}else{//The y2 selected does not use the buffer
											
											//set x1 and x2
											x1=set.getElementAt(set.getSetSize(false), false).n2;
											x2=nSol.getNextCity(x1);
											//add the x to the set
											set.addToSet(x1, x2, true);
											
											//update solution
											//reverse x21, x31
											nSol.reverse(nSol.getIndexOfCity(set.getElementAt(2, true).n1), nSol.getIndexOfCity(set.getElementAt(3, true).n1));
											
											//reverse x32, x12
											nSol.reverse(nSol.getIndexOfCity(set.getElementAt(3, true).n2), nSol.getIndexOfCity(set.getElementAt(1, true).n2));
											//CHECK BEST GAIN
											
										}
									}else{//We have not found an Y for the alternative x2
										
										//we dont have to reduce gain
										
										//if not found remove from the set the X as it could not find and Y
										set.removeLastFewElement(1, true);
										//and disallow continuing this path
										canContinue=false;
									}
									
									
									
									
								}else{
									//we cannot select the previous x22
									canContinue=false;
								}
								
								
							}else{//we selected the x which allows us to reconnect the solution if we were to connect xlast2 and x11
								
								//get x2
								x2=nSol.getNextCity(x1);
								
								//add x to the set
								set.addToSet(x1, x2, true);
								
								//we update the solution
								//reverse x12 x22
								nSol.reverse(nSol.getIndexOfCity(set.getElementAt(1, true).n2), nSol.getIndexOfCity(set.getElementAt(2, true).n2));
								
							}
							
							
							while(canContinue){
								
								//this is where our algorithm goes on its loop thingi
								
								//set x1 and x2 to be the last x added
								x1=set.getElementAt(set.getSetSize(true), true).n1;
								x2=set.getElementAt(set.getSetSize(true), true).n2;
								
								//check best gain (current gain + last added x (xn1, xn2) + the connection to form a tour (xn2, x11))
								if(gain + tsp.getDistance(x1, x2) - tsp.getDistance(x2, set.getElementAt(1, true).n1) > bGain){
									//set new gain
									
									bGain=gain + tsp.getDistance(set.getElementAt(set.getSetSize(true), true).n1, set.getElementAt(set.getSetSize(true), true).n2) - tsp.getDistance(set.getElementAt(set.getSetSize(true), true).n2, set.getElementAt(1, true).n1);
									
									//set best solution (which is already the current solution)
									bSol=nSol.clone();
								}
								
								
								
								//select y (if no y can be found end)
								//set yn1 (which is x(n-1)2)
								y1=x2;
								
								//reset yFound
								yFound=false;
								
								//find the y
								for(int yIndex = 1; yIndex <= this.NN.length() && !yFound; yIndex++){
									//if we found a city that is not inside the set yet, allows us to break the next x, is at the correct location and has a positive gain we add it to the set
									if( !set.doesSetInclude(y1, this.NN.getCityNN(y1, yIndex), true) && ( !set.doesSetInclude(this.NN.getCityNN(y1, yIndex), nSol.getNextCity(this.NN.getCityNN(y1, yIndex)), true) && !set.doesSetInclude(this.NN.getCityNN(y1, yIndex), nSol.getNextCity(this.NN.getCityNN(y1, yIndex)), false) && ( nSol.getNextCity(this.NN.getCityNN(y1, yIndex))!=y1 ) ) &&  (gain + this.tsp.getDistance(x1, x2) - this.tsp.getDistance(y1,this.NN.getCityNN(y1, yIndex)) >0) ){
										
										//set y2
										y2=this.NN.getCityNN(y1, yIndex);
										
										//calculate gain
										gain+=this.tsp.getDistance(x1, x2) - this.tsp.getDistance(y1,this.NN.getCityNN(y1, yIndex));
										
										//get out of the loop
										yFound=true;
									}
								}
								
								if(yFound){
									
									//we get the x (which we should if we found the Y)
									x1=y2;
									x2=nSol.getNextCity(x1);
									
									//gain already calculated when we selected Y
									
									//update solution
									nSol.reverse(nSol.getIndexOfCity(x2), nSol.getIndexOfCity(y1));
									
									//add to set
									set.addToSet(y1,y2,false);
									set.addToSet(x1,x2,true);
									
									//check gain stopping criterion
									if(bGain >= gain){
										canContinue=false;
									}
									
								}else{
									//if we did not find an Y we stop the LK algorithm with this configuration
									canContinue=false;
								}
								
							}
							
							
							
							//revert back to the prep soltuion with X and Y having 1 element 
							if(set.getSetSize(false)>1){
								set.removeLastFewElement(set.getSetSize(false)-1, false);
							}
							if(set.getSetSize(true)>1){
								set.removeLastFewElement(set.getSetSize(true)-1, true);
							}
							
							
							
							
							//increase the backtrack and the buffer
							bt4Inf++;
							buff++;
							
						}while(bt4Inf<2);
						
						
					}else{
						
					}
					
					//if we backtrack we have to remove the Y we found (if we found it, thats why we check whenever we did or not)
					if(set.getSetSize(false)>0){
						set.removeLastFewElement(1, false);
					}
					
				}while(yInd<=this.NN.length());
				
				//bt2Inf==0 then we have to prepare for the backtracking, we could always do it since backtracking would reset but this is a trade of an if for all the things listed below
				if(bt2Inf==0){
					
					//this should set up the next run of the algorithm
					nSol=prep.clone();
					
					//set up x1
					x1=nSol.getCity(0);
					nSol.reverseAll();
					nSol.shiftRight();
					
					
				}
				//we remove the previously added X (should be fine as we're reducing the set at higher levels whenever we backtrack or finish)
				set.removeLastFewElement(1, true);
				bt2Inf++;
				
			}while(bt2Inf<2);
			
			
		}while(firstBack>0 && bt1);
		
		
		//if we found a better solution return it else return the starting one
		if(bGain>0){
			//System.out.printf("\nCalculation: %.1f-%.1f=%.1f\n", this.tsp.getFitness(s), bGain, this.tsp.getFitness(bSol));
			return bSol;
		}else{
			return s;
		}
		
	}
	
}


