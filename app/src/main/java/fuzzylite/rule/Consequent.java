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
package fuzzylite.rule;

import fuzzylite.Engine;
import fuzzylite.FuzzyLite;
import fuzzylite.factory.FactoryManager;
import fuzzylite.factory.HedgeFactory;
import fuzzylite.hedge.Hedge;
import fuzzylite.norm.TNorm;
import fuzzylite.term.Activated;
import fuzzylite.variable.OutputVariable;

import java.util.*;
import java.util.logging.Level;

/**
 The Consequent class is a proposition set that represents and evaluates the
 consequent of a Rule.. The structure of a rule is: `if (antecedent) then
 (consequent)`. The structure of the consequent of a rule is:

 `then variable is [hedge]* term [and variable is [hedge]* term]* [with w]?`

 where `*`-marked elements may appear zero or more times, elements in brackets
 are optional, elements in parentheses are compulsory, and `?`-marked elements
 may appear once or not at all.

 @author Juan Rada-Vilela, Ph.D.
 @see Antecedent
 @see Rule
 @since 4.0
 */
public class Consequent {

    private String text;
    private List<Proposition> conclusions;

    public Consequent() {
        this.text = "";
        this.conclusions = new ArrayList<Proposition>();
    }

    /**
     Gets the text of the consequent

     @return the text of the consequent
     */
    public String getText() {
        return text;
    }

    /**
     Sets the text of the consequent

     @param text is the text of the consequent
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     Returns the list of propositions that represent the Consequent of a Rule

     @return the list of propositions that represent the Consequent of a Rule
     */
    public List<Proposition> getConclusions() {
        return conclusions;
    }

    /**
     Sets the list of propositions that represent the Consequent of a Rule

     @param conclusions is the list of propositions that represent the
     Consequent of a Rule
     */
    public void setConclusions(List<Proposition> conclusions) {
        this.conclusions = conclusions;
    }

    /**
     Modifies the proposition set according to the activation degree (computed
     in the Antecedent of the Rule) and the implication operator (given in the
     RuleBlock)

     @param activationDegree is the activation degree computed in the Antecedent
     of the Rule
     @param implication is the implication operator configured in the RuleBlock
     */
    public void modify(double activationDegree, TNorm implication) {
        if (!isLoaded()) {
            throw new RuntimeException(String.format(
                    "[consequent error] consequent <%s> is not loaded", text));
        }
        for (Proposition proposition : conclusions) {
            if (proposition.getVariable().isEnabled()) {
                if (!proposition.getHedges().isEmpty()) {
                    final int lastIndex = proposition.getHedges().size();
                    ListIterator<Hedge> rit = proposition.getHedges().listIterator(lastIndex);
                    while (rit.hasPrevious()) {
                        activationDegree = rit.previous().hedge(activationDegree);
                    }
                }
                Activated term = new Activated(proposition.getTerm(), activationDegree, implication);
                ((OutputVariable) proposition.getVariable()).fuzzyOutput().getTerms().add(term);
                if (FuzzyLite.isDebugging()) {
                    FuzzyLite.logger().log(Level.FINE, "Aggregating {0}", term.toString());
                }
            }
        }
    }

    /**
     Indicates whether the consequent is loaded

     @return whether the consequent is loaded
     */
    public boolean isLoaded() {
        return !conclusions.isEmpty();
    }

    /**
     Unloads the consequent
     */
    public void unload() {
        conclusions.clear();
    }

    /**
     Loads the consequent with text given from Consequent::getText() and uses
     the engine to identify and retrieve references to the input variables and
     output variables as required

     @param engine is the engine from which the rules are part of
     */
    public void load(Engine engine) {
        load(getText(), engine);
    }

