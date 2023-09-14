import java.util.ArrayList;
import java.io.*;
public class Mallocator {
    public static void main(String[] args) {


        /*
         * FORMAT FOR INPUT.DATA:
         * 
         * # of Memory slots
         * Memslot1StartAddress Memslot1EndAddress
         * ...
         * ...
         * # of Processes
         * Process1ID Process1Size
         * ...
         * ...
         */


        //stores memory input info
        ArrayList<String> minput = readFile("input.data");

        //converts data into memorySlot objects
        int memSlots = Integer.parseInt(minput.get(0));
        ArrayList<MemorySlot> memory = new ArrayList<MemorySlot>();

        for (int i = 1; i < memSlots+1; i++) {
            String[] adds = minput.get(i).split(" ");
            MemorySlot mem = new MemorySlot(Integer.parseInt(adds[0]), Integer.parseInt(adds[1]));
            memory.add(mem);
        }

        //converts data into process objects
        int processes = Integer.parseInt(minput.get(memSlots+1));
        ArrayList<Process> process = new ArrayList<Process>();

        for (int i = memSlots+2; i < memSlots + processes + 2; i++) {
            String[] y = minput.get(i).split(" ");
            Process pro = new Process(Integer.parseInt(y[0]), Integer.parseInt(y[1]));
            process.add(pro);
        }

        for (int i = 0; i < process.size(); i++) {
            System.out.println(process.get(i));
        }

        //Call each method
        //since they all use the same mem and process objects, need to reset allocated boolean and unsort each time
        firstFit(memory, process);
        reset(memory, process);
        bestFit(memory, process);
        reset(memory, process);
        worstFit(memory, process);
    }

    //method to read input files
    public static ArrayList<String> readFile(String filePath) {

        ArrayList<String> data = new ArrayList<String>();
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            while (line != null) {
                data.add(line);
                line = reader.readLine();
            }
        reader.close();
        }
        catch (IOException e) {
            System.out.println("An error occurred reading the file");
        }

