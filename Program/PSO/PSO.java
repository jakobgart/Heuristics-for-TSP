package Program.PSO;
/*
Particle Swarm Optimization

Suggest reading the PSOLK before PSO2OPT as its more commented

*/
//random for integers
import java.util.*;
//random for doubles
import java.lang.Math;
import Program.TSP.Solution;
import Program.TSP.TSP;
import Program.LS.*;
import Program.LK.*;

public class PSO {
	
	//The particle class
	public static class Particle{
		
		//the paritcles current solution
		public Solution s;
		
		//the fitness of the current solution
		public double sFitness;
		
		//the best solution of the particle
		public Solution best;
		
		//the fitness of the best particle
		public double bestFitness;
		
		//create a new particle with a random solution
		public Particle(int n, TSP tsp){
			this.s=new Solution(n);
			this.best=this.s.clone();
			this.sFitness=tsp.getFitness(this.s);
			this.bestFitness=this.sFitness;
		}
		
		//create a new particle with a given solution
		public Particle(Solution sol, TSP tsp){
			this.s=sol.clone();
			this.best=this.s.clone();
			this.sFitness=tsp.getFitness(this.s);
			this.bestFitness=this.sFitness;
		}
		
		//updates the best particle with the current one
		public void updateBest(){
			this.best=s.clone();
			this.best.timeMS=this.s.timeMS;
			this.bestFitness=this.sFitness;
		}
		
