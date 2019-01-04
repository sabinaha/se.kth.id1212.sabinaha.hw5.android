package server.model;

import java.util.LinkedList;

import server.exceptions.AlreadyGuessedException;
import server.integration.LineReader;
import shared.GameInfo;
import shared.GameState;

/**
 * This object represents a hangman game.
 */
public class Game {

    private int score;
    private int remainingGuesses;
    private SoughtWord soughtWord;
    private LinkedList<Character> guesses;
    private GameState gameState;

    /**
     * Create a new game instance. Starts fresh with score: 0 and a new word.
     */
    public Game() {
        this.score = 0;
        soughtWord = new SoughtWord(LineReader.getRandomLineFromFile());
        System.out.println("Debug | word: " + soughtWord);
        this.remainingGuesses = soughtWord.length();
        guesses = new LinkedList<>();
        this.gameState = GameState.GAME_ONGOING;
    }

    /**
     * Creates a new game from a old game state, preserving the previous score and player id.
     * @param oldGame The previous game to which from restore from
     */
    public Game(Game oldGame) {
        this.score = oldGame.getScore();
        soughtWord = new SoughtWord(LineReader.getRandomLineFromFile());
        System.out.println("Debug | word: " + soughtWord);
        this.remainingGuesses = soughtWord.length();
        guesses = new LinkedList<>();
        this.gameState = GameState.GAME_ONGOING;
    }


    private int getScore() {
        return this.score;
    }

    /**
     * Concedes the game, resulting in -1 points.
     */
    public void concede() {
        this.gameState = GameState.GAME_LOST;
        this.score--;
    }

    /**
     * Makes a word or letter guess in the hangman game.
     * @param word The word or letter to guess.
     * @throws AlreadyGuessedException If the word or letter has already been guessed this exception is thrown.
     */
    public void makeGuess(String word) throws AlreadyGuessedException {
        boolean correct;
        if (word.length() > 1) {
            correct = soughtWord.matches(word);
        } else {
            if (guesses.contains(word.charAt(0)))
                throw new AlreadyGuessedException("You already guessed this character or word");
            correct = soughtWord.containsChar(word.charAt(0));
            guesses.add(word.charAt(0));
        }
        if (!correct)
            remainingGuesses--;

        updateGameState();
    }

    /**
     * Updates the game state to check if the game is still ongoing, won or lost.
     */
    private void updateGameState() {
        if (remainingGuesses < 1) {
            score--;
            this.gameState = GameState.GAME_LOST;
            return;
        }
        char c;
        for (int i = 0; i < this.soughtWord.getProgress().length; i++) {
            c = this.soughtWord.getProgress()[i];
            if (c != this.soughtWord.toString().charAt(i)) {
                this.gameState = GameState.GAME_ONGOING;
                return;
            }
        }
        score++;
        this.gameState = GameState.GAME_WON;
    }

    /**
     * Gets the game state at this time.
     * @return A <code>GameInfo</code> object to represent the game state.
     */
    public GameInfo getGameInfo() {
        return new GameInfo(soughtWord.getProgress(), getGuessesAsArray(), this.remainingGuesses, this.score, this.soughtWord.toString(), this.gameState);
    }

    private char[] getGuessesAsArray() {
        LinkedList<Character> failed = new LinkedList<>();
        for (Character guess : guesses) {
            if (!soughtWord.containsChar(guess))
                failed.add(guess);
        }
        char[] returnArr = new char[failed.size()];
        int i = 0;
        for (Character character : failed) {
            returnArr[i++] = character;
        }
        return returnArr;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public String getWord() {
        return this.soughtWord.toString();
    }
}