        return data;
    }

    //method for first fit
    public static void firstFit(ArrayList<MemorySlot> memSlots, ArrayList<Process> processes){

        //iterate through processes
        for (int i = 0; i < processes.size(); i++) {

            //iterate throught memSlots
            for(int x = 0; x < memSlots.size(); x++){

                if((processes.get(i).getSize() <= memSlots.get(x).getTotalMem()) && 
                (processes.get(i).getAllocated() == false)) {

                    //set process start add
                    processes.get(i).setStartAdd(memSlots.get(x).getStartAdd());

                    //set allocated boolean to true
                    processes.get(i).allocate(true);

                    //set new memory start add
                    memSlots.get(x).setStartAdd(processes.get(i).getEndAdd());
                }
                
            }
        }

        //Call method to sort by memory address for final output
        sortByMem(processes);

        //Call method to write results
        writeResults("FFoutput.data", processes);

    }

    //method for best fit
    public static void bestFit(ArrayList<MemorySlot> memSlots, ArrayList<Process> processes){

        //Iterate through processes
        for (int i = 0; i < processes.size(); i++){
            int best = -1;
            int bestFitMem = -1;
            

            //Iterate through memory slots
            for (int x = 0; x < memSlots.size(); x++){
                int fitMem = memSlots.get(x).getTotalMem() - processes.get(i).getSize();

                //If memory slot is large enough to fit process
                if (fitMem >= 0 &&
                    (processes.get(i).getAllocated() == false)){

                    //if this is the first eligible mem slot
                    if (best==-1){
                        best = x;
                        bestFitMem = fitMem;
                    }

                    //else if this mem slot is a better fit than the current best
                    else if(fitMem < bestFitMem){
                        best = x;
                        bestFitMem = fitMem;
                    }

                }
            }

            //If there is an elligble match after searching mem slots
            if(best != -1){

                //set new process start add
                processes.get(i).setStartAdd(memSlots.get(best).getStartAdd());

                //set allocated boolean to true
                processes.get(i).allocate(true);

                //set new memory start add
                memSlots.get(best).setStartAdd(processes.get(i).getEndAdd());
            }
        }

        //Call method to sort by memory address for final output
        sortByMem(processes);

        //Call method to write results
        writeResults("BFoutput.data", processes);
    }

    //method for worst fit
    public static void worstFit(ArrayList<MemorySlot> memSlots, ArrayList<Process> processes){
        //Iterate through processes
        for (int i = 0; i < processes.size(); i++){
            int worst = -1;
            
            //Iterate through memory slots
            for (int x = 0; x < memSlots.size(); x++){

                //If memory slot is large enough to fit process
                if (memSlots.get(x).getTotalMem() - processes.get(i).getSize() >= 0 &&
                    (processes.get(i).getAllocated() == false)){

                    //if this is the first eligible mem slot
                    if (worst==-1){
                        worst = x;
                    }

                    //else if this mem slot is a larger than the current worst fit
                    else if(memSlots.get(worst).getTotalMem() < memSlots.get(x).getTotalMem()){
                        worst = x;
                    }

                }
            }

            //If there is an elligble match after searching mem slots
            if(worst != -1){

                //set new process start add
                processes.get(i).setStartAdd(memSlots.get(worst).getStartAdd());

                //set allocated boolean to true
                processes.get(i).allocate(true);

                //set new memory start add
                memSlots.get(worst).setStartAdd(processes.get(i).getEndAdd());
            }
        }

        //Call method to sort by memory address for final output
        sortByMem(processes);

        //Call method to write results
        writeResults("WFoutput.data", processes);
    }

    //method to sort results by memory
    public static void sortByMem(ArrayList<Process> processes) {
        
        int n = processes.size();

        for (int i = 0; i < n-1; i++) {
            int min = i;

            for (int j = i+1; j < n; j++) {

                if(processes.get(j).getStartAdd() < processes.get(min).getStartAdd()) {
                    min = j;
                }
            }

            Process temp = processes.get(min);
            processes.set(min, processes.get(i));
            processes.set(i, temp);
        }
    }

    //method to write results to file
    public static void writeResults(String name, ArrayList<Process> results){

        ArrayList<Integer> notAllocated = new ArrayList<Integer>();

        //create file
            File file = new File(name);
        
        //write to file
        try {
            FileWriter writer = new FileWriter(file);

            for (int i=0; i<results.size(); i++){

                if(results.get(i).getAllocated() == true){
                    writer.write(results.get(i) + "\n");
                }

                else {
                    notAllocated.add(results.get(i).getID());
                }
            }

            //Print non-allocated processes

            if(notAllocated.size() == 0){
                writer.write("-0");
            }

            if(notAllocated.size() == 1){
                writer.write("-" + notAllocated.get(0).toString());
            }

            if(notAllocated.size() > 1){
                writer.write("-" + notAllocated.get(0).toString());

                for(int i = 1; i < notAllocated.size(); i++){
                    writer.write(", " + notAllocated.get(i).toString());
                }
            }

            writer.close();
        }
        catch (IOException e) {
            System.out.println("An error occurred writing to file");
        }

    }

    //method to reset memory slots and processes
    public static void reset(ArrayList<MemorySlot> memory, ArrayList<Process> processes){

        //reset allocated booleans of both processes and mem slots
        for(int i = 0; i < processes.size(); i++){
            processes.get(i).allocate(false);
        }

        for(int i = 0; i < memory.size(); i++){
            memory.get(i).reset();
        }

        //resort by Pid instead of mem address
        for(int i = 0; i < processes.size(); i++) {
            int x = processes.get(i).getID();

            if(x != i+1){
                Process temp = processes.get(x-1);
                processes.set(x-1, processes.get(i));
                processes.set(i, temp);
            }
        }

    }

}

class MemorySlot {
    int originalStartAdd;
    int startadd;
    int endadd;
    int totalmem;

    public MemorySlot(int start, int end) {
        this.originalStartAdd = start;
        this.endadd = end;
        startadd = originalStartAdd;
        totalmem = (end - start);
    }

    public int getStartAdd() {
        return startadd;
    }

    public int getTotalMem() {
        return totalmem;
    }

    public void reset(){
        this.startadd = originalStartAdd;
        this.totalmem = (endadd - startadd);
    }

    public void setStartAdd(int add){
        this.startadd = add;
        this.totalmem = (endadd - startadd);
    }
}

class Process {
    int id;
    int startadd;
    int endadd;
    int size;
    boolean allocated;

    public Process(int id, int size) {
        this.size = size;
        this.id = id;
        this.allocated = false;
    }

    public void setStartAdd(int start) {
        this.startadd = start;
        this.endadd = start + this.size;
    }

    public void allocate(boolean al) {
        this.allocated = al;
    }

    public int getID(){
        return id;
    }

    public int getSize(){
        return size;
    }

    public boolean getAllocated(){
        return allocated;
    }

    public int getStartAdd(){
        return startadd;
    }

    public int getEndAdd(){
        return endadd;
    }

    public String toString(){
        Integer x = Integer.valueOf(id);
        Integer y = Integer.valueOf(startadd);
        Integer z = Integer.valueOf(endadd);
    
        return(y.toString() + " " + z.toString() + " " + x.toString());
    }
    
}