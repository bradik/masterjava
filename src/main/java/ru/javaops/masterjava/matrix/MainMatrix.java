package ru.javaops.masterjava.matrix;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * gkislin
 * 03.07.2016
 */
public class MainMatrix {
    private static final int MATRIX_SIZE = 1000;
    private static final int THREAD_NUMBER = 10;

    private final static ExecutorService executor = Executors.newFixedThreadPool(MainMatrix.THREAD_NUMBER);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        task1Test();

        //mainTest();

    }

    private static void task1Test() throws ExecutionException, InterruptedException {

        final int NUM_OPT1 = 1;
        final int NUM_OPT2 = 4;

        final int[][] matrixA = MatrixUtil.create(MATRIX_SIZE);
        final int[][] matrixB = MatrixUtil.create(MATRIX_SIZE);

        double origThreadSum = 0.;
        double optimThreadSum = 0.;

        int count = 1;
        while (count < 6) {
            System.out.println("Pass " + count);
            long start = System.currentTimeMillis();
            final int[][] matrixC = MatrixUtil.singleThreadMultiplyOpt(NUM_OPT1, matrixA, matrixB);
            double duration = (System.currentTimeMillis() - start) / 1000.;
            out("Orig impl time, sec: %.3f", duration);
            origThreadSum += duration;

            start = System.currentTimeMillis();
            final int[][] concurrentMatrixC = MatrixUtil.singleThreadMultiplyOpt(NUM_OPT2, matrixA, matrixB);
            duration = (System.currentTimeMillis() - start) / 1000.;
            out("New impl time, sec: %.3f", duration);
            optimThreadSum += duration;

            if (!MatrixUtil.compare(matrixC, concurrentMatrixC)) {
                System.err.println("Comparison failed");
                break;
            }
            count++;
        }
        executor.shutdown();
        out("\nAverage Orig impl time, sec: %.3f", origThreadSum / 5.);
        out("Average New impl time, sec: %.3f", optimThreadSum / 5.);


    }


    private static void mainTest() throws ExecutionException, InterruptedException {

        final int[][] matrixA = MatrixUtil.create(MATRIX_SIZE);
        final int[][] matrixB = MatrixUtil.create(MATRIX_SIZE);

        double singleThreadSum = 0.;
        double concurrentThreadSum = 0.;
        int count = 1;
        while (count < 6) {
            System.out.println("Pass " + count);
            long start = System.currentTimeMillis();
            //final int[][] matrixC = MatrixUtil.singleThreadMultiply(matrixA, matrixB);
            final int[][] matrixC = MatrixUtil.singleThreadMultiplyOpt(1, matrixA, matrixB);
            double duration = (System.currentTimeMillis() - start) / 1000.;
            out("Single thread time, sec: %.3f", duration);
            singleThreadSum += duration;

            start = System.currentTimeMillis();
            final int[][] concurrentMatrixC = MatrixUtil.concurrentMultiplyOpt(1, matrixA, matrixB, executor);
            duration = (System.currentTimeMillis() - start) / 1000.;
            out("Concurrent thread time, sec: %.3f", duration);
            concurrentThreadSum += duration;

            if (!MatrixUtil.compare(matrixC, concurrentMatrixC)) {
                System.err.println("Comparison failed");
                break;
            }
            count++;
        }
        executor.shutdown();
        out("\nAverage single thread time, sec: %.3f", singleThreadSum / 5.);
        out("Average concurrent thread time, sec: %.3f", concurrentThreadSum / 5.);

    }

    private static void out(String format, double ms) {
        System.out.println(String.format(format, ms));
    }
}
