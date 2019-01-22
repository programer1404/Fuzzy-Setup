package ir.sinapp.fuzzy;

import fuzzylite.Engine;
import fuzzylite.defuzzifier.Bisector;
import fuzzylite.norm.s.Maximum;
import fuzzylite.norm.t.AlgebraicProduct;
import fuzzylite.rule.Rule;
import fuzzylite.rule.RuleBlock;
import fuzzylite.term.*;
import fuzzylite.term.Gaussian;
import fuzzylite.variable.InputVariable;
import fuzzylite.variable.OutputVariable;

import java.text.DecimalFormat;

public class Fuzzy {

    private Engine engine;
    private InputVariable inRed;
    private InputVariable inGreen;
    private InputVariable inBlue;
    private OutputVariable outLum;
    private OutputVariable outColor;

    private double redCol, greenCol, blueCol, lux;
    private String lblLux;
    private String lblDegree;
    private String lblColor;


    public Fuzzy(int m) {
        setup(m);
    }

    public void setup(int m) {

        // تعریف یک سیستم فازی
        engine = new Engine();
        engine.setName("RGB");

        inRed = new InputVariable();
        inRed.setEnabled(true);
        inRed.setName("red");
        inRed.setRange(0.000, 15.000);
        double standardDeviation = 2.25;

        if (m == 0) {
            inRed.addTerm(new Triangle("low", 0.0, 0.0, 5.0));
            inRed.addTerm(new Triangle("mid", 2.5, 7.5, 12.5));
            inRed.addTerm(new Triangle("high", 10.0, 15.0, 15.0));
        } else if (m == 1) {
            inRed.addTerm(new Gaussian("low", 0.0, standardDeviation));
            inRed.addTerm(new Gaussian("mid", 7.5, standardDeviation));
            inRed.addTerm(new Gaussian("high", 15.0, standardDeviation));
        }
        engine.addInputVariable(inRed);

        inGreen = new InputVariable();
        inGreen.setEnabled(true);
        inGreen.setName("green");
        inGreen.setRange(0.000, 15.000);
        if (m == 0) {
            inGreen.addTerm(new Triangle("low", 0.0, 0.0, 5.0));
            inGreen.addTerm(new Triangle("mid", 2.5, 7.5, 12.5));
            inGreen.addTerm(new Triangle("high", 10.0, 15.0, 15.0));
        } else if (m == 1) {
            inGreen.addTerm(new Gaussian("low", 0.0, standardDeviation));
            inGreen.addTerm(new Gaussian("mid", 7.5, standardDeviation));
            inGreen.addTerm(new Gaussian("high", 15.0, standardDeviation));
        }

        engine.addInputVariable(inGreen);

        inBlue = new InputVariable();
        inBlue.setEnabled(true);
        inBlue.setName("blue");
        inBlue.setRange(0.000, 15.000);
        if (m == 0) {
            inBlue.addTerm(new Triangle("low", 0.0, 0.0, 5.0));
            inBlue.addTerm(new Triangle("mid", 2.5, 7.5, 12.5));
            inBlue.addTerm(new Triangle("high", 10.0, 15.0, 15.0));
        } else if (m == 1) {
            inBlue.addTerm(new Gaussian("low", 0.0, standardDeviation));
            inBlue.addTerm(new Gaussian("mid", 7.5, standardDeviation));
            inBlue.addTerm(new Gaussian("high", 15.0, standardDeviation));
        }
        engine.addInputVariable(inBlue);

//        outLum = new OutputVariable();
//        outLum.setEnabled(true);
//        outLum.setName("lum");
//        outLum.setRange(0.000, 100.000);
//        outLum.fuzzyOutput().setAggregation(new Maximum());
//        outLum.setDefuzzifier(new WeightedAverage());
//        outLum.setDefaultValue(0.000);
//        outLum.setLockPreviousValue(false);
//        outLum.setLockValueInRange(true);
//        outLum.addTerm(new Ramp("max", 0.000, 100.000));
//        engine.addOutputVariable(outLum);

        outColor = new OutputVariable();
        outColor.setEnabled(true);
        outColor.setName("ledColor");
        outColor.setRange(0, 4095);
        outColor.fuzzyOutput().setAggregation(new Maximum());
        outColor.setDefuzzifier(new Bisector(250000));
        outColor.setDefaultValue(Double.NaN);
        outColor.setLockValueInRange(false);
        outColor.setLockPreviousValue(false);

        outColor.addTerm(new Triangle("black", 0, 0, 8));
        outColor.addTerm(new Triangle("blue", 7, 15, 119));
        outColor.addTerm(new Triangle("ocean", 112, 127, 198));
        outColor.addTerm(new Triangle("green", 168, 240, 244));
        outColor.addTerm(new Triangle("turquoise", 243, 247, 251));
        outColor.addTerm(new Triangle("cyan", 250, 255, 1225));
        outColor.addTerm(new Triangle("purple", 837, 1807, 1872));
        outColor.addTerm(new Triangle("grey", 1846, 1911, 1987));
        outColor.addTerm(new Triangle("lime", 1956, 2032, 3162));
        outColor.addTerm(new Triangle("red", 2710, 3840, 3844));
        outColor.addTerm(new Triangle("raspberry", 3843, 3847, 3851));
        outColor.addTerm(new Triangle("magenta", 3850, 3855, 3916));
        outColor.addTerm(new Triangle("orange", 3891, 3952, 4032));
        outColor.addTerm(new Triangle("yellow", 4000, 4080, 4089));
        outColor.addTerm(new Triangle("white", 4085, 4095, 4095));
        engine.addOutputVariable(outColor);

        RuleBlock ruleBlock = new RuleBlock();
        ruleBlock.setEnabled(true);
        ruleBlock.setName("");
        ruleBlock.setImplication(new AlgebraicProduct());
        ruleBlock.setConjunction(new AlgebraicProduct());
        ruleBlock.setDisjunction(null);
        ruleBlock.addRule(Rule.parse("if red is high and green is low and blue is low then ledColor is red", engine));
        ruleBlock.addRule(Rule.parse("if red is high and green is mid and blue is low then ledColor is orange", engine));
        ruleBlock.addRule(Rule.parse("if red is high and green is high and blue is low then ledColor is yellow", engine));
        ruleBlock.addRule(Rule.parse("if red is mid and green is high and blue is low then ledColor is lime", engine));
        ruleBlock.addRule(Rule.parse("if red is low and green is high and blue is low then ledColor is green", engine));
        ruleBlock.addRule(Rule.parse("if red is low and green is high and blue is mid then ledColor is turquoise", engine));
        ruleBlock.addRule(Rule.parse("if red is low and green is high and blue is high then ledColor is cyan", engine));
        ruleBlock.addRule(Rule.parse("if red is low and green is mid and blue is high then ledColor is ocean", engine));
        ruleBlock.addRule(Rule.parse("if red is low and green is low and blue is high then ledColor is blue", engine));
        ruleBlock.addRule(Rule.parse("if red is mid and green is low and blue is high then ledColor is purple", engine));
        ruleBlock.addRule(Rule.parse("if red is high and green is low and blue is high then ledColor is magenta", engine));
        ruleBlock.addRule(Rule.parse("if red is high and green is low and blue is mid then ledColor is raspberry", engine));
        ruleBlock.addRule(Rule.parse("if red is high and green is high and blue is high then ledColor is white", engine));
        ruleBlock.addRule(Rule.parse("if red is mid and green is mid and blue is mid then ledColor is grey", engine));
        ruleBlock.addRule(Rule.parse("if red is low and green is low and blue is low then ledColor is black", engine));
        ruleBlock.addRule(Rule.parse("if red is high then lum is max", engine));
        ruleBlock.addRule(Rule.parse("if green is high then lum is max", engine));
        ruleBlock.addRule(Rule.parse("if blue is high then lum is max", engine));
        engine.addRuleBlock(ruleBlock);
    }

