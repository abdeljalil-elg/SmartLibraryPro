package com.smartlibrary.util;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;

public final class Animations {
    private Animations() {
    }

    public static void fadeSlideIn(Node node, boolean enabled) {
        if (!enabled) {
            node.setOpacity(1);
            return;
        }
        node.setOpacity(0);
        node.setTranslateY(12);
        FadeTransition fade = new FadeTransition(Duration.millis(260), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition translate = new TranslateTransition(Duration.millis(260), node);
        translate.setFromY(12);
        translate.setToY(0);
        fade.play();
        translate.play();
    }

    public static void pulse(Node node, boolean enabled) {
        if (!enabled) {
            return;
        }
        ScaleTransition scale = new ScaleTransition(Duration.millis(180), node);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(1.03);
        scale.setToY(1.03);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    public static void animateCounter(Label label, int target, boolean enabled) {
        if (!enabled) {
            label.setText(Integer.toString(target));
            return;
        }
        IntegerProperty value = new SimpleIntegerProperty(0);
        value.addListener((observable, oldValue, newValue) -> label.setText(Integer.toString(newValue.intValue())));
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(650), new KeyValue(value, target)));
        timeline.play();
    }
}
