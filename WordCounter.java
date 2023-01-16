import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class WordCounter {
    HashMap<String, Integer> wordsMap;
    public WordCounter(int threads){
        wordsMap = new HashMap<>();
    }
    public void countWords(String data){
        String[] words = data.toLowerCase().split("[\s\n]");
        Stream<String> filteredWords = Arrays.stream(words).map(word-> {
            Stream<Character> letters = word.codePoints().mapToObj(c -> (char) c);
            Stream<Character> lettersFiltered = letters.filter(Character::isLetter);
            return lettersFiltered.collect(Collector.of(StringBuilder::new, StringBuilder::append, StringBuilder::append, StringBuilder::toString));
        });
        filteredWords.filter(word -> word.length() > 1).forEach(word -> {
            if(!this.wordsMap.containsKey(word)) {
                this.wordsMap.put(word, 1);
                return;
            }
            this.wordsMap.put(word, this.wordsMap.get(word) + 1);
        });
    }
    public static void main(String[] args){
        if (args == null || args.length != 2) {
            System.out.println("Incorrect argument list");
            return;
        }
        int threads;
        try {
            threads = Integer.parseInt(args[0]);
            if (threads < 1 || threads > 256){
                throw new NumberFormatException();
            }
        } catch(NumberFormatException e){
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
        System.out.println(counter.wordsMap);
    }
}