    private void fuzzyDoIt() {
        StringBuilder status = new StringBuilder();
        if (!engine.isReady(status)) {
            throw new RuntimeException("Engine not ready. "
                    + "The following errors were encountered:\n" + status.toString());
        }
        inBlue.setValue(blueCol);
        inGreen.setValue(greenCol);
        inRed.setValue(redCol);

        DecimalFormat df1 = new DecimalFormat("#0.0");
        DecimalFormat df2 = new DecimalFormat("#0.00");

        engine.process();

//        lblLux = (df1.format(outLum.getValue()));
        lux = outLum.getValue();
        lblDegree = (df2.format(outColor.getValue()));

//        lblColor = outColor.fuzzyOutputValue();

        checkName(outColor.getValue(), 0, 15, "black", "blue");
        checkName(outColor.getValue(), 15, 127, "blue", "ocean");
        checkName(outColor.getValue(), 127, 240, "ocean", "green");
        checkName(outColor.getValue(), 240, 247, "green", "Turquoise");
        checkName(outColor.getValue(), 247, 255, "Turquoise", "cyan");
        checkName(outColor.getValue(), 255, 1807, "cyan", "purple");
        checkName(outColor.getValue(), 1807, 1911, "purple", "grey");
        checkName(outColor.getValue(), 1911, 2032, "grey", "lime");
        checkName(outColor.getValue(), 2032, 3840, "lime", "red");
        checkName(outColor.getValue(), 3840, 3847, "red", "raspberry");
        checkName(outColor.getValue(), 3847, 3855, "raspberry", "magenta");
        checkName(outColor.getValue(), 3855, 3952, "magenta", "orange");
        checkName(outColor.getValue(), 3952, 4080, "orange", "yellow");
        checkName(outColor.getValue(), 4080, 4095, "yellow", "white");
    }

