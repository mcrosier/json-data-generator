/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import org.junit.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author andrewserff
 */
public class CalculatedDateTypeTypeTest {

    private String inputDate = "2015/05/01T00:00:00";
    private String inputDatePlus6h = "2015/05/01T06:00:00";
    private String inputDatePlus6m = "2015/05/01T00:06:00";
    private String inputDateMinus6h = "2015/04/30T18:00:00";
    private String inputDateMinus6m = "2015/04/30T23:54:00";

    private String offset6h = "6_h";
    private String offset6m = "6_m";
    private String offsetminus6h = "-6_h";
    private String offsetminus6m = "-6_m";

    private Date datePlus6h;
    private Date datePlus6m;
    private Date dateMinus6h;
    private Date dateMinus6m;

    private Date date;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");
    private SimpleDateFormat sdfMs = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss.SSS");

    public CalculatedDateTypeTypeTest() {
    }


    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        try {
            date = sdf.parse(inputDate);
            datePlus6h = sdf.parse(inputDatePlus6h);
            datePlus6m = sdf.parse(inputDatePlus6m);
            dateMinus6h = sdf.parse(inputDateMinus6h);
            dateMinus6m = sdf.parse(inputDateMinus6m);
        } catch (ParseException pe) {
        }
    }

    @After
    public void tearDown() {
    }

    @Test(expected = IllegalArgumentException.class)
    public void noArgumentsTest()  {
        String[] launchArguments = {};
        Date result = performCalc(launchArguments);
    }

    @Test(expected = IllegalArgumentException.class)
    public void oneArgumentTest() {
        String[] launchArguments = {inputDate};
        Date result = performCalc(launchArguments);
        assertTrue (result.equals(date));
    }

    @Test
    public void testGetCalculatedBasedOn6hoffset() {
        String[] launchArguments = {inputDate, offset6h};
        Date result = performCalc(launchArguments);
        assertTrue (result.equals(datePlus6h));
    }


    @Test
    public void testGetCalculatedBasedOnNegative6hoffset() {
        String[] launchArguments = {inputDate, offsetminus6h};
        Date result = performCalc(launchArguments);
        assertTrue (result.equals(dateMinus6h));
    }

    @Test
    public void testGetCalculatedBasedOnNegative6moffset() {
        String[] launchArguments = {inputDate, offsetminus6m};
        Date result = performCalc(launchArguments);
        assertTrue (result.equals(dateMinus6m));
    }

    @Test
    public void testGetCalculatedBasedOn6moffset() {
        String[] launchArguments = {inputDate, offsetminus6h};
        Date result = performCalc(launchArguments);
        assertTrue (result.equals(dateMinus6h));
    }


    private Date performCalc(String[] args) {
        CalculatedDateType instance = new CalculatedDateType();
        instance.setLaunchArguments(args);
        return instance.getNextDate();
    }

    /**
     * Test of getName method, of class CalcDateType.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        CalculatedDateType instance = new CalculatedDateType();
        String expResult = "calcDate";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

}
