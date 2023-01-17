import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordCounter {
  private ExecutorService executor;
  private Map<String, Integer> results;
  private Map<String, Integer> threads;

  public WordCounter(int threadsNo) {
    executor = Executors.newFixedThreadPool(threadsNo);
    results = Collections.synchronizedMap(new HashMap<>());
    threads = Collections.synchronizedMap(new HashMap<>());
  }

  private Map<String, Integer> count(Stream<String> line){
    Map<String, Integer> occurrences = new HashMap<>();
    line.forEach(word -> {
      if(!occurrences.containsKey(word)) {
            occurrences.put(word, 1);
            return;
      }
      occurrences.put(word, occurrences.get(word) + 1);
    });
    return occurrences;
  }

  public void countWords(String data) {
    executor.execute(() -> {
      var line = Arrays
        .stream(data.toLowerCase()
          .split("[\s\n]"))
          .map(word -> word
          .chars()
          .mapToObj(i -> (char) i)
          .filter(Character::isLetter).collect(Collector.of(
            StringBuilder::new,
            StringBuilder::append,
            StringBuilder::append,
            StringBuilder::toString)))
          .filter(word -> word.length() > 1);
      count(line).forEach((key, value) -> {
        if (!results.containsKey(key)) {
          results.put(key, value);
          return;
        }
        results.put(key, results.get(key) + value);
      });
      var currentThread = Thread.currentThread().getName();
      if(!threads.containsKey(currentThread)){
        threads.put(currentThread, 1);
        return;
      }
      threads.put(currentThread, threads.get(currentThread) + 1);
    });
  }

  public void shutdownExecutor() {
    try {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }
  }
  public void printResults() {
    this.results.forEach((key, value) -> System.out.println(key + " " + value));
  }
  public void printThreads() {
    this.threads.forEach((key, value) -> System.out.println(key + ": " + value));
  }

  public static void main(String[] args) throws InterruptedException {
    if (args == null || args.length != 2) {
      System.out.println("Incorrect argument list");
      return;
    }
    int threads;
    try {
      threads = Integer.parseInt(args[0]);
      if (threads < 1 || threads > 256) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      System.out.println("Incorrect thread value");
      return;
    }
    WordCounter counter = new WordCounter(threads);
    try {
      File inputText = new File(args[1]);
      Scanner reader = new Scanner(inputText);
      while (reader.hasNextLine()) {
        String data = reader.nextLine();
        counter.countWords(data);
      }
      reader.close();
    } catch (FileNotFoundException e) {
      System.out.println("File " + args[1] + " not found.");
      e.printStackTrace();
      return;
    }
    counter.shutdownExecutor();
    counter.printThreads();
    counter.printResults();
  }
}