    private void checkName(double deg, double l_val, double r_val, String l_name, String r_name) {
        DecimalFormat df = new DecimalFormat("#0.000");

        if (deg > l_val && deg < r_val) {
            if (outColor.fuzzyOutput().activationDegree(outColor.getTerm(r_name)) > outColor.fuzzyOutput().activationDegree(outColor.getTerm(l_name))) {
                lblColor = r_name + " " + df.format(outColor.fuzzyOutput().activationDegree(outColor.getTerm(r_name)));
            } else {
                lblColor = l_name + " " + df.format(outColor.fuzzyOutput().activationDegree(outColor.getTerm(l_name)));
            }
        }
    }

    public String getLblLux() {
        return lblLux;
    }

    public String getLblDegree() {
        return lblDegree;
    }


    public String getLblColor() {
        return lblColor;
    }

    public void setColor(double currentRed, double currentGreen, double currentBlue) {
        redCol = currentRed;
        greenCol = currentGreen;
        blueCol = currentBlue;
        fuzzyDoIt();
    }
}

/*
0 0 0    0
0 0 1    1
0 0 2    2
0 1 0    4
0 1 1    5
0 1 2    6
0 2 0    7
0 2 1    8
0 2 2    9
1 0 0    10
1 0 1    11
1 0 2    12
1 1 0    13
1 1 1    14
1 1 2    15
1 2 0    16
1 2 1    17
1 2 2    18
2 0 0    19
2 0 1    20
2 0 2    21
2 1 0    22
2 1 1    23
2 1 2    24
2 2 0    25
2 2 1    26
2 2 2    27


back         0 0 0       0         000        0
             0 0 1       1         007        7
blue         0 0 2       2         00F        15
             0 1 0       3         070        112
             0 1 1       4         077        119
ocean        0 1 2       5         07F        127
green        0 2 0       6         0F0        240
turquoise    0 2 1       7         0F7        247
cyan         0 2 2       8         0FF        255
             1 0 0       9         700        1792
             1 0 1       10        707        1799
purple       1 0 2       11        70F        1807
             1 1 0       12        770        1904
grey         1 1 1       13        777        1911
             1 1 2       14        77F        1919
lime         1 2 0       15        7F0        2032
             1 2 1       16        7F7        2039
             1 2 2       17        7FF        2047
red          2 0 0       18        F00        3840
rasp         2 0 1       19        F07        3847
magenta      2 0 2       20        F0F        3855
orange       2 1 0       21        F70        3952
             2 1 1       22        F77        3959
             2 1 2       23        F7F        3967
yellow       2 2 0       24        FF0        4080
             2 2 1       25        FF7        4087
white        2 2 2       26        FFF        4095






























*
*
* */