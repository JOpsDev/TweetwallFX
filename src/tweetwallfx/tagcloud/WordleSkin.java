/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import twitter.TweetInfo;

/**
 *
 * @author sven
 */
public class WordleSkin extends SkinBase<Wordle> {

    private final Random rand = new Random();
    private final int dDeg = 10;
    private final double dRadius = 5.0;

    // used for TagCloud
    private final Map<Word, Text> word2TextMap = new HashMap<>();
    // 
    private final List<TweetWordNode> tweetWordList = new ArrayList<>();
    private double max;
    private double min;
    private final Pane pane;
    private List<Word> limitedWords;
    private Set<Word> tweetWords = Collections.emptySet();
    private HBox hbox;
    private Point2D lowerLeft;
    private HBox mediaBox;

    public WordleSkin(Wordle wordle) {
        super(wordle);

        pane = new Pane();
//        pane.setStyle("-fx-border-width: 1px; -fx-border-color: black;");
        this.getChildren().add(pane);
        updateCloud();

        wordle.wordsProperty.addListener((obs, oldValue, newValue) -> {
            switch (wordle.layoutModeProperty.get()) {
                case TWEET:
                    break;
                case WORDLE:
                    updateCloud();
                    break;
            }
        });

        pane.widthProperty().addListener(bounds -> {
            switch (wordle.layoutModeProperty.get()) {
                case TWEET:
                    break;
                case WORDLE:
                    updateCloud();
                    break;
            }
        });

        pane.heightProperty().addListener(bounds -> {
            switch (wordle.layoutModeProperty.get()) {
                case TWEET:
                    break;
                case WORDLE:
                    updateCloud();
                    break;
            }
        });

        wordle.layoutModeProperty.addListener((obs, oldVModee, newMode) -> {
            switch (newMode) {
                case TWEET:
//                    addTweetToCloud();

                    cloudToTweet();
                    break;
                case WORDLE:
                    tweetToCloud();
//                    removeTweetFromCloud();
                    break;
            }
        });

        wordle.tweetInfoProperty.addListener((obs, oldValue, newValue) -> {
            String text = newValue.getText();
            tweetWords = pattern.splitAsStream(text)
                    .filter(l -> l.length() > 2)
                    .filter(l -> !l.startsWith("@"))
                    .filter(l -> !l.startsWith("http:"))
                    .filter(l -> !l.startsWith("https:"))
                    .map(l -> l.toLowerCase())
                    .filter(l -> !stopList.contains(l)).map(l -> new Word(l, -2)).collect(Collectors.toSet());

        });
    }

    private final List<String> stopList = new ArrayList<>(
            Arrays.asList("http", "https", "has", "have", "do", "for", "are", "the", "and",
                    "with", "here", "#devoxx", "active", "see", "next", "will", "any", "off", "there", "while", "just", "all", "from", "got", "think", "nice",
                    "ask", "can", "you", "week", "some", "not", "didn", "isn", "per", "how", "show", "out", "but", "last", "your", "one", "should",
                    "now", "also", "done", "will", "become", "did", "what", "when", "let", "that", "this", "always", "where", "our"));

//    private void addTweetToCloud() {
//        System.out.println("Add tweet to cloud");
//        String text = getSkinnable().tweetInfoProperty.get().getText();
//        tweetWords = pattern.splitAsStream(text)
//                .filter(l -> l.length() > 2)
//                .filter(l -> !l.startsWith("@"))
//                .filter(l -> !l.startsWith("http:"))
//                .filter(l -> !l.startsWith("https:"))
//                .map(l -> l.toLowerCase())
//                .filter(l -> !stopList.contains(l)).map(l -> new Word(l, 0)).collect(Collectors.toSet());                
//        List<Word> words = getSkinnable().wordsProperty.get();
//        words.addAll(tweetWords);
//        Platform.runLater(() -> getSkinnable().wordsProperty.set(words));
//    }
    private void removeTweetFromCloud() {
        tweetWords = Collections.emptySet();
        updateCloud();
    }

//    private Point2D layoutTweetWord(Bounds targetBounds, Point2D upperLeft, double maxWidth) {
//        double y = upperLeft.getY() + targetBounds.getMinY();
//        double x = upperLeft.getX() + targetBounds.getMinX();
//        double rightMargin = upperLeft.getX() + maxWidth;
//        while (x + targetBounds.getWidth() > rightMargin) {
//            y += targetBounds.getHeight();
//            x -= maxWidth;
//        }
//        return new Point2D(x, y);
//    }
    private Point2D tweetWordLineOffset(Bounds targetBounds, Point2D upperLeft, double maxWidth, Point2D lineOffset) {
        double y = upperLeft.getY() + targetBounds.getMinY();
        double x = upperLeft.getX() + targetBounds.getMinX() - lineOffset.getX();
        double rightMargin = upperLeft.getX() + maxWidth;
        if (x + targetBounds.getWidth() > rightMargin) {
            return new Point2D(lineOffset.getX() + (x - upperLeft.getX()), lineOffset.getY() + targetBounds.getHeight());
        }
        return lineOffset;
    }