    /**
     Loads the consequent with the given text and uses the engine to identify
     and retrieve references to the input variables and output variables as
     required

     @param consequent is the consequent of the rule in text
     @param engine is the engine from which the rules are part of
     */
    public void load(String consequent, Engine engine) {
        unload();
        setText(consequent);
        if (consequent.trim().isEmpty()) {
            throw new RuntimeException("[syntax error] consequent is empty");
        }

        /*
         Extracts the list of propositions from the consequent
         The rules are:
         1) After a variable comes 'is' or '=',
         2) After 'is' comes a hedge or a term
         3) After a hedge comes a hedge or a term
         4) After a term comes operators 'and' or 'with'
         5) After operator 'and' comes a variable
         6) After operator 'with' comes a float
         */
        final byte S_VARIABLE = 1, S_IS = 2, S_HEDGE = 4, S_TERM = 8, S_AND = 16, S_WITH = 32;
        byte state = S_VARIABLE;

        Proposition proposition = null;

        StringTokenizer tokenizer = new StringTokenizer(consequent);
        String token = "";
        try {
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();

                if ((state & S_VARIABLE) != 0) {
                    if (engine.hasOutputVariable(token)) {
                        proposition = new Proposition();
                        proposition.setVariable(engine.getOutputVariable(token));
                        getConclusions().add(proposition);
                        state = S_IS;
                        continue;
                    }
                }

                if ((state & S_IS) != 0) {
                    if (Rule.FL_IS.equals(token)) {
                        state = S_HEDGE | S_TERM;
                        continue;
                    }
                }

                if ((state & S_HEDGE) != 0) {
                    HedgeFactory hedgeFactory = FactoryManager.instance().hedge();
                    if (hedgeFactory.hasConstructor(token)) {
                        Hedge hedge = hedgeFactory.constructObject(token);
                        proposition.getHedges().add(hedge);
                        state = S_HEDGE | S_TERM;
                        continue;
                    }
                }

                if ((state & S_TERM) != 0) {
                    if (proposition.getVariable().hasTerm(token)) {
                        proposition.setTerm(proposition.getVariable().getTerm(token));
                        state = S_AND | S_WITH;
                        continue;
                    }
                }

                if ((state & S_AND) != 0) {
                    if (Rule.FL_AND.equals(token)) {
                        state = S_VARIABLE;
                        continue;
                    }
                }

                //if reached this point, there was an error:
                if ((state & S_VARIABLE) != 0) {
                    throw new RuntimeException(String.format(
                            "[syntax error] consequent expected output variable, but found <%s>",
                            token));
                }
                if ((state & S_IS) != 0) {
                    throw new RuntimeException(String.format(
                            "[syntax error] consequent expected keyword <%s>, but found <%s>",
                            Rule.FL_IS, token));
                }
                if ((state & S_HEDGE) != 0 || (state & S_TERM) != 0) {
                    throw new RuntimeException(String.format(
                            "[syntax error] consequent expected hedge or term, but found <%s>",
                            token));
                }
                if ((state & S_AND) != 0 || ((state & S_WITH) != 0)) {
                    throw new RuntimeException(String.format(
                            "[syntax error] consequent expected operator <%s> or keyword <%s>, sbut found <%s>",
                            Rule.FL_AND, Rule.FL_WITH, token));
                }
                throw new RuntimeException(String.format(
                        "[syntax error] unexpected token <%s>", token));
            }

            if (!((state & S_AND) != 0 || ((state & S_WITH) != 0))) { //only acceptable final state
                if ((state & S_VARIABLE) != 0) {
                    throw new RuntimeException(String.format(
                            "[syntax error] consequent expected output variable after <%s>", token));
                }
                if ((state & S_IS) != 0) {
                    throw new RuntimeException(String.format(
                            "[syntax error] consequent expected keyword <%s> after <%s>", Rule.FL_IS, token));
                }

                if ((state & S_HEDGE) != 0 || (state & S_TERM) != 0) {
                    throw new RuntimeException(String.format(
                            "[syntax error] consequent expected hedge or term after <%s>", token));
                }
            }
        } catch (RuntimeException ex) {
            unload();
            throw ex;
        }
    }

    /**
     Returns a string representation of the Consequent

     @return a string representation of the Consequent
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Proposition> it = this.conclusions.iterator();
             it.hasNext();) {
            sb.append(it.next().toString());
            if (it.hasNext()) {
                sb.append(String.format(" %s ", Rule.FL_AND));
            }
        }
        return sb.toString();
    }

}
