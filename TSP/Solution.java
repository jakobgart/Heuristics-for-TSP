package Program.TSP;
/*
This class creates and/or holds a solution
*/
//for random and arraylist
import java.util.*;
//for time
import java.time.*;
//for log
import java.lang.Math;
//for error
import java.lang.*;

/*
Potential updates:
check if  solution is valid (contains cities from 1 to nodeNum, no doubles and no extras)
Add some clarity regarding when we use city numbers and when we use indexes
*/
public class Solution {

	//the amount of cities in the solution
	public int nodeNum;
	
	//array holds the solution in order. tour[0] will give the first city in the solution
	public int[] tour;
	
	//array holds in which position (1, 2, ..., nodeNum) in the tour array a city is. city[0] will give the position of city 1 in tour array
	/*
	example:
	city[0] being 4 means the 4th city in tour[] will be city 1. That is:
	tour[3] will be 1
	*/
	public int[] city;
	
	//time when this solution was created or last modified
	public long timeMS;
	
	
	//makes a new random solution
	public Solution(int num){
		this.nodeNum=num;
		this.makeRandomSolution();
		this.updateTime();
	}
	
	//creates a new solution based on a given array (we do not check for the correctess of the array)
	public Solution(int[] t){
		this.nodeNum=t.length;
		this.tour=t.clone();
		this.updateCityPosition();
		this.updateTime();
	}
	
	//same as above, but we can also can get the city list
	public Solution(int[] t, int[] c){
		this.nodeNum=t.length;
		this.tour=t.clone();
		this.city=c.clone();
		this.updateTime();
	}
	
	
	//updates time
	public void updateTime(){
		this.timeMS=System.currentTimeMillis();
	}
	
	//makes the current solution a random one
	public void makeRandomSolution(){
		
		Random rand = new Random();
		
		ArrayList<Integer> a = new ArrayList<Integer>();
		
		//create the tour and city array
		this.tour = new int[this.nodeNum];
		this.city = new int[this.nodeNum];
		
		int index=-1;
		
		//create an order array list with values from 1 to nodeNum
		for(int i=1; i <= nodeNum ; i++){
			a.add(i);
		}
		
		//creates a random solution
		for(int i=0; i < nodeNum ; i++){
			index=rand.nextInt(nodeNum-i);
			this.tour[i]=a.get(index);
			this.city[this.tour[i]-1]=(i+1);
			a.remove(index);
		}
		
		
		
	}
	
	//updates city array if the solution has gotten a new tour array
	public void updateCityPosition(){
		//make new array
		this.city = new int[this.nodeNum];
		
		//fill i tup
		for(int i = 0 ; i < this.nodeNum ; i++){
			this.city[this.tour[i]-1]=(i+1);
		}
		
	}
	
	//returns the solution (for debuging purposes)
	public String toString(){
		//sets up the format
		String outFormat = "%" + ((int)Math.log10(nodeNum)+1) + "d";
		
		String ret = "";
		for(int i = 0 ; i < this.nodeNum ; i++){
			ret=ret+String.format(outFormat, this.tour[i]);
			if(i<this.tour.length-1){
				ret=ret+", ";
			}
		}
		return ret;
	}
	
	//similar as above, but for the city array
	public String toStringCity(){
		//sets up the format
		String outFormat = "%" + ((int)Math.log10(nodeNum)+1) + "d -> %" + ((int)Math.log10(nodeNum)+1) + "d";
		
		String ret = "";
		for(int i = 0 ; i < this.nodeNum ; i++){
			ret=ret+String.format(outFormat, i+1, this.city[i]);
			if(i<this.tour.length-1){
				ret=ret+", ";
			}
		}
		return ret;
	}
	
	//gets the i-th city in the tour 
	//if the param is not within the boundries of the solution it modules it by the size of the array
	//this is due to us sometimes wanting the city following the one on a given index and this allows to be lazy and not need to check if the currently selected index is the last one while still being correct
	//!!! Apperently did not work on negative, so we have to include a while statment (realistically should only be used once total, but just in case we'll make it a while). Very lazy.
	public int getCity(int i){
		while(i%this.nodeNum<0){
			i=i+this.nodeNum;
		}
		return this.tour[i%this.nodeNum];
	}
	
	//reutrns how many cities this solution holds (same thing as in TSP)
	public int howManyCities(){
		return this.nodeNum;
	}
	
	//returns the index of a city in a tour (returns -1 if not found) in the range of [0, 1, ..., howManyCities-1]
	public int getIndexOfCity(int c){
		if(c>this.nodeNum){
			return -1;
		}
		return this.city[c-1]-1;
	}
	
	//returns the city coming after the city given as the param
	public int getNextCity(int c){
		
		//get the index of the current city
		int ind=this.getIndexOfCity(c);
		
		//this way even if we get the last city in the solution we return the first city
		return this.getCity(ind+1);
		
		
	}
	
