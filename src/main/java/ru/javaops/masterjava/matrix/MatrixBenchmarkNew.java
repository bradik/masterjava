package ru.javaops.masterjava.matrix;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 1)
@Measurement(iterations = 6)
@BenchmarkMode({Mode.SingleShotTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Threads(1)
@Fork(1)
@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
public class MatrixBenchmarkNew {

    private static final int MATRIX_SIZE = 1000;

    @Param({"3","10"})
    private int threadNumber;

    final int[][] matrixA = MatrixUtil.create(MATRIX_SIZE);
    final int[][] matrixB = MatrixUtil.create(MATRIX_SIZE);

    private ExecutorService executor;

    @Setup
    public void setup() {
        executor = Executors.newFixedThreadPool(threadNumber);
    }

    @TearDown
    public void tearDown() {
        executor.shutdown();
    }

    @Benchmark
    public int[][] singleThreadMultiplyOpt(){
        return MatrixUtil.singleThreadMultiplyOpt(matrixA, matrixB);
    }

    @Benchmark
    public int[][] singleThreadMultiplyOpt1(){
        return MatrixUtil.singleThreadMultiplyOpt1(matrixA, matrixB);
    }

    @Benchmark
    public int[][] concurrentMultiplyStreams() throws Exception {
        return MatrixUtil.concurrentMultiplyStreams(matrixA, matrixB, threadNumber);
    }

    @Benchmark
    public int[][] concurrentMultiply() throws Exception {
        return MatrixUtil.concurrentMultiply(matrixA, matrixB, executor);
    }

    @Benchmark
    public int[][] concurrentMultiplyOpt1() throws Exception {

        return MatrixUtil.concurrentMultiplyOpt1(matrixA, matrixB, executor);
    }

    @Benchmark
    public int[][] concurrentMultiplyOpt2() throws Exception {

        return MatrixUtil.concurrentMultiplyOpt2(matrixA, matrixB, executor);
    }



}