    private Point2D layoutTweetWord(Bounds targetBounds, Point2D upperLeft, Point2D lineOffset) {
        double y = upperLeft.getY() + targetBounds.getMinY() + lineOffset.getY();
        double x = upperLeft.getX() + targetBounds.getMinX() - lineOffset.getX();
        return new Point2D(x, y);
    }
    
    Point2D tweetLineOffset = new Point2D(0, 0);

    private void cloudToTweet() {

        Bounds layoutBounds = pane.getLayoutBounds();
        TweetInfo tweetInfo = getSkinnable().tweetInfoProperty.get();

        Point2D minPosTweetText = new Point2D(layoutBounds.getWidth() / 6d, layoutBounds.getHeight() / 3d);

        double width = layoutBounds.getWidth() * (2 / 3d);

        List<TweetWord> tweetLayout = recalcTweetLayout(tweetInfo);

        ParallelTransition fadeOuts = new ParallelTransition();
        ParallelTransition moves = new ParallelTransition();
        ParallelTransition fadeIns = new ParallelTransition();
        SequentialTransition morph = new SequentialTransition(fadeOuts, moves, fadeIns);

        lowerLeft = new Point2D(minPosTweetText.getX(), minPosTweetText.getY());
        tweetLineOffset = new Point2D(0, 0);

        tweetLayout.stream().forEach(tweetWord -> {
            Word word = new Word(tweetWord.text.trim(), -2);
            if (word2TextMap.containsKey(word)) {
                Text textNode = word2TextMap.remove(word);
                tweetWordList.add(new TweetWordNode(tweetWord, textNode));

                FontSizeTransition ft = new FontSizeTransition(Duration.seconds(1.5), textNode);
                ft.setFromSize(textNode.getFont().getSize());
                ft.setToSize(getFontSize(-1));
                moves.getChildren().add(ft);

                Bounds bounds = tweetLayout.stream().filter(tw -> tw.text.trim().equals(word.text)).findFirst().get().bounds;

                LocationTransition lt = new LocationTransition(Duration.seconds(1.5), textNode);
                lt.setFromX(textNode.getLayoutX());
                lt.setFromY(textNode.getLayoutY());
                tweetLineOffset = tweetWordLineOffset(bounds, lowerLeft, width, tweetLineOffset);
                Point2D twPoint = layoutTweetWord(bounds, minPosTweetText, tweetLineOffset);
                lt.setToX(twPoint.getX());
                lt.setToY(twPoint.getY());
                if (twPoint.getY() > lowerLeft.getY()) {
                    lowerLeft = lowerLeft.add(0, twPoint.getY() - lowerLeft.getY());
                }
                moves.getChildren().add(lt);
            } else {
                Text textNode = createTextNode(word);

                fontSizeAdaption(textNode, -1);
                tweetWordList.add(new TweetWordNode(tweetWord, textNode));

                Bounds bounds = tweetWord.bounds;

                tweetLineOffset = tweetWordLineOffset(bounds, lowerLeft, width, tweetLineOffset);
                Point2D twPoint = layoutTweetWord(bounds, minPosTweetText, tweetLineOffset);

                textNode.setLayoutX(twPoint.getX());
                textNode.setLayoutY(twPoint.getY());
                if (twPoint.getY() > lowerLeft.getY()) {
                    lowerLeft = lowerLeft.add(0, twPoint.getY() - lowerLeft.getY());
                }
                textNode.setOpacity(0);
                pane.getChildren().add(textNode);
                FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
                ft.setToValue(1);
                fadeIns.getChildren().add(ft);
            }
        });

        // kill the remaining words from the cloud
        word2TextMap.entrySet().forEach(entry -> {
            Text textNode = entry.getValue();
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
            ft.setToValue(0);
            fadeOuts.getChildren().add(ft);
            ft.setOnFinished((event) -> {
                pane.getChildren().remove(textNode);
            });
        });
        word2TextMap.clear();

        // layout image and meta data first
        hbox = new HBox(20);
        hbox.setStyle("-fx-padding: 20px;");
        hbox.setPrefHeight(80);
        hbox.setMaxHeight(80);
        hbox.setLayoutX(lowerLeft.getX());
        hbox.setLayoutY(lowerLeft.getY());

        HBox hImage = new HBox();
        hImage.setPadding(new Insets(10));
        Image image = new Image(tweetInfo.getImageURL(), 64, 64, true, false);
        ImageView imageView = new ImageView(image);
        Rectangle clip = new Rectangle(64, 64);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        imageView.setClip(clip);
        hImage.getChildren().add(imageView);

//        HBox hName = new HBox(20);
        Label name = new Label(tweetInfo.getName());
        name.setStyle("-fx-font: 36px \"Calibri\"; -fx-text-fill: #e1ecee; -fx-font-weight: bold;");
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        Label handle = new Label("@" + tweetInfo.getHandle() + " · " + df.format(tweetInfo.getDate()));
        handle.setStyle("-fx-font: 28px \"Calibri\"; -fx-text-fill: #8899A6;");
//        hName.getChildren().addAll(name, handle);
//        hName.setAlignment(Pos.BOTTOM_CENTER);

//        VBox vbox = new VBox(10);
//        vbox.getChildren().addAll(hName);
        hbox.getChildren().addAll(hImage, name, handle);

        hbox.setOpacity(0);
        hbox.setAlignment(Pos.CENTER);
        pane.getChildren().add(hbox);

        if(tweetInfo.getMediaEntities().length > 0) {
//            System.out.println("Media detected: " + tweetInfo.getText() + " " + Arrays.toString(tweetInfo.getMediaEntities()));
            mediaBox = new HBox();
            hImage.setPadding(new Insets(10));
            Image mediaImage = new Image(tweetInfo.getMediaEntities()[0].getMediaURL());
            ImageView mediaView = new ImageView(mediaImage);
            mediaView.setPreserveRatio(true);
            mediaView.setCache(true);
            mediaView.setSmooth(true);
//            Rectangle clip = new Rectangle(64, 64);
//            clip.setArcWidth(10);
//            clip.setArcHeight(10);
//            imageView.setClip(clip);
            mediaBox.getChildren().add(mediaView);
            mediaBox.setOpacity(0);
            mediaBox.setLayoutX(layoutBounds.getWidth()/2d);
            mediaBox.setLayoutY(lowerLeft.getY() + 100);
            mediaBox.setMaxSize(layoutBounds.getWidth()/2d, layoutBounds.getHeight()-50);
            mediaView.setFitWidth(mediaBox.getWidth()-10);
            mediaView.setFitHeight(mediaBox.getHeight()-10);
            // add fade in for image and meta data
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), mediaBox);
            ft.setToValue(1);
            fadeIns.getChildren().add(ft);  
            pane.getChildren().add(mediaBox);
        }        
        
        // add fade in for image and meta data
        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), hbox);
        ft.setToValue(1);
        fadeIns.getChildren().add(ft);

        morph.play();
    }

    private void tweetToCloud() {
        List<Word> sortedWords = new ArrayList<>(getSkinnable().wordsProperty().getValue());

        if (sortedWords.isEmpty()) {
            return;
        }

        limitedWords = sortedWords.stream().limit(MAX_CLOUD_TAGS).collect(Collectors.toList());
        limitedWords.sort(Comparator.reverseOrder());

        max = limitedWords.get(0).weight;
        min = limitedWords.stream().filter(w -> w.weight > 0).min(Comparator.naturalOrder()).get().weight;

        Map<Word, Bounds> boundsMap = recalcTagLayout(limitedWords);

        ParallelTransition fadeOuts = new ParallelTransition();
        ParallelTransition moves = new ParallelTransition();
        ParallelTransition fadeIns = new ParallelTransition();
        SequentialTransition morph = new SequentialTransition(fadeOuts, moves, fadeIns);

        Bounds layoutBounds = pane.getLayoutBounds();

        boundsMap.entrySet().stream().forEach(entry -> {
            Word word = entry.getKey();
            Bounds bounds = entry.getValue();
            Optional<TweetWordNode> optionalTweetWord = tweetWordList.stream().filter(tweetWord -> tweetWord.tweetWord.text.trim().equals(word.text)).findFirst();
            if (optionalTweetWord.isPresent()) {
                boolean removed = tweetWordList.remove(optionalTweetWord.get());
                Text textNode = optionalTweetWord.get().textNode;

                word2TextMap.put(word, textNode);
                LocationTransition lt = new LocationTransition(Duration.seconds(1.5), textNode);

                lt.setFromX(textNode.getLayoutX());
                lt.setFromY(textNode.getLayoutY());
                lt.setToX(bounds.getMinX() + layoutBounds.getWidth() / 2d);
                lt.setToY(bounds.getMinY() + layoutBounds.getHeight() / 2d + bounds.getHeight() / 2d);
                moves.getChildren().add(lt);

                FontSizeTransition ft = new FontSizeTransition(Duration.seconds(1.5), textNode);
                ft.setFromSize(textNode.getFont().getSize());
                ft.setToSize(getFontSize(word.weight));
                moves.getChildren().add(ft);

            } else {
                Text textNode = createTextNode(word);

                word2TextMap.put(word, textNode);
                textNode.setLayoutX(bounds.getMinX() + layoutBounds.getWidth() / 2d);
                textNode.setLayoutY(bounds.getMinY() + layoutBounds.getHeight() / 2d + bounds.getHeight() / 2d);
                textNode.setOpacity(0);
                pane.getChildren().add(textNode);
                FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
                ft.setToValue(1);
                fadeIns.getChildren().add(ft);
            }
        });

        tweetWordList.forEach(tweetWord -> {
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), tweetWord.textNode);
            ft.setToValue(0);
            fadeOuts.getChildren().add(ft);
            ft.setOnFinished((event) -> {
                pane.getChildren().remove(tweetWord.textNode);
            });
        });

        tweetWordList.clear();

        if (null != hbox) {
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), hbox);
            ft.setToValue(0);
            fadeOuts.getChildren().add(ft);
            ft.setOnFinished(event -> {
                pane.getChildren().remove(hbox);
            });
        }
        if (null != mediaBox) {
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), mediaBox);
            ft.setToValue(0);
            fadeOuts.getChildren().add(ft);
            ft.setOnFinished(event -> {
                pane.getChildren().remove(mediaBox);
            });
        }

        morph.play();
    }

    private void updateCloud() {
//        pane.setStyle("-fx-border-width: 1px; -fx-border-color: red;");
        List<Word> sortedWords = new ArrayList<>(getSkinnable().wordsProperty().getValue());
        if (sortedWords.isEmpty()) {
            return;
        }

        limitedWords = sortedWords.stream().limit(MAX_CLOUD_TAGS).collect(Collectors.toList());
        limitedWords.sort(Comparator.reverseOrder());

        max = limitedWords.get(0).weight;
        min = limitedWords.stream().filter(w -> w.weight > 0).min(Comparator.naturalOrder()).get().weight;

        Map<Word, Bounds> boundsMap = recalcTagLayout(limitedWords);
        Bounds layoutBounds = pane.getLayoutBounds();

        List<Word> unusedWords = word2TextMap.keySet().stream().filter(word -> !boundsMap.containsKey(word)).collect(Collectors.toList());
        SequentialTransition morph = new SequentialTransition();

        ParallelTransition fadeOuts = new ParallelTransition();
        unusedWords.forEach(word -> {
            Text textNode = word2TextMap.remove(word);

            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
            ft.setToValue(0);
            fadeOuts.getChildren().add(ft);
            ft.setOnFinished((event) -> {
                pane.getChildren().remove(textNode);
            });
        });

        morph.getChildren().add(fadeOuts);

        List<Word> existingWords = boundsMap.keySet().stream().filter(word -> word2TextMap.containsKey(word)).collect(Collectors.toList());
        ParallelTransition moves = new ParallelTransition();

        existingWords.forEach(word -> {

            Text textNode = word2TextMap.get(word);
            fontSizeAdaption(textNode, word.weight);
            Bounds bounds = boundsMap.get(word);

            LocationTransition lt = new LocationTransition(Duration.seconds(1.5), textNode);
            lt.setFromX(textNode.getLayoutX());
            lt.setFromY(textNode.getLayoutY());
            lt.setToX(bounds.getMinX() + layoutBounds.getWidth() / 2d);
            lt.setToY(bounds.getMinY() + +layoutBounds.getHeight() / 2d + bounds.getHeight() / 2d);
            moves.getChildren().add(lt);
        });

        morph.getChildren().add(moves);

        List<Word> newWords = boundsMap.keySet().stream().filter(word -> !word2TextMap.containsKey(word)).collect(Collectors.toList());
        ParallelTransition fadeIns = new ParallelTransition();

        newWords.forEach(word -> {
            Text textNode = createTextNode(word);
            word2TextMap.put(word, textNode);

            Bounds bounds = boundsMap.get(word);
            textNode.setLayoutX(bounds.getMinX() + layoutBounds.getWidth() / 2d);
            textNode.setLayoutY(bounds.getMinY() + layoutBounds.getHeight() / 2d + bounds.getHeight() / 2d);
            textNode.setOpacity(0);
            pane.getChildren().add(textNode);
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
            ft.setToValue(1);
            fadeIns.getChildren().add(ft);
        });
        morph.getChildren().add(fadeIns);
        morph.play();
    }

    private static final int MAX_CLOUD_TAGS = 50;

    private final Font defaultFont = Font.font("Calibri", FontWeight.BOLD, MINIMUM_FONT_SIZE);

    private double getFontSize(double weight) {
        // maxFont = 48
        // minFont = 18

        double size = defaultFont.getSize();
        if (weight == -1) {
            size = TWEET_FONT_SIZE;
        } else if (weight == -2) {
            size = MINIMUM_FONT_SIZE -10;
        } else {
            // linear
            //y = a+bx
//        double size = defaultFont.getSize() + ((48-defaultFont.getSize())/(max-min)) * word.weight;
            // logarithmic
            // y = a * Math.ln(x) + b
            double a = (defaultFont.getSize() - MAX_FONT_SIZE) / (Math.log(min / max));
            double b = defaultFont.getSize() - a * Math.log(min);
            size = a * Math.log(weight) + b;
        }
        return size;
    }

    private void fontSizeAdaption(Text text, double weight) {
        text.setFont(Font.font(defaultFont.getFamily(), getFontSize(weight)));
    }

    private Text createTextNode(Word word) {
        Text textNode = new Text(word.text);
        textNode.setStyle("-fx-fill: white; -fx-padding: 5px");
        fontSizeAdaption(textNode, word.weight);
        return textNode;
    }

    private final Pattern pattern = Pattern.compile("\\s+");

    private List<TweetWord> recalcTweetLayout(TweetInfo info) {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(300);
        pattern.splitAsStream(info.getText())
                .forEach(w -> {
                    Text textWord = new Text(w.concat(" "));
                    String color = "#292F33";
                    textWord.setStyle("-fx-fill: " + color + ";");
                    textWord.setFont(Font.font(defaultFont.getFamily(), TWEET_FONT_SIZE));
                    flow.getChildren().add(textWord);
                });
        flow.requestLayout();
        return flow.getChildren().stream().map(node -> new TweetWord(node.getBoundsInParent(), ((Text) node).getText())).collect(Collectors.toList());
    }
    private static final int TWEET_FONT_SIZE = 72;
    private static final int MINIMUM_FONT_SIZE = 36;
    private static final int MAX_FONT_SIZE = 72;


    private Map<Word, Bounds> recalcTagLayout(List<Word> words) {
        List<Bounds> boundsList = new ArrayList<>();
        Text firstNode = createTextNode(words.get(0));
        double firstWidth = firstNode.getLayoutBounds().getWidth();
        double firstHeight = firstNode.getLayoutBounds().getHeight();

        boundsList.add(new BoundingBox(0,
                0, firstWidth, firstHeight));

        for (int i = 1; i < words.size(); ++i) {
            Word word = words.get(i);
            Text textNode = createTextNode(word);
            double width = textNode.getLayoutBounds().getWidth();
            double height = textNode.getLayoutBounds().getHeight();

            Point2D center = new Point2D(0, 0);
            double totalWeight = 0.0;
            for (int prev = 0; prev < i; ++prev) {
                Bounds prevBounds = boundsList.get(prev);
                double weight = words.get(prev).weight;
                center = center.add((prevBounds.getWidth() / 2d) * weight, (prevBounds.getHeight() / 2d) * weight);
                totalWeight += weight;
            }
            center = center.multiply(1d / totalWeight);
            boolean done = false;
            double radius = 0.5 * Math.min(boundsList.get(0).getWidth(), boundsList.get(0).getHeight());
            while (!done) {
                int startDeg = rand.nextInt(360);
                double prev_x = -1;
                double prev_y = -1;
                for (int deg = startDeg; deg < startDeg + 360; deg += dDeg) {
                    double rad = ((double) deg / Math.PI) * 180.0;
                    center = center.add(radius * Math.cos(rad), radius * Math.sin(rad));
                    if (prev_x == center.getX() && prev_y == center.getY()) {
                        continue;
                    }
                    prev_x = center.getX();
                    prev_y = center.getY();
                    Bounds mayBe = new BoundingBox(center.getX() - width / 2d,
                            center.getY() - height / 2d, width, height);
                    boolean useable = true;
                    for (int prev = 0; prev < i; ++prev) {
                        if (mayBe.intersects(boundsList.get(prev))) {
                            useable = false;
                            break;
                        }
                    }
                    if (useable) {
                        done = true;
                        boundsList.add(new BoundingBox(center.getX() - width / 2d,
                                center.getY() - height / 2d, width, height));
                        break;
                    }
                }
                radius += this.dRadius;
            }
        }

        Map<Word, Bounds> boundsMap = new HashMap<>();

        for (int k = 0; k < words.size(); k++) {
            boundsMap.put(words.get(k), boundsList.get(k));
        }
        return boundsMap;
    }

    private static class LocationTransition extends Transition {

        private final Node node;
        private double startX;
        private double startY;
        private double targetY;
        private double targetX;

        public LocationTransition(Duration duration, Node node) {
            setCycleDuration(duration);
            this.node = node;
        }

        public void setFromX(double startX) {
            this.startX = startX;
        }

        public void setFromY(double startY) {
            this.startY = startY;
        }

        public void setToX(double targetX) {
            this.targetX = targetX;
        }

        public void setToY(double targetY) {
            this.targetY = targetY;
        }

        @Override
        protected void interpolate(double frac) {
            if (!Double.isNaN(startX)) {
                node.setLayoutX(startX + frac * (targetX - startX));
            }
            if (!Double.isNaN(startY)) {
                node.setLayoutY(startY + frac * (targetY - startY));
            }
        }

    }

    private static class FontSizeTransition extends Transition {

        private final Text node;
        private double startSize;
        private double toSize;

        public FontSizeTransition(Duration duration, Text node) {
            setCycleDuration(duration);
            this.node = node;
        }

        public void setFromSize(double startSize) {
            this.startSize = startSize;
        }

        public void setToSize(double toSize) {
            this.toSize = toSize;
        }

        @Override
        protected void interpolate(double frac) {
            if (!Double.isNaN(startSize)) {
                node.setFont(Font.font(node.getFont().getFamily(), startSize + frac * (toSize - startSize)));
            }
        }

    }

    private static class TweetWord {

        Bounds bounds;
        String text;

        public TweetWord(Bounds bounds, String text) {
            this.bounds = bounds;
            this.text = text;
        }

        @Override
        public String toString() {
            return "TweetWord{" + "text=" + text + ", bounds=" + bounds + '}';
        }

    }

    private static class TweetWordNode {

        private TweetWord tweetWord;
        private Text textNode;

        public TweetWordNode(TweetWord tweetWord, Text textNode) {
            this.tweetWord = tweetWord;
            this.textNode = textNode;
        }

    }

}
