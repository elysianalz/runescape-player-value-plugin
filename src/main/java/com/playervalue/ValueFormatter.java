package com.playervalue;

import java.awt.*;
import static com.playervalue.ValueGrade.*;

public class ValueFormatter {

    long value;
    Color color;
    ValueGrade grade;
    String formattedString;

    final String K = "K";
    final String M = "M";
    final String gp = "gp";
    final long TEN_MILLION = 10000000;
    final long HUNDRED_THOUSAND = 100000;
    final long DIVIDE_MILLION = 1000000;
    final long DIVIDE_THOUSAND = 1000;
    final long DIVIDE_ONE = 1;

    ValueFormatter(long value)
    {
        this.value = value;
        gradeValue();
        formatValue();
    }

    public void gradeValue()
    {
        if(value >= TEN_MILLION)
        {
           this.grade = HIGH;
        }
        else if(value >= HUNDRED_THOUSAND)
        {
           this.grade = MED;
        }
        else
        {
           this.grade = LOW;
        }
    }
    
    public void formatValue()
    {
        long f;
        String text;
        Color textColor;

        switch (this.grade)
        {
            case HIGH:
                changeValue(DIVIDE_MILLION, Color.GREEN, M);
                break;

            case MED:
                changeValue(DIVIDE_THOUSAND, Color.WHITE, K);
                break;

            case LOW:
                changeValue(DIVIDE_ONE, Color.YELLOW, gp);
                break;
        }
    }

    public void changeValue(long div, Color color, String grade)
    {
        long f = value / div;
        this.formattedString = Long.toString(f) + grade;
        this.color = color;
    }

}