		//adds a new current solution
		public void newSolution(Solution sol, TSP tsp){
			this.s=sol.clone();
			this.sFitness=tsp.getFitness(this.s);
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	/*
	tsp			- the TSP
	numPar		- number of particles
	iter		- number of total iterations
	?m			- number of iterations with specific p parameters
	stepF		- holds the step factor
	inv			- holds the upper limit of elements to invert during local search (from 2 to howmanycities TSP has)
	p1			- the starting value of p1
	p1F			- the p1 factor by which its multiplied
	p2			- the starting value of p2
	p2F			- the p2 factor by which its multiplied
	p3			- the starting value of p3
	p3F			- the p3 factor by which its multiplier
	amount2opt	- the amount of 2 opt choices
	*/
	public static Solution ParticleSwarm2Opt(TSP tsp, int numPar, int iter, double stepF, int inv, double p1, double p1F, double p2, double p2F, double p3, double p3F, int amount2opt){
		
		//a random generator, used in element reversal when a particle chooses its own way
		Random rand = new Random();
		
		//this variable holds the random number generated for each particle as in which move they'll pick
		double move=0.0;
		
		//this variable helps normalize p1 p2 and p3
		double norm=0.0;
				
		//normalize the current probabilites if they're currenty not
		norm=p1+p2+p3;
		p1=p1/norm;
		p2=p2/norm;
		p3=p3/norm;	
		
		//this variable holds the index of the best particle
		int best=0;
		
		//create starting solutions (random)
		Particle[] part = new Particle[numPar];
		for(int i = 0 ; i < numPar; i++){
			part[i] = new Particle(tsp.howManyCities(), tsp);
			if(tsp.getFitness(part[best].s) > tsp.getFitness(part[i].s)){
				best=i;
			}
		}
		
		//loop for iterations
		for(int it = 0 ; it < iter ; it++){
			
			//loop through particles and make their decision
			for(int i = 0 ; i < numPar ; i++){
				move=Math.random();
				
				//check if it selects step 1
				if(move<p1){
					
					for(int zz = 0 ; zz < inv ; zz++){
						
						part[i].newSolution(kopt.firstBest2Opt(part[i].s, tsp, amount2opt), tsp);
					}
					
				}else if(move<(p1+p2)){//check if it selects step 2
					
					part[i].newSolution(pathRelink(part[i].s, part[i].best, stepF), tsp);
					
				}else{//if it does not select step 1 and step 2 it selects step 3
					
					//best cannot move to itself
					if(i!=best){
						part[i].newSolution(pathRelink(part[i].s, part[best].best, stepF), tsp);
					}
				}
				
				//potentially update the personal best
				if(part[i].bestFitness > part[i].sFitness){
					part[i].updateBest();
				}
				
				//potentially update the global best
				if(part[best].bestFitness > part[i].bestFitness){
					best=i;
				}
				
				
				
			}
			
			//update p1 p2 and p3
			p1=p1*p1F;
			p2=p2*p2F;
			p3=p3*p3F;
			
			norm=p1+p2+p3;
			
			//normalize them
			p1=p1/norm;
			p2=p2/norm;
			p3=p3/norm;
			
		}
		
		
		//return best
		return part[best].best;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	same as above, but for LK (also a bit more commented)
	
	tsp			- the TSP
	numPar		- number of particles
	iter		- number of total iterations
	?m			- number of iterations with specific p parameters
	stepF		- holds the step factor
	inv			- holds the amount of LK steps we'll make
	p1			- the starting value of p1
	p1F			- the p1 factor by which its multiplied
	p2			- the starting value of p2
	p2F			- the p2 factor by which its multiplied
	p3			- the starting value of p3
	p3F			- the p3 factor by which its multiplier
	LKBacktrack	- LK backtrack code
	LKnum		- the number of times LK should select the first city
	*/
	public static Solution ParticleSwarmLK(TSP tsp, int numPar, int iter, double stepF, int inv, double p1, double p1F, double p2, double p2F, double p3, double p3F, int LKBacktrack, int LKnum){
		
		
		//The lin kerning instance so we can call the algorithm later
		LK lk = new LK(tsp, 10);
		
		//a random generator, used in element reversal when a particle chooses its own way
		Random rand = new Random();
		
		//this variable holds the random number generated for each particle as in which move they'll pick
		double move=0.0;
		
		//this variable helps normalize p1 p2 and p3
		double norm=0.0;
		
		//normalize the current probabilites if they're currenty not
		norm=p1+p2+p3;
		p1=p1/norm;
		p2=p2/norm;
		p3=p3/norm;	
		
		
		//this variable holds the index of the best particle
		int best=0;
		
		//create starting solutions (random)
		Particle[] part = new Particle[numPar];
		for(int i = 0 ; i < numPar; i++){
			part[i] = new Particle(tsp.howManyCities(), tsp);
			if(part[best].sFitness > part[i].sFitness){
				best=i;
			}
		}
		
		//loop for the amount of iterations
		for(int it = 0 ; it < iter ; it++){
			
			
			//loop through particles and make their decision
			for(int i = 0 ; i < numPar ; i++){
				move=Math.random();
				
				//check if the particle selects step 1 (its own way)
				if(move<p1){
					
					//we do LK a certian number of times
					for(int z = 0 ; z < inv ; z++){
						part[i].newSolution(lk.Lin_Kernighan(part[i].s, LKBacktrack, LKnum), tsp);
					}
					
					//else check if the particle selects step 2 (it'll go to its personal best)
				}else if(move<(p1+p2)){
					
					part[i].newSolution(pathRelink(part[i].s, part[i].best, stepF), tsp);
					
					//else its step 3 (go to global best)
				}else{
					
					//only if the current solution isnt also the best one, then they just stay still
					if(i!=best){
						part[i].newSolution(pathRelink(part[i].s, part[best].best, stepF), tsp);
					}
				}
				
				//potentially update the personal best
				if(part[i].bestFitness > part[i].sFitness){
					part[i].updateBest();
				}
				
				//potentially update the global best
				if(part[best].bestFitness > part[i].bestFitness){
					best=i;
				}
				
			}
			
			//update p1 p2 and p3
			p1=p1*p1F;
			p2=p2*p2F;
			p3=p3*p3F;
			
			norm=p1+p2+p3;
			
			//normalize them
			p1=p1/norm;
			p2=p2/norm;
			p3=p3/norm;
			
		}
		
		
		//return the best solution
		return part[best].best;
	}
	
	
	
	
	
	
	
	
	
	
	/*
	Path relinking.
	Given a starting solution, target solution and step factor does this:
	Check the target solution x-th city and identify at which position this city is in the starting solution.
	if the selected city is on an index that is higher (meaning it comes) in the starting solution then the target solution take this city in the starting solution and move it left once.
		if its on the left move it right in the starting solution.
	Continue this untill either the solutions are the same or we've reached the end of our relinking (limited number of steps).
	
	start		- Starting solution
	target		- Target solution
	stepFactor	- Assuming you can get an approxiate amount of steps you'd have to do in order to move from the start to target, multiply that number by this factor and we obtain the amount of steps
	*/
	public static Solution pathRelink(Solution start, Solution target, double stepFactor){
		
		//create a clone of the city path in the starting solution
		Solution nSol = start.clone();
		
		//calculate the approximate amount of steps
		int steps = (int) (calcDifference(start, target)*stepFactor);
		
		//this variable represents the index of which current city do we want to match
		int curIndex=0;
		
		//loop which goes through all the step of path relinking
		for(int i = 0 ; i < steps && curIndex < nSol.howManyCities() ; i++){
			
			//The first case is whenever the cities are in the same poision, that means we can move onto the next position
			if(nSol.getCity(curIndex)==target.getCity(curIndex)){
				//we move onto the next position by increasing curIndex
				curIndex++;
				
				//we check whenever the city in the current position in the target city is in the later part of the solution in the currrent solution
			}else if(nSol.getIndexOfCity(target.getCity(curIndex)) > curIndex){
				
				//we swap left the selected city in the current solution
				nSol.swapLeft(target.getCity(curIndex));
				
				//else the city is on the former part of the solution in the current solution
			}else{
				nSol.swapRight(target.getCity(curIndex));
			}
			
		}
		
		return nSol;
		
	}
	
	/*
	Returns the sum of the differences of index for all cities of the two given solutions.
	*/
	private static int calcDifference(Solution s1, Solution s2){
		int sum=0;
		for(int i = 1 ; i <= s1.howManyCities(); i++){
			sum+=(Math.abs(s2.getIndexOfCity(i)-s1.getIndexOfCity(i)));
		}
		return sum;
		
	}
	
}