import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) {
        var text = "first line line\nsecond line\nsome text in third line";
        var futureResults = new ArrayList<Future<Map<String, Long>>>();
        var threadExecutions = Collections.synchronizedList(new ArrayList<String>());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Arrays.stream(text.split("\n")).forEach(line -> {
            Future<Map<String, Long>> future = executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                threadExecutions.add(threadName);
                return countWords(line);
            });
            futureResults.add(future);
        });

        Map<String, Long> result = mergeResults(futureResults);
        Map<String, Long> countOfThreadExecutions = countThreadExecutions(threadExecutions);

        System.out.println(countOfThreadExecutions);
        System.out.println(result);
        executor.shutdown();
    }

    private static Map<String, Long> countWords(String line) {
        return Arrays.stream(line.split(" "))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private static Map<String, Long> mergeResults(ArrayList<Future<Map<String, Long>>> futureResults) {
        return futureResults.stream()
                .map(Test::waitForResult)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.summingLong(Map.Entry::getValue)));
    }

    private static Map<String, Long> waitForResult(Future<Map<String, Long>> it) {
        try {
            return it.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Error when retrieving result");
            return Collections.emptyMap();
        }
    }

    private static Map<String, Long> countThreadExecutions(List<String> threadExecutions) {
        return threadExecutions.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }



}
