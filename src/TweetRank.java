import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class TweetRank {
    private final Integer N = 465017;
    private final Double REVN = 1.0 / N;
    private final Double EPSILON = 1E-15;
    private final Double BETA = 0.8;
    private final Double OFFSET = (1 - BETA) * REVN;

    private boolean firstRead = true;
    private boolean stat = false;

    private HashSet<Integer> deadEnds = new HashSet<>();
    private Double[] r0 = new Double[N];
    private Double[] r1 = new Double[N];
    Double[] tmp;

    private final String filename;

    /***
     * Runs the algorithm that will parse a file and give user insights on the data
     * @param filename
     */
    public TweetRank(String filename){
        this.filename = filename;
        initR(r0, REVN);
        initDead();
        while(!stat) {
            initR(r1, 0.0);
            //Open file and parse it
            compute();
            append();
            isStat();
            swap();
            firstRead = false;
        }

        result();
    }

    /***
     * Initialises arrays with a specific values
     * @param r
     * @param value
     */
    private void initR(Double[] r, Double value){
        for(int i = 0; i < N; ++i)
            r[i] = value;
    }

    /***
     * Initialises the set of dead ends by considering that all the nodes in the network
     * are dead ends
     */
    private void initDead(){
        for(int i = 0; i < N; ++i)
            deadEnds.add(i);
    }

    /***
     * Parses the file, initialises the array r1 r1[first] += value * r0[second]
     * handles the deletion of none dead end nodes from the set
     */
    private void compute(){
        int first, second;
        Double value;
        try{
            BufferedReader buffer = new BufferedReader(new FileReader(filename));
            String strLine;
            while ((strLine = buffer.readLine()) != null) {
                String[] line = strLine.split("\\s+");
                if(line.length != 3)
                    throw new IOException("File format issue");

                first = Integer.parseInt(line[1])  - 1; second = Integer.parseInt(line[0])  - 1;
                value = Double.parseDouble(line[2]);

                r1[first] += value * r0[second];

                if(firstRead)
                    handleDeadEnd(first);
            }
            buffer.close();
        }catch(IOException e){
            System.out.println("Error while parsing file " + e.getMessage());
        }
    }

    /***
     * Removes nodes that shouldn't be considered as dead ends from the set
     * @param notEnd
     */
    private void handleDeadEnd(Integer notEnd){
        deadEnds.remove(notEnd);
    }


    /***
     * Adds values to the rank of each array to make sure it supports random transportation
     * and handles dead ends
     * r1[row] = beta * (r1[row] + SDeadEndsRevN) + offset + [(1 - leak) * revN]
     */
    private void append(){
        Double SDeadEndsRevN = sumDeadEnds() * REVN;
        Double leak = 0.0;

        //Leaking has been calculated here to enhance performance given that
        //r1[i] is already in the cash
        for(int i = 0; i < N; ++i){
            r1[i] = BETA * (r1[i] + SDeadEndsRevN) + OFFSET;
            leak += r1[i];
        }

        for(int i = 0; i < N; ++i){
            r1[i] += (1 - leak) * REVN;
        }
    }

    /***
     * @return sum of the rank of dead ends
     */
    private Double sumDeadEnds(){
        return deadEnds.stream().map(s -> r0[s]).mapToDouble(Double::doubleValue).sum();
    }

    /***
     * Determines if the array's values have converged or not
     */
    private void isStat(){
        Double diff = 0.0;
        for(int i = 0; i < N; ++i)
            diff += Math.abs(r0[i] - r1[i]);
        if(diff < EPSILON)
            stat = true;
    }

    /***
     * swaps the arrays r0 and r1
     */
    private void swap(){
        tmp = r0;
        r0 = r1;
        r1 = tmp;
    }

    /***
     * Draws insights whilst the algorithm's execution
     */
    private void result(){
        Double sum = 0.;
        Double min = r1[0];
        int imin = 0;
        Double max = r1[0];
        int imax = 0;

        for(int i = 0; i < N; ++i){
            sum += r1[i];
            if(r1[i] < min){
                min = r1[i];
                imin = i;
            }
            if(r1[i] > max){
                max = r1[i];
                imax = i;
            }
        }

        System.out.println(sum);
        System.out.println("Most influencer user : " + imax + 1 + " with a stat proba of " + max);
        System.out.println("Least influencer user : " + imin + 1 + " with a stat proba of " + min);
    }

    public static void main(String[] args){
        new TweetRank("in");
    }

}