	//returns the city coming before the city given as the param (similar as above)
	public int getPrevCity(int c){
		
		int ind=this.getIndexOfCity(c);
		
		return this.getCity(ind-1);
		
		
	}
	
	
	/*
	Method reverses part of the solution and updates the time of the solution.
	Params:
	str - the starting index of the reversal (the start city included in the reversal)
	end - the ending index of the reversal (the ending node included in the reversal)
	
	Example
	Current solution: [1 2 3 4 5 6 7 8]
	Input:
	str = 3
	end = 5
	Output: [1 2 3 6 5 4 7 8]
	
	Potential improvments:
	1.
	Figure out whats better to reverse (from str to end or from end to str)
	Create a copy of the solution and only reverse the cheaper part
	This works since solution [1 2 3 4 5] could be reversed given str and end (2,4) to either [1 2 5 4 3] or [2 1 5 4 3].
	
	*/
	public void reverse(int start, int end){
		
		//check if out of bounds
		if(start<0 || end>=this.nodeNum){
			throw new java.lang.Error(String.format("Cannot reverse, indexes out of bounds.\nSolution:%s\nFrom:%d\nTo:%d",this.toString(),start,end));
		}
		
		//if the start of reversal is before the end of reversal
		if(start>end){
			this.reverse(end,start);
			return;
		}
		
		
		//create a temporary array of the length of the reversed part
		int[] n = new int[end-start+1];
		
		//save the reversed part in the temporary array
		for(int i = end ; i >= start ; i--){
			n[end-i] = this.tour[i];
		}
		
		//save the temporary array in the solution and update the city array
		for(int i = 0 ; i < (end-start+1) ; i++){
			this.tour[start+i] = n[i];
			//update the city array
			this.city[this.tour[start+i]-1]=start+i+1;
		}
		
		
		//update time of the current solution
		this.updateTime();
		
	}
	
	/*
	reverses the cities between the edges (a1, a2) and (b1, b2).
	Uses cities, not indexes
	
	example:
	TSP =	[1 2 3 4 5 6 7 8]
			[0 1 2 3 4 5 6 7]
	a1 =	2
	a2 =	3
	b1 =	6
	b2 =	7
	output:	[1 2 6 5 4 3 7 8]
	
	Same output if a[x] and b[x] were exchanged
	Same output if a/b[0] and (the same edge) a/b[1] were exchanged
	
	*/
	public void reverseCityEdge(int a1, int a2, int b1, int b2){
		
		
		
		//we check a and b edges and order them
		if( this.getNextCity(a1)!=a2 ){
			int temp = a1;
			a1 = a2;
			a2 = temp;
		}
		if( this.getNextCity(b1)!=b2 ){
			int temp = b1;
			b1 = b2;
			b2 = temp;
		}
		
		
		//check whenever they're next to eachother, if not throw error
		if(this.getNextCity(a1)!=a2 || this.getNextCity(b1)!=b2){
			throw new java.lang.Error(String.format("Cannot reverse, one of the two cities are not next to each other. \nSolution:\n%s\nEdges:\nA: (%d, %d)\nB: (%d, %d)\n",this.toString(), a1, a2, b1, b2));
		}
		
		//check for overlap of edges
		if(a1==b1){
			throw new java.lang.Error(String.format("Cannot reverse, the edges given are the same. \nSolution:\n%s\nEdges:\n(%d, %d)\n(%d, %d)\n",this.toString(), a1, a2, b1, b2));
		}
		
		//order the two edges so a comes before b
		if(this.getIndexOfCity(a1) > this.getIndexOfCity(b1)){
			int temp = a1;
			a1=b1;
			b1=temp;
			temp=a2;
			a2=b2;
			b2=temp;
		}
		
		//System.out.printf("\na:(%d, %d)\nb:(%d, %d)\n", a1, a2, b1, b2);
		
		//length of the reverse
		int len=this.getIndexOfCity(b1)-this.getIndexOfCity(a1);
		
		//we need to save the position of city a2 so we can correctly change the city array
		int a2Pos = this.getIndexOfCity(a2);
		
		//the array which holds the reversed cities
		int[] rev = new int[len];
		
		//fill up the array and update city
		for(int i = 0 ; i < len ; i++){
			rev[i]=this.tour[a2Pos+len-1-i];
			city[this.tour[a2Pos+len-1-i]-1]=a2Pos+i+1;
		}
		
		//replace the array
		for(int i = 0 ; i < len ; i++){
			this.tour[a2Pos+i]=rev[i];
		}
		
		this.updateTime();
		
	}
	
