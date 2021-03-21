import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Stream;

public class TweetRank {
    private final Integer N = 465017;
    private final Double revN = 1 / new Double(N);
    private Double epsilon = 1E-15;
    private Double beta = 0.8;
    private final Double offset = (1 - beta) * revN;

    private boolean firstRead = true;
    private boolean stat = false;

    private HashSet<Integer> deadEnds = new HashSet<>();
    private Double[] r0 = new Double[N];
    private Double[] r1 = new Double[N];
    Double[] tmp;

    private final String filename;

    public TweetRank(String filename){
        this.filename = filename;
        if(firstRead)
            init(r0, revN);
        while(!stat) {
            init(r1, 0.0);
            //Open file and parse it
            compute();
            firstRead = false;
            append();
//            leak();
            isStat();
            tmp = r0;
            r0 = r1;
            r1 = tmp;
        }
        Double sum = 0.;
        for(int i = 0; i < N; ++i){
            sum += r1[i];
        }
        System.out.println(sum);
        System.out.println("Least influencer user : " + Arrays.stream(r1).min(Double::compare).toString());
        System.out.println("Most influencer user : " + Arrays.stream(r1).max(Double::compare).toString());

    }

    private void init(Double[] r, Double value){
        boolean initDead = false;
        if (deadEnds.size() == 0)
            initDead = true;
        for(int i = 0; i < N; ++i) {
            r[i] = value;
            //remove items from deadends on the first pass only
            //sum up all the old rs of deadends, devide them by N and add to all r new
            //multiply all the elements of r new with (beta + (1-beta)/N)
            if(initDead && firstRead)
                deadEnds.add(i);
        }
    }

    private void compute(){
        Integer first, second;
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

    private void handleDeadEnd(Integer notEnd){
        if(deadEnds.contains(notEnd))
            deadEnds.remove(notEnd);
    }


    private void append(){
        Double SDeadEndsRevN = sumDeadEnds() * revN;
        Double leak = 0.0;
        // r1[first] = beta * (r1[first] + SDeadEndsRevN) + offset
        for(int i = 0; i < N; ++i){
            r1[i] = beta * (r1[i] + SDeadEndsRevN) + offset;
            leak += r1[i];
        }
        for(int i = 0; i < N; ++i){
            r1[i] += (1 - leak) * revN;
        }
    }

    private Double sumDeadEnds(){
        return deadEnds.stream().map(s -> r0[s]).mapToDouble(Double::doubleValue).sum();
    }

//    private void leak(){
//        Double sum = 0.0;
//        for(int i = 0; i < N; ++i){
//            sum += r1[i];
//        }
//        for(int i = 0; i < N; ++i){
//            r1[i] += (1 - sum) * revN;
//        }
//    }

    private void isStat(){
        Double diff = 0.0;
        for(int i = 0; i < N; ++i)
            diff += Math.abs(r0[i] - r1[i]);
        if(diff < epsilon)
            stat = true;
    }

    public static void main(String[] args){
        TweetRank analyze = new TweetRank("/Users/rima/Desktop/LinkAnalysis/src/out2");
    }

}
