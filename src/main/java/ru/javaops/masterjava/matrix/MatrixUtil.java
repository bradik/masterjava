package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
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

    public static List<Result> iterationMultiply(final int i, final int matrixSize, int[][] matrixA, int[][] matrixB) {

        List<Result> results = new ArrayList<>();

        for (int j = 0; j < matrixSize; j++) {

            int sum = 0;
            for (int k = 0; k < matrixSize; k++) {
                //sum += matrixA[i][k] * matrixB[k][j];
                sum += matrixA[i][k] * matrixB[j][k];
            }

            results.add(new Result(i, j, sum));

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
            futures.add(completionService.submit(() -> iterationMultiply(finalI, matrixSize, matrixA, BT)));
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
        final int thatColumn[] = new int[matrixSize];

        for (int j = 0; j < matrixSize; j++) {
            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][j];
            }

            for (int i = 0; i < matrixSize; i++) {
                int thisRow[] = matrixA[i];

                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += thisRow[k] * thatColumn[k];
                }
                matrixC[i][j] = sum;
            }
            ;
        }

        return matrixC;
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
