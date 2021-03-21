import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class TweetRank {
    private final Integer N = 465017;
    private final Double revN = 1 / new Double(N);
    private final Double epsilon = 1E-15;
    private final Double beta = 0.8;
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
        initR(r0, revN);
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

    private void initR(Double[] r, Double value){
        for(int i = 0; i < N; ++i) {
            r[i] = value;
            //remove items from deadends on the first pass only
            //sum up all the old rs of deadends, devide them by N and add to all r new
            //multiply all the elements of r new with (beta + (1-beta)/N)
        }
    }

    private void initDead(){
        for(int i = 0; i < N; ++i)
            deadEnds.add(i);
    }

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

    private void handleDeadEnd(Integer notEnd){
        deadEnds.remove(notEnd);
    }

    // r1[first] = beta * (r1[first] + SDeadEndsRevN) + offset + [(1 - leak) * revN]
    private void append(){
        Double SDeadEndsRevN = sumDeadEnds() * revN;
        Double leak = 0.0;

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

    private void isStat(){
        Double diff = 0.0;
        for(int i = 0; i < N; ++i)
            diff += Math.abs(r0[i] - r1[i]);
        if(diff < epsilon)
            stat = true;
    }

    private void swap(){
        tmp = r0;
        r0 = r1;
        r1 = tmp;
    }

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
        new TweetRank("out2");
    }

}
