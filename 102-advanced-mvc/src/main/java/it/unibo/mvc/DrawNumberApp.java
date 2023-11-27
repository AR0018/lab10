package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import it.unibo.mvc.Configuration.Builder;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    //TODO: fix this method (find a way to create a new model with the read parameters)
    private Configuration loadConfiguration(String path) throws IOException {
        try( BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                new FileInputStream(path), StandardCharsets.UTF_8))) {
            StringTokenizer minTokenizer = new StringTokenizer(reader.readLine());
            minTokenizer.nextToken();
            final int min = Integer.parseInt(minTokenizer.nextToken());
            StringTokenizer maxTokenizer = new StringTokenizer(reader.readLine());
            maxTokenizer.nextToken();
            final int max = Integer.parseInt(maxTokenizer.nextToken());
            StringTokenizer attemptsTokenizer = new StringTokenizer(reader.readLine());
            attemptsTokenizer.nextToken();
            final int attempts = Integer.parseInt(attemptsTokenizer.nextToken());
            Builder builder = new Builder();
            builder = builder.setMin(min).setMax(max).setAttempts(attempts);
            return builder.build();
        }catch(final IOException e) {
            for(final DrawNumberView view : views) {
                view.displayError(e.getMessage());
            }
        }
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
