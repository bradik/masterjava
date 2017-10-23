package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    private static class Result {

        private int i, j;
        private int sum;

        public Result(int i, int j, int sum) {
            this.i = i;
            this.j = j;
            this.sum = sum;
        }

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }

        public int getSum() {
            return sum;
        }
    }

    public static List<Result> iteration(final int finalI, final int matrixSize, final int[][] matrixA, final int[][] matrixB) {

        List<Result> results = new ArrayList<>();

        for (int j = 0; j < matrixSize; j++) {

            int sum = 0;
            for (int k = 0; k < matrixSize; k++) {
                //sum += matrixA[i][k] * matrixB[k][j];
                sum += matrixA[finalI][k] * matrixB[j][k];
            }

            results.add(new Result(finalI, j, sum));

        }

        return results;

    }

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {


        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final int BT[][] = new int[matrixSize][matrixSize];


        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                BT[j][i] = matrixB[i][j];
            }
        }


        final CompletionService<List<Result>> completionService = new ExecutorCompletionService<>(executor);

        List<Future<List<Result>>> futures = new ArrayList<>();

        for (int i = 0; i < matrixSize; i++) {
            int finalI = i;
            futures.add(completionService.submit(() -> iteration(finalI, matrixSize, matrixA, BT)));
        }

        while (!futures.isEmpty()) {
            Future<List<Result>> future = completionService.poll(10, TimeUnit.SECONDS);
            for (Result result : future.get()) {
                matrixC[result.getI()][result.getJ()] = result.getSum();
            }

            futures.remove(future);
        }

        return matrixC;
    }

    public static int[][] concurrentMultiplyOpt1(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {


        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final int BT[][] = new int[matrixSize][matrixSize];


        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                BT[j][i] = matrixB[i][j];
            }
        }


        final CompletionService<int[][]> completionService = new ExecutorCompletionService<>(executor);

        List<Future<int[][]>> futures = new ArrayList<>();

        for (int i = 0; i < matrixSize; i++) {
            int finalI = i;

            futures.add(completionService.submit(() -> {

                int results[][] = new int[matrixSize][3];

                for (int j = 0; j < matrixSize; j++) {

                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        //sum += matrixA[i][k] * matrixB[k][j];
                        sum += matrixA[finalI][k] * BT[j][k];
                    }

                    results[j][0] = finalI;
                    results[j][1] = j;
                    results[j][2] = sum;

                }

                return results;

            }));
        }

        while (!futures.isEmpty()) {
            Future<int[][]> future = completionService.poll(10, TimeUnit.SECONDS);
            for (int[] result : future.get()) {
                matrixC[result[0]][result[1]] = result[2];
            }

            futures.remove(future);
        }

        return matrixC;
    }


    public static int[][] concurrentMultiplyOpt(int numOpt, int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {

        final int[][] matrixC;

        switch (numOpt) {
            case 1:
                matrixC = concurrentMultiplyOpt1(matrixA, matrixB, executor);
                break;

            default:
                matrixC = concurrentMultiply(matrixA, matrixB, executor);
        }

        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {

        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixB[k][j];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] singleThreadMultiplyOpt1(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final int BT[][] = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                BT[j][i] = matrixB[i][j];
            }
        }

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * BT[j][k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] singleThreadMultiplyOpt2(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int thatColumn[] = new int[matrixSize];

        for (int j = 0; j < matrixSize; j++) {

            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][j];
            }

            for (int i = 0; i < matrixSize; i++) {
                int thisRow[] = matrixA[i];

                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += thisRow[k] * thatColumn[k];
                    ;
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] singleThreadMultiplyOpt3(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int thatColumn[] = new int[matrixSize];

        try {
            for (int j = 0; ; j++) {

                for (int k = 0; k < matrixSize; k++) {
                    thatColumn[k] = matrixB[k][j];
                }

                for (int i = 0; i < matrixSize; i++) {
                    int thisRow[] = matrixA[i];

                    matrixC[i][j] = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        matrixC[i][j] += thisRow[k] * thatColumn[k];
                    }
                }
            }
        } catch (IndexOutOfBoundsException ignored) {
        }
        ;

        return matrixC;
    }

    public static int[][] singleThreadMultiplyOpt4(int[][] matrixA, int[][] matrixB) {

        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final int A[] = new int[matrixSize * matrixSize];
        final int B[] = new int[matrixSize * matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                final int ij = i * matrixSize + j;
                A[ij] = matrixA[i][j];
                B[ij] = matrixB[i][j];
            }
        }

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {

                    final int ik = i * matrixSize + k;
                    final int kj = k * matrixSize + j;

                    sum += A[ik] * B[kj];
                }
                matrixC[i][j] = sum;
            }
        }

        return matrixC;
    }

    public static int[][] singleThreadMultiplyOpt5(int[][] matrixA, int[][] matrixB) {



        return null;
    }

    public static int[][] singleThreadMultiplyOpt(int numOpt, int[][] matrixA, int[][] matrixB) {

        final int[][] matrixC;

        switch (numOpt) {
            case 1:
                matrixC = singleThreadMultiplyOpt1(matrixA, matrixB);
                break;
            case 2:
                matrixC = singleThreadMultiplyOpt2(matrixA, matrixB);
                break;
            case 3:
                matrixC = singleThreadMultiplyOpt3(matrixA, matrixB);
                break;
            case 4:
                matrixC = singleThreadMultiplyOpt4(matrixA, matrixB);
                break;

            case 5:
                matrixC = singleThreadMultiplyOpt5(matrixA, matrixB);
                break;

            default:
                matrixC = singleThreadMultiply(matrixA, matrixB);
        }

        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
