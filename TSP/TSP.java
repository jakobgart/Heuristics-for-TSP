package Program.TSP;
/*
This class reads a TSPLIB file and holds its information.
Will highly recommend reading the pdf about the TSPLIB format atleast once before reading the code (link in the next comment).

Note:
Not everything is supported, mainly things that are related to the STSP instances are!
*/

import java.lang.*;
import java.util.*;
import java.io.*;
import Program.TSP.Solution;

/*
Link to format of TSPLIB:
website:	http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/
pdf:		http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/tsp95.pdf

Basic rundown of a TSPLIB file and its elements relevance to my task:

what row/column upper/lower means:

a b c
b a d
c d a
lower row will show: b c d
upper row will show: b c d


The format and its relevance to my task:

NAME 				- name
TYPE 				- always TSP
COMMENT 			- irelevant
DIMENSION 			- nodeNum
CAPACITY 			- irelevant
EDGE_WEIGHT_TYPE 	- function by which we caluclate distances edgeType
EDGE_WEIGHT_FORMAT 	- if above is EXPLICIT then this exists (edgeFormat)
NODE_COORD_TYPE 	- coordType
DISPLAY_DATA_TYPE 	- dispType
EOF

*/

/*
The class which holds a TSP instance
*/
public class TSP {

	//an array of nodes for the TSP instance
	public node[] city;
	
	//name of instance
	public String name;
	
	//number of cities
	public int nodeNum;
	
	//weights/distances between cities
	//the matrix works like this, edgeWeight[A][B] gives us the distance going from city with index <A-1> to the city with index <B-1>.
	public double[][] edgeWeight;
	
	//if th distances between cities are to be calculated this variable holds the information of which formula to use to calculate them
	public String edgeType;
	
	//information about displaying cities (where -1 means cannot display, 0 means coords = display and 1 means explicit coordinates are given for display)
	public int display;
	
	/*public enum distFun {//REMOVE
		EXPLICIT, EUC_2D, EUC_3D, MAX_2D, MAX_3D, MIN_2D, MIN_3D, CEIL_2D, GEO, ATT, XRAY1, XRAY2, SPECIAL 
	};*/
	
	//WEIGHT_FORMAT will be managed in the reading part
	//same holds for EDGE_DATA_FORMAT and NODE_COORD_TYPE
	
	//if you can display a TSP this is how you display it
	/*public enum display { //REMOVE
		COORD_DISPLAY, TWOD_DISPLAY, NO_DISPLAY
	};*/
	
	
	
	/*
	This class holds both the actual coordinates and the display coordinates of a city
	*/
	public class node{
		
		
		//the coordinates of the city and the display coordinates of the city
		public double x;
		
		public double disx;
		
		public double y;
		
		public double disy;
		
		public double z;
		
		public double disz;
		
		//constructor if given 3d coordinates where <set> tells us how to save them
		public node(double a, double b, double c, int set){
			
			if(set==0){//only actual coordiantes	
				this.x=a;
				this.y=b;
				this.z=c;
				
			}else if(set==1){//only display coordinates
				this.disx=a;
				this.disy=b;
				this.disz=c;
				
			}else if(set==2){//both
				this.x=a;
				this.y=b;
				this.z=c;
				this.disx=a;
				this.disy=b;
				this.disz=c;
			}
		}
		
		//same as above but for 2d coordinates
		public node(double a, double b, int set){
			
			if(set==0){				
				this.x=a;
				this.y=b;
			}else if(set==1){
				this.disx=a;
				this.disy=b;
			}else if(set==2){
				this.x=a;
				this.y=b;
				this.disx=a;
				this.disy=b;
			}
		}
		
		
		
		//empty constructor if we'll ever need it
		public node(){};
		
		//display setter for 3 coords
		public void set3Display(double a, double b, double c){
			this.disx=a;
			this.disy=b;
			this.disz=c;
		}
		
		//display setter for 2 coords
		public void set2Display(double a, double b){
			this.disx=a;
			this.disy=b;
		}
		
		//coord setter for 3 coords
		public void set3Coord(double a, double b, double c){
			this.x=a;
			this.y=b;
			this.z=c;
		}
		
		//coord setter for 2 coords
		public void set2Coord(double a, double b){
			this.x=a;
			this.y=b;
		}
		
	}
	
