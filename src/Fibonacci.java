import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Fibonacci {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    /* define constants */
    //static long MAXVALUE = 200000000;
    //static long MINVALUE = -200000000;
    static int numberOfTrials = 100;
    static int MAXINPUTSIZE = 40;
    static int MININPUTSIZE = 0;
    static int STEPNUMBER = 1;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

    static String ResultsFolderPath = "/home/sethowens/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;


    public static void main(String[] args) {
        /*checkFib(Fibonacci::FibLoop);
        checkFib(Fibonacci::FibRecur);
        checkFib(Fibonacci::FibRecurDP);
        checkFib(Fibonacci::FibMatrix);
        //run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("FibLoop-Exp1-ThrowAway.txt", Fibonacci::FibLoop);
        System.out.println("Running second full experiment...");
        runFullExperiment("FibLoop-Exp2.txt", Fibonacci::FibLoop);
        System.out.println("Running third full experiment...");
        runFullExperiment("FibLoop-Exp3.txt", Fibonacci::FibLoop);*/

        System.out.println("Running first full experiment...");
        runFullExperiment("FibRecur-Exp1-ThrowAway.txt", Fibonacci::FibRecur);
        System.out.println("Running second full experiment...");
        runFullExperiment("FibRecur-Exp2.txt", Fibonacci::FibRecur);
        System.out.println("Running third full experiment...");
        runFullExperiment("FibRecur-Exp3.txt", Fibonacci::FibRecur);

        /*System.out.println("Running first full experiment...");
        runFullExperiment("FibRecurDP-Exp1-ThrowAway.txt", Fibonacci::FibRecurDP);
        System.out.println("Running second full experiment...");
        runFullExperiment("FibRecurDP-Exp2.txt", Fibonacci::FibRecurDP);
        System.out.println("Running third full experiment...");
        runFullExperiment("FibRecurDP-Exp3.txt", Fibonacci::FibRecurDP);

        System.out.println("Running first full experiment...");
        runFullExperiment("FibMatrix-Exp1-ThrowAway.txt", Fibonacci::FibMatrix);
        System.out.println("Running second full experiment...");
        runFullExperiment("FibMatrix-Exp2.txt", Fibonacci::FibMatrix);
        System.out.println("Running third full experiment...");
        runFullExperiment("FibMatrix-Exp3.txt", Fibonacci::FibMatrix);*/
    }

    static void runFullExperiment(String resultsFileName, Function<java.lang.Long, java.lang.Long> func) {

        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for (long inputSize = MININPUTSIZE; inputSize <= MAXINPUTSIZE; inputSize += STEPNUMBER) {
            // progress message...
            System.out.println("Running test for input size " + inputSize + " ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");
            //long[] testList = createRandomIntegerList(inputSize);
            //testList = quickSort(testList);
            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the tirals
            for (long trial = 0; trial < numberOfTrials; trial++) {

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                func.apply(inputSize);
                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n", inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    static void checkFib(Function<java.lang.Long, java.lang.Long> func){
        //Correct list to test against
        long[] correctList = {1, 1, 2, 3, 5, 8, 13, 21, 34, 55};
        long[] testList = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for(long iii = 0; iii < 10; ++iii){
            testList[(int)iii] = func.apply(iii);
            //Check against the correct list
            if(testList[(int)iii] != correctList[(int)iii]){
                System.out.println("Testing returned incorrect value.");
                return;
            }
            //Print it out for further verification
            System.out.printf("%d ", testList[(int)iii]);
        }
        System.out.println("\nTesting completed correctly.");
    }

    static long FibLoop(long x){
        //Start with 1
        if(x <= 1){
            return 1;
        }
        //Add up two previous values each iteration
        long secondToLast = 1;
        long last = 1;
        long current = 0;
        for(int iii = 2; iii <= (int)x; ++iii){
            current = secondToLast + last;
            last = secondToLast;
            //Only save the current value and two previous values
            secondToLast = current;
        }
        return current;
    }

    //Splits the value recursively
    static long FibRecur(long x){
        if(x <= 1){
            return 1;
        }
        return (FibRecur(x - 1) + FibRecur(x - 2));
    }

    static long FibRecurDP(long x){
        //Cache saved as array and passed as value
        long[] FibResultsCache = new long[(int)x];
        for(int iii = 0; iii < (int)x; ++iii){
            //Initialize cache with -1
            FibResultsCache[iii] = -1;
        }
        long result = FibRecursiveWithCache(x, FibResultsCache);
        //Release Cache. Could call garbage collection, but it would take additional time
        //Will automatically be called on FibResultsCache next time it runs.
        FibResultsCache = null;
        return result;
    }

    static long FibRecursiveWithCache(long x, long[] cache){
        if(x <= 1){
            return 1;
        }
        //If it doesn't exist, set it
        else if (cache[(int) x - 1] == -1){
            cache[(int) x - 1] = (FibRecursiveWithCache(x - 1, cache) + FibRecursiveWithCache(x - 2, cache));
        }
        //Value should be stored here
        return cache[(int)x - 1];
    }

    static long[] matrixFourMultiplication(long[] matrixA, long[] matrixB){
        //The following calculations are found on page 63 of the Algorithms paper
        long P1 = matrixA[0] * (matrixB[1] - matrixB[3]);
        long P2 = (matrixA[0] + matrixA[1]) * matrixB[3];
        long P3 = (matrixA[2] + matrixA[3]) * matrixB[0];
        long P4 = matrixA[3] * (matrixB[2] - matrixB[0]);
        long P5 = (matrixA[0] + matrixA[3]) * (matrixB[0] + matrixB[3]);
        long P6 = (matrixA[1] - matrixA[3]) * (matrixB[2] + matrixB[3]);
        long P7 = (matrixA[0] - matrixA[2]) * (matrixB[0] + matrixB[1]);

        long[] result = new long[4];
        result[0] = (P5 + P4 - P2 + P6);
        result[1] = (P1 + P2);
        result[2] = (P3 + P4);
        result[3] = (P1 + P5 - P3 - P7);
        return result;
    }

    static long FibMatrix(long x){
        if(x == 0){
            return 1;
        }
        --x;
        //Use binary instead of division
        String bin = Long.toBinaryString(x);
        //This is the matrix at 0
        long[] matrix = {1, 1, 1, 0};
        long[] currentMatrix = {1, 1, 1, 0};
        //Fib(0) == 1
        long result = 0;
        for(int iii = 0; iii < bin.length(); ++iii){
            //If the binary is not zero, it should be added to the total
            if (bin.charAt(bin.length() - (iii + 1)) == '1') {
                matrix = matrixFourMultiplication(currentMatrix, matrix);
            }
            //Square the matrix
            currentMatrix = matrixFourMultiplication(currentMatrix, currentMatrix);
        }
        result = matrix[0];
        return result;
    }
}