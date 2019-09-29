package hangman;

import java.io.File;

public class EvilHangman {

    public static void main(String[] args) {
        String dictionaryName = args[0];
        int wordLength = Integer.parseInt(args[1]);
        int guesses = Integer.parseInt(args[2]);
        try {
            if (wordLength >= 2 && guesses > 0) {
                EvilHangmanGame evilHangmanGame = new EvilHangmanGame();
                evilHangmanGame.startGame(new File(dictionaryName), wordLength);
                evilHangmanGame.playGame(guesses);
            } else {
                throw new InvalidInputException();
            }
        } catch (InvalidInputException | ArrayIndexOutOfBoundsException ex){
            System.out.println("Invalid Input");
        } catch (EmptyDictionaryException ex){
            System.out.println("Empty Dictionary");
        } catch (Exception ex){
            System.out.println("Something Went Wrong");
        }
    }

}
