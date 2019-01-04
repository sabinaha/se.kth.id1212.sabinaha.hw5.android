package se.kth.id1212.sabinaha.hw5.android.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import se.kth.id1212.sabinaha.hw5.android.client.model.GameActionFeedback;
import se.kth.id1212.sabinaha.hw5.android.client.net.ServerCallback;
import se.kth.id1212.sabinaha.hw5.android.client.net.ServerHandler;

public class HangmanActivity extends AppCompatActivity implements ServerCallback {

    TextView mRemainingGuesses;
    TextView mScore;
    TextView mWordProgress;
    TextView mGuessedLetters;
    TextView mSecretWord;

    EditText mEnteredGuess;

    Button mGuessButton;
    Button mStartGame;
    Button mQuitGame;

    private boolean ongoingGame = false;

    private boolean connectedToServer = false;

    private ServerHandler serverHandler = new ServerHandler("10.0.2.2", 4455, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hangman);

        mRemainingGuesses = findViewById(R.id.tv_remaining_guesses);
        mScore = findViewById(R.id.tv_score);
        mWordProgress = findViewById(R.id.tv_progress);
        mGuessedLetters = findViewById(R.id.tv_letters_guessed);
        mSecretWord = findViewById(R.id.tv_secret_word);

        mEnteredGuess = findViewById(R.id.et_enter_guess);

        mGuessButton = findViewById(R.id.btn_guess_letter);
        mStartGame = findViewById(R.id.btn_start_game);
        mQuitGame = findViewById(R.id.btn_quit_game);

        serverHandler.connect();

        mStartGame.setEnabled(true);

        mGuessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverHandler.makeGuess(mEnteredGuess.getText().toString());
            }
        });

        mStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearUI(false);
                mGuessButton.setEnabled(true);
                mQuitGame.setEnabled(true);
                mEnteredGuess.setEnabled(true);
                if (ongoingGame) {
                    serverHandler.restartGame();
                } else {
                    serverHandler.startGame();
                }
            }
        });

        mQuitGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectedToServer) {
                    serverHandler.quitGame();
                    mQuitGame.setText("Reconnect to server");
                } else {
                    serverHandler.connect();
                    mQuitGame.setText(R.string.button_quit);
                }
            }
        });
    }

    private void clearUI (final boolean fullClear) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fullClear) {
                    mRemainingGuesses.setText(R.string.remaining_guesses);
                    mScore.setText(R.string.score);
                    mWordProgress.setText(null);
                }
                mSecretWord.setText(null);
                mGuessedLetters.setText(null);
            }
        });
    }

    @Override
    public void messageSent() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEnteredGuess.setText(null);
            }
        });
    }

    @Override
    public void messageReceived(String receivedMessage) {
        if (!receivedMessage.contains("|"))
            informAction(receivedMessage);
        else {
            class UIRunner {
                String[] params;

                private UIRunner (String[] params) {
                    this.params = params;
                }

                void updateUI() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mWordProgress.setText(params[1].trim());

                            if (params[2].trim().equals("_"))
                                mGuessedLetters.setText(null);
                            else
                                mGuessedLetters.setText(getString(R.string.guessed_letter) + " " + params[2]);

                            mRemainingGuesses.setText(getString(R.string.remaining_guesses) + " " + params[3]);
                            mScore.setText(getString(R.string.score) + " " + params[4]);

                            if (params.length > 5) {
                                mSecretWord.setText(getString(R.string.secret_word) + " " + params[5]);
                            }
                        }
                    });
                }
            }
            String[] params = receivedMessage.split("\\|");
            new UIRunner(params).updateUI();
        }
    }

    @Override
    public void notifyConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectedToServer = true;
                mQuitGame.setText(R.string.button_quit);
                mStartGame.setEnabled(true);
                Toast.makeText(HangmanActivity.this, "Connected to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setGameInteractionEnabled (boolean enabled) {
        mStartGame.setEnabled(enabled);
        mGuessButton.setEnabled(enabled);
        mEnteredGuess.setEnabled(enabled);
    }

    @Override
    public void notifyDisconnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectedToServer = false;
                setGameInteractionEnabled(false);
                Toast.makeText(HangmanActivity.this, "Disconnected from server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setGameOngoing (boolean ongoing) {
        ongoingGame = ongoing;
        if (ongoingGame) {
            mStartGame.setText(R.string.button_restart);
        } else {
            mStartGame.setText(R.string.button_start_game);
        }
    }

    private void informAction (String message) {
        class UIRunner {
            private final String message;

            private UIRunner (String message) {
                this.message = message;
            }

            private void displayMessage() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String toastMessage = "Unknown message!";
                        GameActionFeedback gaf = GameActionFeedback.valueOf(message);
                        switch (gaf) {
                            case DUPLICATE_GUESS:
                                toastMessage = "You already guessed this";
                                break;
                            case NO_GAME_STARTED:
                                toastMessage = "No game started";
                                break;
                            case GAME_WON:
                                setGameOngoing(false);
                                toastMessage = "YOU WON!";
                                break;
                            case GAME_LOST:
                                setGameOngoing(false);
                                toastMessage = "YOU LOST";
                                break;
                            case GAME_QUIT:
                                toastMessage = "Quit game";
                                clearUI(true);
                                break;
                            case GAME_ONGOING:
                                toastMessage = "A game is already ongoing";
                                break;
                            case GAME_STARTED:
                                setGameOngoing(false);
                                toastMessage = "Game started";
                                clearUI(false);
                                setGameOngoing(true);
                                break;
                            case GAME_RESTARTED:
                                toastMessage = "Game restarted";
                                clearUI(false);
                                mGuessedLetters.setText(null);
                                break;
                            case INVALID_COMMAND:
                                toastMessage = "Invalid command";
                                break;
                            case HELP:
                                mSecretWord.setText(null);
                                mGuessedLetters.setText(null);
                            case GAME_INFO:
                                return;
                        }
                        Toast.makeText(HangmanActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        new UIRunner(message).displayMessage();
    }
}
