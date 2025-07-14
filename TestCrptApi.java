package ru.crpt;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestCrptApi {

    public static void main(String[] args) throws InterruptedException {
        try (CrptApi api = new CrptApi(TimeUnit.SECONDS, 5)) {
            try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
                test(executorService, api);
                test(executorService, api);
                test(executorService, api);
                test(executorService, api);
                test(executorService, api);
            }
        }
    }

    private static void test(ExecutorService executorService, CrptApi api) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(100);
            executorService.submit(() -> {
                String name = Thread.currentThread().getName();
                try {
                    System.out.println(LocalDateTime.now() + " | " + name + " Отправка запроса");
                    String response = api.makeRequest(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        return "response";
                    });
                    System.out.println(LocalDateTime.now() + " | " + name + " Ответ API: " + response);
                } catch (Exception e) {
                    System.err.println(LocalDateTime.now() + " | " + name + " | " + e.getMessage());
                }
            });
        }
    }
}
