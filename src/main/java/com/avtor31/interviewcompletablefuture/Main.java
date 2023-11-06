package com.avtor31.interviewcompletablefuture;


import lombok.SneakyThrows;

import java.time.OffsetDateTime;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        System.out.println(getNow() + "Started");
        Main main = new Main();
        main.startCompletableFuture();
    }

    private void start() {
        Main.PreparationService preparationService = new Main.PreparationService();
        Main.ReportService reportService = new Main.ReportService();
        Main.SecureService secureService = new Main.SecureService();
        Main.ResultService resultService = new Main.ResultService();

        Boolean isPrepared = preparationService.prepare();
        String report = reportService.getReport();
        String pinCode = secureService.getPinCode();
        resultService.getResult(report, pinCode);

    }

    private void startStreamApi() {
        Main.PreparationService preparationService = new Main.PreparationService();
        Main.ReportService reportService = new Main.ReportService();
        Main.SecureService secureService = new Main.SecureService();
        Main.ResultService resultService = new Main.ResultService();

        Stream.of(preparationService.prepare())
                .map(isPrepared1 ->
                        resultService.getResult(reportService.getReport(), secureService.getPinCode()))
                .findFirst().get();
        System.out.println(getNow() + "Finished");
    }

    @SneakyThrows
    private void startFuture() {
        Main.PreparationService preparationService = new Main.PreparationService();
        Main.ReportService reportService = new Main.ReportService();
        Main.SecureService secureService = new Main.SecureService();
        Main.ResultService resultService = new Main.ResultService();

        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        Boolean isPrepared = executorService.submit(preparationService::prepare).get();
        Future<String> report = executorService.submit(reportService::getReport);
        Future<String> pinCode = executorService.submit(secureService::getPinCode);
        executorService.submit(() -> {
            try {
                executorService.submit(
                        () -> resultService
                                .getResult(report.get(), pinCode.get())).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SneakyThrows
    private void startCompletableFuture() {
        Main.PreparationService preparationService = new Main.PreparationService();
        Main.ReportService reportService = new Main.ReportService();
        Main.SecureService secureService = new Main.SecureService();
        Main.ResultService resultService = new Main.ResultService();

        CompletableFuture.supplyAsync(preparationService::prepare)
                .thenCompose(isPrepared -> CompletableFuture.supplyAsync(reportService::getReport)
                        .thenCombineAsync(CompletableFuture.supplyAsync(secureService::getPinCode),
                                resultService::getResult)
                )
                .exceptionally(Throwable::getMessage)
                .thenApply(result -> result).get();

    }

    public static String getNow() {
        return OffsetDateTime.now() + ": ";
    }

    private static class PreparationService {

        @SneakyThrows
        public Boolean prepare() {
            Thread.sleep(2000L);
            System.out.println(getNow() + "Preparation is ready");
            return true;
        }
    }

    private static class ReportService {

        @SneakyThrows
        public String getReport() {
            String result = "Report is ready";
            Thread.sleep(2000L);
            System.out.println(getNow() + result);
            return "some report";
        }
    }

    private static class SecureService {

        @SneakyThrows
        public String getPinCode() {
            String result = "PinCode is ready";
            Thread.sleep(2000L);
            System.out.println(getNow() + result);
            return "some pin code";
        }
    }

    private static class ResultService {

        @SneakyThrows
        public String getResult(String report, String pinCode) {
            String result = "result is ready with " + report + " and " + pinCode;
            Thread.sleep(2000L);
            System.out.println(getNow() + result);
            return result;
        }
    }
}