	//same as above but reverses everything
	public void reverseAll(){
		this.reverse(0, this.nodeNum-1);
	}
	
	
	//returns a clone of the solution (refreshes the timer)
	public Solution clone(){		
		return new Solution(this.tour, this.city);
	}
	
	//shifts the solution to the right by one ([1 2 3 4] -> [2 3 4 1])
	public void shiftLeft(){
		
		//temporarly hold the first solution in the tour
		int temp=this.tour[0];
		
		//shift to the left and fix the tour and city array
		for(int i = 1 ; i < this.nodeNum ; i++){
			this.tour[i-1]=this.tour[i];
			this.city[this.tour[i]-1]--;//=this.city[this.tour[i]-1]-1;
		}
		
		//fix the last city
		this.tour[this.nodeNum-1]=temp;
		//the temporary city was previously the first in the solution, so now it will be the last
		this.city[temp-1]=this.nodeNum;
		
	}
	
	//shifts the solution to the right by one ([1 2 3 4] -> [4 1 2 3])
	public void shiftRight(){
		//same as above but for shift right
		
		int temp=this.tour[this.nodeNum-1];
		
		for(int i = this.nodeNum-2 ; i >= 0 ; i--){
			this.tour[i+1]=this.tour[i];
			//System.out.println(this.tour[i]-1);
			this.city[this.tour[i]-1]++;//=this.city[this.tour[i]-1]+1;
		}
		
		this.tour[0]=temp;
		this.city[temp-1]=1;
		
	}
	
	//Given a city c we change the solution so the city c is the first city in the sequence
	public void setFirstCity(int c){
		
		//basically we'll shift left untill we get our desired outcome
		//but we'll do it in a better way then calling shiftLeft() over and over
		
		//first we'll check whenever the currect city is allready first, if it is return
		if(this.tour[0]==c){
			return;
		}
		
		//get the index of the city right now (should allways be >0)
		int cPosition = this.city[c-1]-1;
		
		//we'll create a new array which holds the shifted solution
		int[] newArray = new int[this.nodeNum];
		
		//fill up the array
		for(int i = 0 ; i < nodeNum ; i++){
			//put in i-th position the city currently at the position (i+cPosition)%nodeNum
			newArray[i]=this.tour[(i+cPosition)%nodeNum];
			//update city
			this.city[this.tour[(i+cPosition)%nodeNum]-1]=i+1;
		}
		
		//replace the tour of the solution
		this.tour=newArray;
		
		
		
	}
	
	//makes a random double bridge move on the solution
	public void randomDoubleBridgeMove(){
		
		Random rand = new Random();
		
		int[] cit = new int[4];
		
		//get 4 unique indexes of cities
		cit[0]=rand.nextInt(this.nodeNum);
		
		
		cit[1]=rand.nextInt(this.nodeNum);		
		while(cit[0]==cit[1]){
			cit[1]=rand.nextInt(this.nodeNum);
		}
		
		
		cit[2]=rand.nextInt(this.nodeNum);		
		while(cit[2]==cit[1] && cit[2]==cit[0]){
			cit[2]=rand.nextInt(this.nodeNum);
		}
		
		
		cit[3]=rand.nextInt(this.nodeNum);		
		while(cit[3]==cit[2] && cit[3]==cit[1] && cit[3]==cit[0]){
			cit[3]=rand.nextInt(this.nodeNum);
		}
		
		//sort them
		Arrays.sort(cit);
		
		//preform the move
		this.reverse(cit[0]+1, cit[1]);
		this.reverse(cit[1]+1, cit[2]);
		this.reverse(cit[2]+1, cit[3]);
		this.reverse(cit[0]+1, cit[3]);
		
	}
	
	
	/*
	Given a city x swaps it with the city on the left of it.
	*/
	public void swapLeft(int c){
		
		//temporary variable for swap
		int temp;
		
		//holds the previous city
		int prevC=this.getPrevCity(c);
		
		//check whenever the city to swap is the first city in the solution
		if(this.tour[0]==c){
			
			//correct the values in tour
			this.tour[0]=prevC;
			this.tour[this.nodeNum-1]=c;
			
			//then correct the city array
			this.city[c-1]=this.nodeNum;
			this.city[prevC-1]=1;
			
			
		}else{//same as above but for any other city
		
			//correct the values in tour
			this.tour[this.getIndexOfCity(c)]=prevC;
			this.tour[this.getIndexOfCity(prevC)]=c;
			
			//then correct the city array
			this.city[c-1]--;
			this.city[prevC-1]++;
			
		}
		
		//update time as it actually changes the solution
		this.updateTime();
		
	}
	
