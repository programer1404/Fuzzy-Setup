/*
 jfuzzylite (TM), a fuzzy logic control library in Java.
 Copyright (C) 2010-2017 FuzzyLite Limited. All rights reserved.
 Author: Juan Rada-Vilela, Ph.D. <jcrada@fuzzylite.com>

 This file is part of jfuzzylite.

 jfuzzylite is free software: you can redistribute it and/or modify it under
 the terms of the FuzzyLite License included with the software.

 You should have received a copy of the FuzzyLite License along with
 jfuzzylite. If not, see <http://www.fuzzylite.com/license/>.

 jfuzzylite is a trademark of FuzzyLite Limited.
 fuzzylite (R) is a registered trademark of FuzzyLite Limited.
 */
package fuzzylite.term;

import fuzzylite.Op;

import java.util.Iterator;
import java.util.List;

/**
 The Binary class is an edge Term that represents the binary membership
 function.

 @image html binary.svg

 @author Juan Rada-Vilela, Ph.D.
 @see Term
 @see fuzzylite.variable.Variable
 @since 6.0
 */
public class Binary extends Term {

    /**
     Direction is an enumerator that indicates the direction of the edge.
     */
    public enum Direction {
        /**
         `(_|)` increases to the right (infinity)
         */
        Positive,
        /**
         `(--)` direction is NaN
         */
        Undefined,
        /**
         `(|_)` increases to the left (-infinity)
         */
        Negative
    }
    private double start, direction;

    public Binary() {
        this("");
    }

    public Binary(String name) {
        this(name, Double.NaN, Double.NaN);
    }

    public Binary(String name, double start, double direction) {
        this(name, start, direction, 1.0);
    }

    public Binary(String name, double start, double direction, double height) {
        super(name, height);
        this.start = start;
        this.direction = direction;
    }

    /**
     Returns the parameters of the term

     @return `"start direction [height]"`
     */
    @Override
    public String parameters() {
        return Op.join(" ", start, direction)
                + (!Op.isEq(height, 1.0) ? " " + Op.str(height) : "");
    }

    /**
     Configures the term with the parameters

     @param parameters as `"start direction [height]"`
     */
    @Override
    public void configure(String parameters) {
        if (parameters.isEmpty()) {
            return;
        }
        List<String> values = Op.split(parameters, " ");
        int required = 2;
        if (values.size() < required) {
            throw new RuntimeException(String.format(
                    "[configuration error] term <%s> requires <%d> parameters",
                    this.getClass().getSimpleName(), required));
        }
        Iterator<String> it = values.iterator();
        setStart(Op.toDouble(it.next()));
        setDirection(Op.toDouble(it.next()));
        if (values.size() > required) {
            setHeight(Op.toDouble(it.next()));
        }
    }

    /**
     Computes the membership function evaluated at `x`

     @param x
     @return `\begin{cases} 1h & \mbox{if $ \left(s < d \vedge x \in [s, d)\right) \wedge
     \left( s > d \vedge x \in (d, s] \right) $} \cr 0h & \mbox{otherwise}
     \end{cases}`

     where `h` is the height of the Term,
     `s` is the start of the Binary edge,
     `d` is the direction of the Binary edge.
     */
    @Override
    public double membership(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (direction > start
                && Op.isGE(x, start)) {
            return height * 1.0;
        }
        if (direction < start
                && Op.isLE(x, start)) {
            return height * 1.0;
        }
        return height * 0.0;
    }

    /**
     Gets the start of the binary edge

     @return the start of the binary edge
     */
    public double getStart() {
        return start;
    }

    /**
     Sets the start of the binary edge

     @param start is the start of the binary edge
     */
    public void setStart(double start) {
        this.start = start;
    }

    /**
     Gets the direction of the binary edge

     @return the direction of the binary edge
     */
    public double getDirection() {
        return direction;
    }

    /**
     Sets the direction of the binary edge.

     `\begin{cases} \text{Positive} & \mbox{if $ d > s $}\cr \text{Negative} &
     \mbox{if $ d < s $}\cr \mbox{\tt NaN} & \mbox{otherwise} \end{cases}`

     where `d` is the given direction, and `s` is the start of the
     Binary edge

     @param direction is the direction of the binary edge
     */
    public void setDirection(double direction) {
        this.direction = direction;
    }

    /**
     Gets the Direction of the binary edge as an enumerator

     @return the Direction of the binary edge as an enumerator
     */
    public Direction direction() {
        if (direction > start) {
            return Direction.Positive;
        }
        if (direction < start) {
            return Direction.Negative;
        }
        return Direction.Undefined;
    }

    @Override
    public Binary clone() throws CloneNotSupportedException {
        return (Binary) super.clone();
    }

}
