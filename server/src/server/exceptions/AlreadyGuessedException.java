package server.exceptions;

/**
 * If a user guesses a word or letter which is already guessed.
 */
public class AlreadyGuessedException extends Exception {
    public AlreadyGuessedException(String s) {
        super(s);
    }
}