	//Same as above but swap to the right
	public void swapRight(int c){
		
		//temporary variable for swap
		int temp;
		
		//holds the previous city
		int nextC=this.getNextCity(c);
		
		//check whenever the city to swap is the first city in the solution
		if(this.tour[this.nodeNum-1]==c){
			
			//correct the values in tour
			this.tour[0]=c;
			this.tour[this.nodeNum-1]=nextC;
			
			//then correct the city array
			this.city[c-1]=1;
			this.city[nextC-1]=this.nodeNum;
			
			
		}else{//same as above but for any other city
		
			//correct the values in tour
			this.tour[this.getIndexOfCity(c)]=nextC;
			this.tour[this.getIndexOfCity(nextC)]=c;
			
			//then correct the city array
			this.city[c-1]++;
			this.city[nextC-1]--;
			
		}
		
		//update time as it actually changes the solution
		this.updateTime();
	}
	
	
	/*
	inserts city c2 to the left of city c1
	
	example 1:
	TSP =	[1 2 3 4 5 6 7 8]
	c1	=	3
	c2	=	7
	output:	[1 2 7 3 4 5 6 8]
	
	example 2:
	TSP =	[1 2 3 4 5 6 7 8]
	c1	=	5
	c2	=	2
	output:	[1 3 4 2 5 6 7 8]
	
	*/
	public void insertOnTheLeft(int c1, int c2){
		//get the index where c1 and c2 currently are at
		int indc1 = this.getIndexOfCity(c1);
		int indc2 = this.getIndexOfCity(c2);
		
		//fix city and tour array
		//case where c1 comes before c2
		if(indc1 < indc2){
			
			//fix city array, just add 1 to [c1, c2)
			for(int i = 0 ; i < (indc2-indc1) ; i++){
				this.city[this.tour[indc1+i]-1]++;
			}
			//and also the current city (which will now be in the position that c1 was at the start) (we have to add +1 so its consistent with how city array and getIndexOfCity works)
			this.city[c2-1]=indc1+1;
			
			//fix tour array, overwrite cities [c1, c2) to the ones on the right
			for(int i = 1 ; i <= (indc2-indc1) ; i++){
				this.tour[indc2-i+1]=this.tour[indc2-i];
			}
			//also fix the current city
			this.tour[indc1]=c2;
			
		}else{//case where c1 comes after c2
		
			//fix city array, just remove 1 to (c2, c1) 
			for(int i = 1 ; i < (indc1-indc2) ; i++){
				this.city[this.tour[indc2+i]-1]--;
			}
			//and also the current city (which will now be in the position that c1 was at the start)
			this.city[c2-1]=indc1;
			
			//fix tour array, overwrite cities (c2, c1) to the ones on the left
			for(int i = 1 ; i < (indc1-indc2) ; i++){
				this.tour[indc2+i-1]=this.tour[indc2+i];
			}
			//also fix the current city
			this.tour[indc1-1]=c2;
			
			
		}
		
		this.updateTime();
		
	}
	
	/*
	similar to above but on the right
	
	example 1:
	TSP =	[1 2 3 4 5 6 7 8]
	c1	=	5
	c2	=	2
	output:	[1 3 4 5 2 6 7 8]
	
	example 2:
	TSP =	[1 2 3 4 5 6 7 8]
	c1	=	3
	c2	=	7
	output:	[1 2 3 7 4 5 6 8]
	*/
	public void insertOnTheRight(int c1, int c2){
		//get the index where c1 and c2 currently are at
		int indc1 = this.getIndexOfCity(c1);
		int indc2 = this.getIndexOfCity(c2);
		
		//fix city and tour array
		//case where c1 comes after c2
		if(indc1 > indc2){
			
			//fix city array, just remove 1 from cities (c2, c1]
			for(int i = 1 ; i <= (indc1-indc2) ; i++){
				this.city[this.tour[indc2+i]-1]--;
			}
			//and also the c2
			this.city[c2-1]=indc1+1;
			
			//fix tour array, overwrite cities [c1, c2) to the ones on the right
			for(int i = 1 ; i <= (indc1-indc2) ; i++){
				this.tour[indc2+i-1]=this.tour[indc2+i];
			}
			//also fix the current city
			this.tour[indc1]=c2;
			
		}else{//case where c1 comes before c2
		
			//fix city array, just remove 1 to (c1, c2) 
			for(int i = 1 ; i < (indc2-indc1) ; i++){
				this.city[this.tour[indc1+i]-1]++;
			}
			//and also the current city (which will now be in the position that c1 was at the start)
			this.city[c2-1]=indc1+2;
			
			//fix tour array, overwrite cities (c2, c1) to the ones on the left
			for(int i = 1 ; i < (indc2-indc1) ; i++){
				this.tour[indc2-i+1]=this.tour[indc2-i];
			}
			//also fix the current city
			this.tour[indc1+1]=c2;
			
			
		}
		
		this.updateTime();
		
	}
	
	
}
