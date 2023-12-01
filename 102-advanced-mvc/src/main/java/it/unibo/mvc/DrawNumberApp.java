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
    private static final String PATH_SEPARATOR = System.getProperty("file.separator");
    private static final String PATH =
            System.getProperty("user.dir") + PATH_SEPARATOR
            + "102-advanced-mvc" + PATH_SEPARATOR 
            + "src" + PATH_SEPARATOR 
            + "main" + PATH_SEPARATOR 
            + "resources" + PATH_SEPARATOR 
            + "config.yml";

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
        Configuration conf = loadConfiguration(PATH);
        this.model = new DrawNumberImpl(conf.getMin(), conf.getMax(), conf.getAttempts());
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

    /*
     * Loads a configuration from the file. Returns a default Configuration
     * if there is an error while reading the file.
     */
    private Configuration loadConfiguration(final String path) {
        try (BufferedReader reader = new BufferedReader(
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
            return new Builder().setMin(min).setMax(max).setAttempts(attempts).build();
        } catch (final IOException e) {
            for (final DrawNumberView view : views) {
                view.displayError(e.getMessage());
            }
            return new Builder().build();
        }
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(
            new DrawNumberViewImpl(),
            new DrawNumberViewImpl(), 
            new PrintStreamView(System.out), 
            new PrintStreamView("output.log"));
    }

}
