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
package fuzzylite.factory;

import fuzzylite.hedge.*;

/**
 The HedgeFactory class is a ConstructionFactory of Hedge%s.

 @author Juan Rada-Vilela, Ph.D.
 @see Hedge
 @see ConstructionFactory
 @see FactoryManager
 @since 4.0
 */
public class HedgeFactory extends ConstructionFactory<Hedge> {

    public HedgeFactory() {
        register("", null);
        register(new Any().getName(), Any.class);
        register(new Extremely().getName(), Extremely.class);
        register(new Not().getName(), Not.class);
        register(new Seldom().getName(), Seldom.class);
        register(new Somewhat().getName(), Somewhat.class);
        register(new Very().getName(), Very.class);
    }

    @Override
    public HedgeFactory clone() throws CloneNotSupportedException {
        return (HedgeFactory) super.clone();
    }
}
