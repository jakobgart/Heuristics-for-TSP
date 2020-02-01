package Program.TSP;
/*
This class holds supports the sets X and Y used in Lin-Kernighan algorithm
*/
import java.util.*;
public class Set {
	
	//this class will hold a single element of the sets X and Y
	public static class setElement{
		
		//n1 and n2 represent the first and the second node connected by an edge
		public int n1;
		public int n2;
		
		//a pointer to the next set element (its a one way street)
		public setElement next;
		
		//create a new set element
		public setElement(int a, int b){
			this.n1=a;
			this.n2=b;
			this.next=null;
		}
		
		
	}
	
	//we'll have pointers to the first and last elements in the set X and Y
	public setElement firstX;
	public setElement lastX;
	public setElement firstY;
	public setElement lastY;
	
	//we'll hold the sizes of each sets
	public int sizeX;
	public int sizeY;
	
	//initialize a set
	public Set(){
		//initialize X and Y 
		this.firstX=null;
		this.lastX=null;
		this.sizeX=0;
		
		this.firstY=null;
		this.lastY=null;
		this.sizeY=0;
	}
	
	//function checks set X (in case isX=true) or Y (in case isX=false) holds the edge (c1,c2). Returns true if it is included, else false
	public boolean doesSetInclude(int c1, int c2, boolean isX){
		
		//our iterator
		setElement iter;
		
		//check which set we'll go through
		if(isX){
			iter=this.firstX;
		}else{
			iter=this.firstY;
		}
		
		//go through the set
		while(iter!=null){
			
			//check both posabilities
			if( (c1==iter.n1 && c2==iter.n2) || (c1==iter.n2 && c2==iter.n1) ){
				//set does include, so return true
				return true;
			}
			
			//go to the next set element
			iter=iter.next;
			
		}
	
		return false;
		
	}
	
	//function adds to set X (in case isX=true) or Y (in case isX=false) an element. Returns true if added, else false
	//Reminder: in order to add to either set the given edge must not be in either of the sets.
	public void addToSet(int c1, int c2, boolean isX){
		
		//check if set is empty, then we can just add and return 
		if(isX){
			if(this.sizeX==0){
				this.firstX=new setElement(c1, c2);
				this.lastX=this.firstX;
				this.sizeX++;
				return;
			}
		}else{
			if(this.sizeY==0){
				this.firstY=new setElement(c1, c2);
				this.lastY=this.firstY;
				this.sizeY++;
				return;
			}
		}
		//check if the edge is included in either set
		if(this.doesSetInclude(c1,c2,true) || this.doesSetInclude(c1,c2,false)){
			throw new java.lang.Error(String.format("Cannot add element (%d, %d) to the set\n%s",c1, c2, this.toString()));
		}
		
		//create the new set element
		setElement add = new setElement(c1, c2);
		
		//add it to the end of the set and update last
		if(isX){
			this.lastX.next=add;
			this.lastX=add;
			this.sizeX++;
		}else{
			this.lastY.next=add;
			this.lastY=add;
			this.sizeY++;
		}
		
		
	}
	
	//get a set element from either set X (isX=true) or set Y (isX=flase) at a given position, where 1 represents the first element, 2 the second, ...
	public setElement getElementAt(int pos, boolean isX){
		
		
		
		//set up an iterator
		setElement iter;
		
		//check which set
		if(isX){
			//we'll specifically check if the element is the last one to save time whenever thats the case
			if(pos==this.sizeX){
				return this.lastX;
			}
			//check if we want an element outside of element size
			if(pos>this.sizeX){
				throw new java.lang.Error(String.format("Trying to access %d element of X while we only have %d. Full set:\n%s\n", pos, this.sizeX, this.toString()));
			}
			//set up iterator
			iter=this.firstX;
			
		}else{//same as X but for Y
			
			if(pos==this.sizeY){
				return this.lastY;
			}
			if(pos>this.sizeY){
				throw new java.lang.Error(String.format("Trying to access %d element of X while we only have %d. Full set:\n%s\n", pos, this.sizeY, this.toString()));
			}
			iter=this.firstY;
		}
		
		//iterate
		while(pos>1){
			iter=iter.next;
			pos--;
		}
		
		//return the setElement
		return iter;
		
		
	}
	
	//delete the last few elements from either set X (isX=true) or set Y (isX=flase). Returns true if removed, else false
	public void removeLastFewElement(int howMany, boolean isX){
		
		//an iterator
		setElement iter;
		
		//the final size
		int newSize=0;
		
		//chcek whenever we can and setup iterator for the given set
		if(isX){
			//if its the same as the size we just clear it
			if(howMany==this.sizeX){
				this.firstX=null;
				this.lastX=null;
				this.sizeX=0;
				return;
			}
			//check if the size is bigger then the size of X. if so we return false
			if(howMany > this.sizeX){
				throw new java.lang.Error(String.format("Attempting to remove %d elements from set X while we have %d elements total \n%s", howMany, this.sizeX, this.toString()));
			}
			//setup iterator
			iter=this.firstX;
			//reduce size
			newSize=this.sizeX-howMany;
			
		}else{//same as above but for Y
			if(howMany==this.sizeY){
				this.firstY=null;
				this.lastY=null;
				this.sizeY=0;
				return;
			}
			if(howMany > this.sizeY){
				throw new java.lang.Error(String.format("Attempting to remove %d elements from set Y while we have %d elements total \n%s", howMany, this.sizeY, this.toString()));
			}
			iter=this.firstY;
			newSize=this.sizeY-howMany;
		}
		
		//go to the last element
		for(int i = 1; i < newSize; i++){
			iter=iter.next;
		}
		
		//set final element and clear
		if(isX){
			iter.next=null;
			this.lastX=iter;
			this.sizeX=newSize;
		}else{
			iter.next=null;
			this.lastY=iter;
			this.sizeY=newSize;
		}
		
	}
	
	//returns the size of the set X (isX=true) or set Y (isX=false)
	public int getSetSize(boolean isX){
		if(isX){
			return this.sizeX;
		}else{
			return this.sizeY;
		}
	}
	
	//for debugging
	public String toString(){
		String ret="X:\n";
		setElement iter = this.firstX;
		while(iter!=null){
			ret+=String.format("(%-2d,%-2d) ", iter.n1, iter.n2);
			iter=iter.next;
		}
		ret+="\nY:\n";
		iter = this.firstY;
		while(iter!=null){
			ret+=String.format("(%-2d,%-2d) ", iter.n1, iter.n2);
			iter=iter.next;
		}
		return ret;
	}
	
}