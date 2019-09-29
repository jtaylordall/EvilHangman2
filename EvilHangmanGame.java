package hangman;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame {
    private SortedSet<String> gameDictionary;
    private SortedSet<Character> guessedLetters;
    private int guesses;
    private String word;

    public EvilHangmanGame(){
        this.gameDictionary = new TreeSet<>();
        this.guessedLetters = new TreeSet<>();
    }

    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException {
        this.gameDictionary = new TreeSet<>();
        this.guessedLetters = new TreeSet<>();
        this.word = initialWord(wordLength);
        try(Scanner scan = new Scanner(dictionary)){
            if(scan.hasNext()){
                while(scan.hasNext()){
                    String wordIn = scan.next().toLowerCase();
                    int wordInLength = wordIn.length();
                    if (wordIn.endsWith("-")){
                        wordInLength--;
                    }
                    if(wordInLength == wordLength){
                        boolean valid = true;
                        for(int a = 0; a < wordInLength; a++){
                            if(wordIn.charAt(a) < 'a' || wordIn.charAt(a) > 'z'){
                                valid = false;
                            }
                        }
                        if (valid){
                            gameDictionary.add(wordIn);
                        }
                    }
                }
                if (gameDictionary.size() < 1){
                    throw new  EmptyDictionaryException();
                }
            } else {
                throw new EmptyDictionaryException();
            }
        }
        //printDict();
    }

    public void playGame(int guesses){
        try(Scanner userInput = new Scanner(System.in).useDelimiter("\\n")){
            this.guesses = guesses;
            boolean win = false;
            while(this.guesses > 0 && !win) {
                System.out.println("You have " + this.guesses + " guesses left");
                System.out.println("Used Letters:" + toStringGuessedLetters());
                System.out.println("Word: " + word);
                System.out.print("Enter guess: ");
                try {
                    String in = userInput.nextLine().toLowerCase();
                    if (in.length() != 1) {
                        throw new InvalidInputException();
                    } else {
                        char c = in.charAt(0);
                        if (c <'a' || c > 'z'){
                            throw new InvalidInputException();
                        } else {
                            makeGuess(c);
                        }
                    }
                    if(containsHowMany('-', this.word) == 0){
                        System.out.println("You win!");
                        win = true;
                    } else if (this.guesses == 0){
                        System.out.println("You Lose!");
                        System.out.println("The word was: " + generateRandomWord());
                    }
                } catch (InvalidInputException ex) {
                    System.out.println("Invalid input");
                } catch (GuessAlreadyMadeException ex){
                    System.out.println("Guess already made");
                } finally {
                    System.out.println();
                }
            }
        }
        this.gameDictionary = new TreeSet<>();
        this.guessedLetters = new TreeSet<>();
    }

    public void printDict(){
        System.out.println("Dictionary");
        for (String s : gameDictionary){
            System.out.print(s + " ");
        }
        System.out.println("Size: " + gameDictionary.size());
    }

    @Override
    public Set<String> makeGuess(char guessIn) throws GuessAlreadyMadeException {
        char guess = String.valueOf(guessIn).toLowerCase().charAt(0);
        if (guessedLetters.contains(guess)){
            throw new GuessAlreadyMadeException();
        } else {
            guessedLetters.add(guess);
            selectGroup(partition(guess),guess);
            int howMany = containsHowMany(guess, word);
            if(word.contains("-")) {
                if (howMany > 0) {
                    System.out.println("Yes, there is " + howMany + " " + guess + "'s");
                } else {
                    System.out.println("Sorry, there are no " + guess + "'s");
                    this.guesses--;
                }
            }
        }
        System.out.println();
        printDict();
        return gameDictionary;
    }

    public Map<String,Vector<String>> partition(char guess){
        Map<String, Vector<String>> groups = new TreeMap<>();
        for(String s : gameDictionary){
            StringBuilder sb = new StringBuilder();
            for (int a = 0; a < s.length(); a++){
                if(s.charAt(a) == guess){
                    sb.append(guess);
                } else {
                    sb.append('-');
                }
            }
            if (!groups.containsKey(sb.toString())){
                Vector<String> v = new Vector<>();
                v.add(s);
                groups.put(sb.toString(), v);
            } else {
                groups.get(sb.toString()).add(s);
            }
        }
        //System.out.println(groups);
        return groups;
    }

    public void selectGroup(Map<String,Vector<String>> groups, char guess){
        Map<String,Vector<String>> out;
        String outS = "";
        Vector<String> outV;
        out = selectBiggest(groups);
        if(out.size() > 1){
            out = selectFewestLetters(out);
            if(out.size() > 1){
                outS = selectRightMost(out);
                outV = groups.get(outS);
            } else {
                outS = out.entrySet().iterator().next().getKey();
                outV = out.entrySet().iterator().next().getValue();
            }
        } else {
            outS = out.entrySet().iterator().next().getKey();
            outV = out.entrySet().iterator().next().getValue();
        }
        updateWord(outS);
        this.gameDictionary = new TreeSet<>(outV);
    }

    public Map<String, Vector<String>> selectBiggest(Map<String,Vector<String>> groups){
        Map<String,Vector<String>> groupsOut = new TreeMap<>(groups);
        int biggest = 0;
        for(Map.Entry<String,Vector<String>> entry : groups.entrySet()){
            int size = entry.getValue().size();
            if(size > biggest){
                biggest = size;
            }
        }
        Vector<String> removeSmall = new Vector<>();
        for(Map.Entry<String,Vector<String>> entry : groups.entrySet()){
            int size = entry.getValue().size();
            if(size < biggest){
                removeSmall.add(entry.getKey());
            }
        }
        for (String s : removeSmall){
            groupsOut.remove(s);
        }
        return groupsOut;
    }

    public Map<String, Vector<String>> selectFewestLetters(Map<String,Vector<String>> groups){
        Map<String,Vector<String>> groupsOut = new TreeMap<>(groups);
        int emptiest = 0;
        for(Map.Entry<String,Vector<String>> entry : groups.entrySet()){
            int dashCount = containsHowMany('-', entry.getKey());
            if(dashCount > emptiest){
                emptiest = dashCount;
            }
        }
        Vector<String> removeManyLetters = new Vector<>();
        for(Map.Entry<String,Vector<String>> entry : groups.entrySet()){
            int dashCount = containsHowMany('-', entry.getKey());
            if(dashCount < emptiest){
                removeManyLetters.add(entry.getKey());
            }
        }
        for (String s : removeManyLetters){
            groupsOut.remove(s);
        }
        return groupsOut;
    }

    public String selectRightMost(Map<String,Vector<String>> groups){
        Vector<String> v = new Vector<>(groups.keySet());
        return rightMost(v, word.length());
    }

    public String rightMost(Vector<String> v, int subLength){
        Vector<String> vOut = new Vector<>();
        for(String s : v){
            for (int a = subLength - 1; a >= 0; a--){
                if (s.charAt(a) != '-'){
                    vOut.add(s);
                }
            }
            if(vOut.size() == 1){
                return vOut.elementAt(0);
            }
        }
        return rightMost(vOut, subLength - 1);
    }

    @Override
    public SortedSet<Character> getGuessedLetters() {
        return guessedLetters;
    }
    
    public String toStringGuessedLetters(){
        StringBuilder sb = new StringBuilder();
        for(char c : guessedLetters){
            sb.append(" ").append(c);
        }
        return sb.toString();
    }

    public String initialWord(int wordLength){
        StringBuilder sb = new StringBuilder();
        sb.append("-".repeat(Math.max(0, wordLength)));
        return sb.toString();
    }

    public void updateWord(String in){
        StringBuilder out = new StringBuilder();
        for (int a = 0; a < in.length(); a++){
            if(in.charAt(a) != '-'){
                out.append(in.charAt(a));
            } else {
                out.append(word.charAt(a));
            }
        }
        word = out.toString();
    }

    private int containsHowMany(char c, String s){
        int count = 0;
        for (int a = 0; a < s.length(); a++){
            if (s.charAt(a) == c){
                count++;
            }
        }
        return count;
    }

    private String generateRandomWord(){
        Random rand = new Random();
        String toReturn = "";
        int a = rand.nextInt(gameDictionary.size());
        int b = 0;
        for(String s : gameDictionary){
            if(a == b){
                toReturn = s;
                break;
            }
            b++;
        }
        return toReturn;
    }
}