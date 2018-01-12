package net.acesinc.data.json.generator.types;

import java.text.ParseException;
import java.util.Date;

/*
    First Argument is a Date String
    Second Argument is the Offset (same as NowType arguments)
 */
public class CalculatedDateType extends BaseDateType {
    public static final String TYPE_NAME = "calcDate";
    public static final String TYPE_DISPLAY_NAME = "Calculated Date";

    private long timeToAdd = 0;
    private Date baseDate;


    @Override
    public Date getNextRandomValue() {
        return getNextDate();
    }

    @Override
    public String getName() {
        return TYPE_NAME;
    }

    @Override
    public void setLaunchArguments(String[] launchArguments) {
        super.setLaunchArguments(launchArguments);
        if (launchArguments.length == 2) {
            try {
                baseDate = INPUT_DATE_FORMAT.get().parse(stripQuotes(launchArguments[0]));
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
            timeToAdd = NowBaseType.getTimeOffset(launchArguments[1]);
        } else {
            throw new IllegalArgumentException("Invalid number of arguments received.  Expected 2, actual " + (launchArguments.length));
        }
    }

    public Date getNextDate() {
        if (timeToAdd == 0) {
            return baseDate;
        } else {
            return new Date(baseDate.getTime() + timeToAdd);
        }
    }

}