	/*
	Removes spaces at the begining of the string and reduces multiple spaces to only one within the string
	*/
	private String reduceSpaces(String str){
		return str.trim().replaceAll(" +", " ");
	}
	
	/*
	https://www.iwr.uni-heidelberg.de/groups/comopt/software/TSPLIB95/tsp95.pdf
	is the link to the TSPLIB documentation that im following and making this reader by
	*/	
	//this constructor gets a FileReader and makes the according TSP
	public TSP(FileReader file){
		
		
		//some intial values
		this.nodeNum=-1;
		this.display=-1;
		
		
		/*
		format again for easier access:
		
		NAME 				- name
		TYPE 				- always TSP
		COMMENT 			- irelevant
		DIMENSION 			- nodeNum
		CAPACITY 			- irelevant
		EDGE_WEIGHT_TYPE 	- function by which we caluclate distances edgeType
		EDGE_WEIGHT_FORMAT 	- if above is EXPLICIT then this exists (edgeFormat)
		NODE_COORD_TYPE 	- coordType
		DISPLAY_DATA_TYPE 	- dispType
		EOF
		*/
		
		
		//we'll potentially need to know which format the edges are presented in if they're presented explicidly
		String edgeFormat=null;
		
		//we'll potentially need to know the if the graph is complete or not, and if not we'll need a way to figure out which edges are ok and which are not
		String complete=null;
		
		//we'll potentially need to know if the nodes have coordinates or not and how many (by default they do not have coordinates) (i suppose its 2?)
		int coordType=2;
		
		//we'll need a string where we save the line we just read
		String line;
		
		//we'll also need an array of strings for spliting
		String[] split;
		
		try{
			//we create a Scanner
			Scanner sc = new Scanner(file);
			
			//we read the first line
			line=reduceSpaces(sc.nextLine());
			
			//split
			split=line.split(" ");
			
			//loop that goes thourgh the file
			while(line!=null && !line.equals("EOF")){
				
				//we check what the first thing is, we process it and move onto the next one
				switch(split[0]){
					
					//-------------------------------------INFO SEGMENT------------------------------------
					
					//name of the TSP
				case "NAME:":
					if(this.name!=null){
						throw new java.lang.Error("Name already defined");
					}
					
					//get the name and save it
					this.name=split[1];
					
					//get next line and split it
					line=reduceSpaces(sc.nextLine());
					
					split=line.split(" ");
					break;
					
					//the type of TSP (TSP, ATSP, SOP). We should always have TSP
				case "TYPE:":
					if(!split[1].equals("TSP")){
						throw new java.lang.Error("Type is not \"TSP\"");
					}
					
					line=reduceSpaces(sc.nextLine());
					
					split=line.split(" ");
					break;
					
					//the comment
				case "COMMENT:":
					//we skip it
					line=reduceSpaces(sc.nextLine());
					
					split=line.split(" ");
					break;
					
					//the dimension of the TSP (number of cities)
				case "DIMENSION:":
					if(this.nodeNum!=-1){
						throw new java.lang.Error("Dimension (number of nodes/cities) already defined");
					}
					//we save the number of cities
					this.nodeNum=Integer.parseInt(split[1]);
					
					//initialize our cities and distances according to the given dimension
					this.city = new node[this.nodeNum+1];
					this.edgeWeight = new double[this.nodeNum][this.nodeNum];
					
					line=reduceSpaces(sc.nextLine());
					
					split=line.split(" ");
					break;
					
					//if the TSP instance has capacity limit for out salesman
				case "CAPACITY:":
					//error
					throw new java.lang.Error("Capacity defined though program cannot interpret CVRP");
					
					//how we get the distances between cities from this TSP instance
				case "EDGE_WEIGHT_TYPE:":
					if(this.edgeType!=null){
						throw new java.lang.Error("Edge wright type already defined");
					}
					//we save the string
					this.edgeType=split[1];	
					
					line=reduceSpaces(sc.nextLine());
					
					split=line.split(" ");						
					break;
					
					//if the edge_weight_type is "EXPLICIT" we need to know in what format the matrix of distances is
				case "EDGE_WEIGHT_FORMAT:":
					if(edgeFormat!=null){
						throw new java.lang.Error("Edge weight format already defined");
					}
					
					//we save the format in the local place
					edgeFormat=split[1];
					
					line=reduceSpaces(sc.nextLine());
					
					split=line.split(" ");
					break;
					
					//if the graph is not complete we need to know which edges are missing
				case "EDGE_DATA_FORMAT:":
					if(complete!=null){
						throw new java.lang.Error("Edge data format already defined");
					}
					
					complete=split[1];
					
					line=reduceSpaces(sc.nextLine());
					
					split=line.split(" ");
					break;
					
					//if the cities have coordinates this tells us how the coordinates will be given to us
				case "NODE_COORD_TYPE:":
					if(coordType!=-1){
						throw new java.lang.Error("Edge weight format already defined");
					}
					switch(split[1]){
					case "TWOD_COORDS":
						coordType=2;
						break;
						
					case "THREE_COORDS":
						coordType=3;
						break;
						
					case "NO_COORDS":
						coordType=0;
						break;
						
					default:
						throw new java.lang.Error("NODE_COORD_TYPE <string> has an unacceptable value " + split[1]);
						//break;
					}
					line=reduceSpaces(sc.nextLine());
					
					split=line.split(" ");
					break;
					
					//same as above but for display coordinates
				case "DISPLAY_DATA_TYPE:":
					if(this.display!=-1){
						throw new java.lang.Error("Display data type already defined");
					}
					switch(split[1]){
					case "TWOD_DISPLAY":
						this.display=1;
						break;
						
					case "COORD_DISPLAY":
						this.display=0;
						break;
						
					case "NO_DISPLAY":
						this.display=-1;
						break;
						
					default:
						throw new java.lang.Error("DISPLAY_DATA_TYPE <string> has an unacceptable value " + split[1]);
						//break;
					}
					line=reduceSpaces(sc.nextLine());
					
					split=line.split(" ");
					break;
					
					
					//-------------------------------------DATA SEGMENT------------------------------------
					
					
					//we read the coordinates of cities if those are given to us
				case "NODE_COORD_SECTION":
				
					//we check if display coords are the same as standard coord (meaning when we read them we can save them as both)
					int i=0;
					
					if(this.display==0){//if the city coordinates are the same as display coordinates we set i to 2 since thats the value we have to give to our constructor so it saves the coordiates to both display and actual ones
						i=2;
					}else{//else we set it to 0
						i=0;
					}
					
					//we only set both standard and display coordinates when the city is not yet initialized, else we only save the coordinates since the former implies that the display coordinates have already been given
					
					//we check which coord type we have
					if(coordType==2){
						
						//loop that reads the input and saves it to our list of cities (this assumes that every city will be given coordinates)
						for(int ii=0; ii<this.nodeNum ; ii++){
							
							//we get the city and save it
							int citIndex = Integer.parseInt(sc.next());
							
							//we check if a given city is initialized, if its not we initialize it else we just input the information
							if(this.city[citIndex-1]==null){
								this.city[citIndex-1] = new node(Double.valueOf(sc.next()), Double.valueOf(sc.next()), i);
							}else{
								this.city[citIndex-1].set2Coord(Double.valueOf(sc.next()), Double.valueOf(sc.next()));
							}
							
						}
						
					}else if(coordType==3){
						
						//loop that reads the input and saves it to our list of cities (this assumes that every city will be given coordinates)
						for(int ii=0; ii<this.nodeNum ; ii++){
							
							//we get the city and save it
							int citIndex = sc.nextInt();
							
							//we check if a given city is initialized, if its not we initialize it else we just input the information
							if(this.city[citIndex-1]==null){
								this.city[citIndex-1] = new node(Double.valueOf(sc.next()), Double.valueOf(sc.next()), Double.valueOf(sc.next()), i);
							}else{
								this.city[citIndex-1].set3Coord(Double.valueOf(sc.next()), Double.valueOf(sc.next()),  Double.valueOf(sc.next()));
							}
							
						}
						
					}else{
						throw new java.lang.Error("NODE_COORD_SECTION should not exist due to NODE_COORD_TYPE being NO_COORDS");
					}
					
					line=reduceSpaces(sc.nextLine());
					
					//we get to the next line
					while(line.equals("")){
						line=reduceSpaces(sc.nextLine());
					}					
					
					split=line.split(" ");
					
					break;
					
					//not supported
				case "DEPOT_SECTION":
					throw new java.lang.Error("DEPOT_SECTION error");
					
					
					//not supported
				case "DEMAND_SECTION":
					throw new java.lang.Error("CVRP cannot be read by this program");
					
					//not supported
				case "EDGE_DATA_SECTION":
					throw new java.lang.Error("non complete graphs not implemented");
					
					//not supported
				case "FIXED_EDGES_SECTION":
					throw new java.lang.Error("forced to have some edges in a solution implemented");
					
					//display coordinates
				case "DISPLAY_DATA_SECTION":
					//loop that goes thourgh all the display coordinates
					for(int ii=0; ii<nodeNum ; ii++){
						//we get the city and save it
						int citIndex = Integer.parseInt(sc.next());
						
						
						
						//this only happens when TWOD_DISPLAY is enabled meaning theres two posabilities:
						//1. we have no actual coordinates, only display coordinates
						//2. we have both
						
						//only display information part
						if(coordType==0){
							this.city[citIndex-1] = new node(Double.valueOf(sc.next()), Double.valueOf(sc.next()), 1); //THIS PART MIGHT BE REDUNDANT.
						}else{//both
							
							//since actual coordinates and display coordinates can come in whichever order we have to check if the city is already been initialized
							if(this.city[citIndex-1]==null){
								this.city[citIndex-1] = new node(Double.valueOf(sc.next()), Double.valueOf(sc.next()), 1);
							}else{
								this.city[citIndex-1].set2Display(Double.valueOf(sc.next()), Double.valueOf(sc.next()));
							}
						}
						
						
					}
					
					
					line=reduceSpaces(sc.nextLine());
					
					//we get to the next line
					while(line.equals("")){
						line=reduceSpaces(sc.nextLine());
					}
					
					split=line.split(" ");
					break;
					
					//not supported
				case "TOUR_SECTION":
					throw new java.lang.Error("TOUR SECTION not implented, probably the result but i'll manually check those");
					
					
					//gives us the edge weight
					//currently we dont manage matrixes which seperate values by new line
				case "EDGE_WEIGHT_SECTION":
				
					//we figure out which edgeFormat we use
					switch(edgeFormat){
						
					case "FULL_MATRIX"://full matrix, we just save the information
					
						for(int x = 0 ; x < nodeNum ; x++){


							for(int y = 0 ; y < nodeNum ; y++){

								if(sc.hasNext()){
									edgeWeight[x][y]=new Double(Double.valueOf(sc.next()));
								}else{
									throw new java.lang.Error("we dont have double at input while we should have");
								}

							}
						}

						line=reduceSpaces(sc.nextLine());
						
						//we get to the next line
						while(line.equals("")){
							line=reduceSpaces(sc.nextLine());
						}
						
						
						split=line.split(" ");
					break;

					case "UPPER_ROW"://upper row
						
						for(int x = 0 ; x < nodeNum-1 ; x++){

							for(int y = x+1 ; y < nodeNum ; y++){
								//read the value
								double curElm = Double.valueOf(sc.next());
								//save it on both from x to y and the other way around
								edgeWeight[x][y] = new Double(curElm);
								edgeWeight[y][x] = new Double(curElm);
							}
							//we set the distance from the same city to itself as 0
							edgeWeight[x][x] = new Double(0);
						}

						edgeWeight[nodeNum-1][nodeNum-1]=new Double(0);

						line=reduceSpaces(sc.nextLine());
						
						//we get to the next line
						while(line.equals("")){
							line=reduceSpaces(sc.nextLine());
						}
						
						split=line.split(" ");
						break;

					case "LOWER_ROW"://same as above but lower row
						for(int x = 1 ; x < nodeNum ; x++){

							for(int y = 0 ; y < x ; y++){
								double curElm = Double.valueOf(sc.next());
								edgeWeight[x][y] = new Double(curElm);
								edgeWeight[y][x] = new Double(curElm);
							}


							edgeWeight[x][x]=new Double(0);
						}
						edgeWeight[0][0]=new Double(0);

						line=reduceSpaces(sc.nextLine());
						

						//we get to the next line
						while(line.equals("")){
							line=reduceSpaces(sc.nextLine());
						}
						
						split=line.split(" ");
						break;

					case "UPPER_DIAG_ROW"://similar as above
						for(int x = 0 ; x < nodeNum ; x++){

							for(int y = x ; y < nodeNum ; y++){
								double curElm = Double.valueOf(sc.next());
								edgeWeight[x][y] = new Double(curElm);
								edgeWeight[y][x] = new Double(curElm);
							}

						}

						line=reduceSpaces(sc.nextLine());
						
						//we get to the next line
						while(line.equals("")){
							line=reduceSpaces(sc.nextLine());
						}
						
						split=line.split(" ");
						break;

					case "LOWER_DIAG_ROW":
						for(int x = 0 ; x < nodeNum ; x++){

							for(int y = 0 ; y < x+1 ; y++){
								double curElm = Double.valueOf(sc.next());
								edgeWeight[x][y] = new Double(curElm);
								edgeWeight[y][x] = new Double(curElm);
							}
						}

						line=reduceSpaces(sc.nextLine());
						
						//we get to the next line
						while(line.equals("")){
							line=reduceSpaces(sc.nextLine());
						}
						
						split=line.split(" ");
						break;

					case "FUNCTION":
						System.out.println("Function found in edge weight section, not sure how to manage");
						break;

					default:
						throw new java.lang.Error(edgeFormat + " not implemented");
						//break;


					}


					break;
					
				default:
					System.out.println(line + " cannot be properly understood, probably a continuation of something thats not implemented");
					line=reduceSpaces(sc.nextLine());
					
					split=line.split(" ");
					break;
					
					
				}
				
			}
			
		}
		catch(Exception e){
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
		
		//We've read the file
		
		//procesing other distances (CEIL_2D, EUC_2D, GEO)
		//for the math behind all these look at the pdf
		switch(edgeType){
			case "CEIL_2D":
				for(int x = 0 ; x < nodeNum ; x++){
					for(int y = x+1 ; y < nodeNum ; y++){
						Double result = Math.ceil(Math.sqrt(Math.pow(this.city[x].x - this.city[y].x,2) + Math.pow(this.city[x].y - this.city[y].y,2)));
						edgeWeight[x][y]=result;
						edgeWeight[y][x]=result;
					}
					edgeWeight[x][x]=0;
				}
			break;
			
			case "EUC_2D":
				for(int x = 0 ; x < nodeNum ; x++){
					for(int y = x+1 ; y < nodeNum ; y++){
						Double result = (double) Math.round(Math.sqrt(Math.pow(this.city[x].x - this.city[y].x,2) + Math.pow(this.city[x].y - this.city[y].y,2)));
						edgeWeight[x][y]=result;
						edgeWeight[y][x]=result;
					}
					edgeWeight[x][x]=0;
				}
			break;
			
			case "GEO":
			double PI = 3.141592;
			double RRR = 6378.388;
				for(int x = 0 ; x < nodeNum ; x++){
					for(int y = x+1 ; y < nodeNum ; y++){
						double latx, laty, longx, longy;
						
						double deg = Math.round(this.city[x].x);
						latx = PI * (deg + 5.00 * (this.city[x].x-deg) / 3.0) / 180.0;
						
						deg = Math.round(this.city[y].x);
						laty = PI * (deg + 5.00 * (this.city[y].x-deg) / 3.0) / 180.0;
						
						deg = Math.round(this.city[x].y);
						longx = PI * (deg + 5.00 * (this.city[x].y-deg) / 3.0) / 180.0;
						
						deg = Math.round(this.city[y].y);
						longy = PI * (deg + 5.00 * (this.city[y].y-deg) / 3.0) / 180.0;
						
						double q1 = Math.cos(longx - laty);
						double q2 = Math.cos(latx - laty);
						double q3 = Math.cos(latx - longy);
						
						//not sure if math round or floor
						Double result = (double) Math.round(RRR * Math.acos( 0.5 * ( (1.0 + q1) * q2 - (1.0 - q1) * q3 ) ) + 1.0);
						edgeWeight[x][y]=result;
						edgeWeight[y][x]=result;
					}
					edgeWeight[x][x]=0;
				}
			break;
			
		}
	}
	
	//gets distance between two cities
	public double getDistance(int from, int to){
		return edgeWeight[from-1][to-1];
	}
	
	//gets fitness value for the given soltuion
	public double getFitness(Solution s){
		if(s.howManyCities()!=this.nodeNum){
			return -1;
		}
		double res=0;
		for(int i = 0 ; i < s.howManyCities()-1 ; i++){
			res+=this.getDistance(s.getCity(i), s.getCity(i+1));
		}
		return res+this.getDistance(s.getCity(s.howManyCities()-1), s.getCity(0));
	}
	
	//returns the amount of cities in this TSP instance
	public int howManyCities(){
		return this.nodeNum;
	}
	
	
	
}
