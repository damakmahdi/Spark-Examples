/***********************************************************************************************************************
 Copyright (c) Damak Mahdi.
 Github.com/damakmahdi
 damakmahdi2012@gmail.com
 linkedin.com/in/mahdi-damak-400a3b14a/
 **********************************************************************************************************************/

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import com.workday.insights.timeseries.timeseriesutil.ForecastUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Dataset$;
import org.apache.spark.sql.Row;
import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ArimaTest {

    private final DecimalFormat df = new DecimalFormat("##.#####");


    //////////////////////////////////////////////////////////////


    public void forecast(double[] ds) {
        comon("test", ds, 0.1, 1, 1, 3, 1, 0, 1,
                42);
        double lastActualData = forecastSinglePointLogic("test",
                ds, 0, 0, 3, 0, 0, 1, 42);
        comonassert(ds, lastActualData, 0.15);
    }



    private double[] comon(final String name, final double[] dataSet,
                           final double forecastRatio,
                           int p, int d, int q, int P, int D, int Q, int m) {

        //Compute forecast and training size
        final int forecastSize = (int) (dataSet.length * forecastRatio);
        final int trainSize = dataSet.length - forecastSize;
        //Separate data set into training data and test data
        final double[] trainingData = new double[trainSize];
        System.arraycopy(dataSet, 0, trainingData, 0, trainSize);
        final double[] trueForecastData = new double[forecastSize];
        System.arraycopy(dataSet, trainSize, trueForecastData, 0, forecastSize);

        return commonTestSimpleForecast(name + " (common test)", trainingData, trueForecastData,
                forecastSize, p, d, q,
                P, D, Q, m);
    }


    private void comonassert(double[] l, double actualValue, double delta) {
        double lastTrueValue = l[l.length - 1];

        Assert.assertEquals(lastTrueValue, actualValue, delta);
    }




    private double[] commonTestSimpleForecast(final String name, final double[] trainingData,
                                              final double[] trueForecastData, final int forecastSize,
                                              int p, int d, int q, int P, int D, int Q, int m) {

        //Make forecast

        final ForecastResult forecastResult = Arima
                .forecast_arima(trainingData, forecastSize, new ArimaParams(p, d, q, P, D, Q, m));
        //Get forecast data and confidence intervals
        final double[] forecast = forecastResult.getForecast();
        final double[] upper = forecastResult.getForecastUpperConf();
        final double[] lower = forecastResult.getForecastLowerConf();
        //Building output
        final StringBuilder sb = new StringBuilder();
        sb.append(name).append("  ****************************************************\n");
        sb.append("Input Params { ")
                .append("p: ").append(p)
                .append(", d: ").append(d)
                .append(", q: ").append(q)
                .append(", P: ").append(P)
                .append(", D: ").append(D)
                .append(", Q: ").append(Q)
                .append(", m: ").append(m)
                .append(" }");
        sb.append("\n\nFitted Model RMSE: ").append(dbl2str(forecastResult.getRMSE()));
        sb.append("\n\n      TRUE DATA    |     LOWER BOUND          FORECAST       UPPER BOUND\n");

        for (int i = 0; i < forecast.length; ++i) {
            sb.append(dbl2str(trueForecastData[i])).append("    | ")
                    .append(dbl2str(lower[i])).append("   ").append(dbl2str(forecast[i]))
                    .append("   ").append(dbl2str(upper[i]))
                    .append("\n");
        }

        sb.append("\n");

        //Compute RMSE against true forecast data
        double temp = 0.0;
        for (int i = 0; i < forecast.length; ++i) {
            temp += Math.pow(forecast[i] - trueForecastData[i], 2);
        }
        final double rmse = Math.pow(temp / forecast.length, 0.5);
        sb.append("RMSE = ").append(dbl2str(rmse)).append("\n\n");
        System.out.println(sb.toString());
        return forecast;
    }
    //////////////////////////////////////////////////////////////////////

    private double[] commonTestLogic(final String name, final double[] dataSet,
                                     final double forecastRatio,
                                     int p, int d, int q, int P, int D, int Q, int m) {

        //Compute forecast and training size
        final int forecastSize = (int) (dataSet.length * forecastRatio);
        final int trainSize = dataSet.length - forecastSize;
        //Separate data set into training data and test data
        final double[] trainingData = new double[trainSize];
        System.arraycopy(dataSet, 0, trainingData, 0, trainSize);
        final double[] trueForecastData = new double[forecastSize];
        System.arraycopy(dataSet, trainSize, trueForecastData, 0, forecastSize);

        return commonTestSimpleForecast(name + " (common test)", trainingData, trueForecastData,
                forecastSize, p, d, q,
                P, D, Q, m);
    }

    private double[] comonTest(final String name, final ArrayList<Double> dataSet,
                                     final double forecastRatio,
                                     int p, int d, int q, int P, int D, int Q, int m) {

        //Compute forecast and training size
        final int forecastSize = (int) (dataSet.size() * forecastRatio);
        final int trainSize = (int)dataSet.size() - forecastSize;
        //Separate data set into training data and test data
        final double[] trainingData = new double[trainSize];
        System.arraycopy(dataSet, 0, trainingData, 0, trainSize);
        final double[] trueForecastData = new double[forecastSize];
        System.arraycopy(dataSet, trainSize, trueForecastData, 0, forecastSize);

        return commonTestSimpleForecast(name + " (common test)", trainingData, trueForecastData,
                forecastSize, p, d, q,
                P, D, Q, m);
    }

    private double forecastSinglePointLogic(final String name, final double[] dataSet,
                                            int p, int d, int q, int P, int D, int Q, int m) {
        //Compute forecast and training size
        final int forecastSize = 1;
        final int trainSize = dataSet.length - forecastSize;
        //Separate data set into training data and test data
        final double[] trainingData = new double[trainSize];
        System.arraycopy(dataSet, 0, trainingData, 0, trainSize);
        final double[] trueForecastData = new double[forecastSize];
        System.arraycopy(dataSet, trainSize, trueForecastData, 0, forecastSize);

        return commonTestSimpleForecast(name + " (forecast single point)", trainingData,
                trueForecastData, forecastSize, p, d, q,
                P, D, Q, m)[0];
    }

    private String dbl2str(final double value) {
        String rep = df.format(value);
        String padded = String.format("%15s", rep);
        return padded;
    }



    private double commonTestCalculateRMSE(final String name, final double[] trainingData,
                                           final double[] trueForecastData, final int forecastSize,
                                           int p, int d, int q, int P, int D, int Q, int m) {

        //Make forecast

        final ForecastResult forecastResult = Arima
                .forecast_arima(trainingData, forecastSize, new ArimaParams(p, d, q, P, D, Q, m));
        //Get forecast data and confidence intervals
        final double[] forecast = forecastResult.getForecast();
        final double[] upper = forecastResult.getForecastUpperConf();
        final double[] lower = forecastResult.getForecastLowerConf();
        //Building output
        final StringBuilder sb = new StringBuilder();
        sb.append(name).append("  ****************************************************\n");
        sb.append("Input Params { ")
                .append("p: ").append(p)
                .append(", d: ").append(d)
                .append(", q: ").append(q)
                .append(", P: ").append(P)
                .append(", D: ").append(D)
                .append(", Q: ").append(Q)
                .append(", m: ").append(m)
                .append(" }");
        sb.append("\n\nFitted Model RMSE: ").append(dbl2str(forecastResult.getRMSE()));
        sb.append("\n\n      TRUE DATA    |     LOWER BOUND          FORECAST       UPPER BOUND\n");

        for (int i = 0; i < forecast.length; ++i) {
            sb.append(dbl2str(trueForecastData[i])).append("    | ")
                    .append(dbl2str(lower[i])).append("   ").append(dbl2str(forecast[i]))
                    .append("   ").append(dbl2str(upper[i]))
                    .append("\n");
        }

        sb.append("\n");

        //Compute RMSE against true forecast data
        double temp = 0.0;
        for (int i = 0; i < forecast.length; ++i) {
            temp += Math.pow(forecast[i] - trueForecastData[i], 2);
        }
        final double rmse = Math.pow(temp / forecast.length, 0.5);
        sb.append("RMSE = ").append(dbl2str(rmse)).append("\n\n");
        System.out.println(sb.toString());
        return rmse;
    }

    private void commonAssertionLogic(double[] dataSet, double actualValue, double delta) {
        double lastTrueValue = dataSet[dataSet.length - 1];
        Assert.assertEquals(lastTrueValue, actualValue, delta);
    }


    @Test
    public void arma2ma_test() {
        double[] ar = {1.0, -0.25};
        double[] ma = {1.0, 2.0};
        int lag = 10;
        double[] ma_coeff = ForecastUtil.ARMAtoMA(ar, ma, lag);
        double[] true_coeff = {1.0, 2.0, 3.75, 3.25, 2.3125, 1.5, 0.921875, 0.546875, 0.31640625,
                0.1796875};

        Assert.assertArrayEquals(ma_coeff, true_coeff, 1e-6);
    }

    @Test(expected = RuntimeException.class)
    public void common_logic_fail_test() {
        commonTestLogic("simple12345", Datasets.simple12345, 0.1, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test(expected = RuntimeException.class)
    public void one_piont_fail_test() {
        forecastSinglePointLogic("simple12345", Datasets.simple12345, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void a10_test() {
        commonTestLogic("a10_test", Datasets.a10_val, 0.1, 3, 0, 0, 1, 0, 1, 12);
        double actualValue = forecastSinglePointLogic("a10_test", Datasets.a10_val, 3, 0, 0, 1, 0,
                1, 12);
        commonAssertionLogic(Datasets.a10_val, actualValue, 5.05);
    }


    @Test
    public void usconsumption_test() {
        commonTestLogic("usconsumption_test", Datasets.usconsumption_val, 0.1, 1, 0, 3, 1, 0, 1,
                42);
        double lastActualData = forecastSinglePointLogic("usconsumption_test",
                Datasets.usconsumption_val, 0, 0, 3, 0, 0, 1, 42);
        commonAssertionLogic(Datasets.usconsumption_val, lastActualData, 0.15);
    }

    @Test
    public void euretail_test() {
        commonTestLogic("euretail_test", Datasets.euretail_val, 0.1, 3, 0, 3, 1, 1, 0, 0);
        double lastActualData = forecastSinglePointLogic("euretail_test", Datasets.euretail_val, 3,
                0, 3, 1, 1, 0, 0);
        commonAssertionLogic(Datasets.euretail_val, lastActualData, 0.23);
    }

    @Test
    public void sunspots_test() {
        commonTestLogic("sunspots_test", Datasets.sunspots_val, 0.1, 2, 0, 0, 1, 0, 1, 21);
        double actualValue = forecastSinglePointLogic("sunspots_test", Datasets.sunspots_val, 2, 0,
                0, 1, 0, 1, 21);
        commonAssertionLogic(Datasets.sunspots_val, actualValue, 11.83);

    }

    @Test
    public void ausbeer_test() {
        commonTestLogic("ausbeer_test", Datasets.ausbeer_val, 0.1, 2, 0, 1, 1, 0, 1, 8);
        double actualValue = forecastSinglePointLogic("ausbeer_test", Datasets.ausbeer_val, 2, 0, 1,
                1, 0, 1, 8);
        commonAssertionLogic(Datasets.ausbeer_val, actualValue, 8.04);

    }

    @Test
    public void elecequip_test() {
        commonTestLogic("elecequip_test", Datasets.elecequip_val, 0.1, 3, 0, 1, 1, 0, 1, 6);
        double actualValue = forecastSinglePointLogic("elecequip_test", Datasets.elecequip_val, 3,
                0, 1, 1, 0, 1, 6);
        commonAssertionLogic(Datasets.elecequip_val, actualValue, 5.63);
    }

    @Test
    public void chicago_potholes_test() {
        commonTestLogic("chicago_potholes_test", Datasets.chicago_potholes_val, 0.1, 3, 0, 3, 0, 1,
                1, 14);
        double actualValue = forecastSinglePointLogic("chicago_potholes_test",
                Datasets.chicago_potholes_val, 3, 0, 3, 0, 1, 1, 14);
        commonAssertionLogic(Datasets.chicago_potholes_val, actualValue, 25.94);
    }

    @Test
    public void simple_data1_test() {
        final double forecast = forecastSinglePointLogic("simple_data1_test",
                Datasets.simple_data1_val, 3, 0, 3, 1, 1, 0, 0);
        assert (forecast == 2);
    }

    @Test
    public void simple_data2_test() {
        final double forecast = forecastSinglePointLogic("simple_data2_test",
                Datasets.simple_data2_val, 0, 0, 1, 0, 0, 0, 0);
        assert (forecast == 2);
    }

    @Test
    public void simple_data3_test() {
        final double[] forecast = commonTestSimpleForecast("simple_data3_test",
                Datasets.simple_data3_val, Datasets.simple_data3_answer, 7, 3, 0, 0, 1, 0, 1, 12);
        double lastActualData = forecast[forecast.length - 1];
        commonAssertionLogic(Datasets.simple_data3_answer, lastActualData, 0.31);
    }

    @Test
    public void cscchris_test() {
        final int[] params = new int[]{0, 1, 2, 3};
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_rmse = -1.0;
        for(int p : params) for(int d : params) for(int q : params) for(int P : params)
            for(int D : params) for(int Q : params) for(int m : params) try {
                final double rmse = commonTestCalculateRMSE("cscchris_test",
                        Datasets.cscchris_val, Datasets.cscchris_answer, 6, p, d, q, P, D, Q, m);
                if (best_rmse < 0.0 || rmse < best_rmse) {
                    best_rmse = rmse; best_p = p; best_d = d; best_q = q;
                    best_P = P; best_D = D; best_Q = q; best_m = m;
                    System.out.printf(
                            "Better (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,p,d,q,P,D,Q,m);
                }
            } catch (final Exception ex) {
                System.out.printf("Invalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);

            }
        System.out.printf("Best (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n",
                best_rmse,best_p,best_d,best_q,best_P,best_D,best_Q,best_m);
    }
}
 final class Datasets {

    /**
     * Simple one two tree four five
     */
    public static double[] simple12345 = {1.0, 2.0, 3.0, 4.0, 5.0};

    /**
     * a10 Monthly anti-diabetic drug sales in Australia from 1992 to 2008. strong complex seasonal
     * component as well as a (super)linear trend
     * [Source] https://rdrr.io/cran/fpp/man/a10.html
     */
    public static String[] a10_key = {"07-1991", "08-1991", "09-1991", "10-1991", "11-1991",
            "12-1991", "01-1992", "02-1992", "03-1992",
            "04-1992", "05-1992", "06-1992", "07-1992", "08-1992", "09-1992", "10-1992", "11-1992",
            "12-1992",
            "01-1993", "02-1993", "03-1993", "04-1993", "05-1993", "06-1993", "07-1993", "08-1993",
            "09-1993",
            "10-1993", "11-1993", "12-1993", "01-1994", "02-1994", "03-1994", "04-1994", "05-1994",
            "06-1994",
            "07-1994", "08-1994", "09-1994", "10-1994", "11-1994", "12-1994", "01-1995", "02-1995",
            "03-1995",
            "04-1995", "05-1995", "06-1995", "07-1995", "08-1995", "09-1995", "10-1995", "11-1995",
            "12-1995",
            "01-1996", "02-1996", "03-1996", "04-1996", "05-1996", "06-1996", "07-1996", "08-1996",
            "09-1996",
            "10-1996", "11-1996", "12-1996", "01-1997", "02-1997", "03-1997", "04-1997", "05-1997",
            "06-1997",
            "07-1997", "08-1997", "09-1997", "10-1997", "11-1997", "12-1997", "01-1998", "02-1998",
            "03-1998",
            "04-1998", "05-1998", "06-1998", "07-1998", "08-1998", "09-1998", "10-1998", "11-1998",
            "12-1998",
            "01-1999", "02-1999", "03-1999", "04-1999", "05-1999", "06-1999", "07-1999", "08-1999",
            "09-1999",
            "10-1999", "11-1999", "12-1999", "01-2000", "02-2000", "03-2000", "04-2000", "05-2000",
            "06-2000",
            "07-2000", "08-2000", "09-2000", "10-2000", "11-2000", "12-2000", "01-2001", "02-2001",
            "03-2001",
            "04-2001", "05-2001", "06-2001", "07-2001", "08-2001", "09-2001", "10-2001", "11-2001",
            "12-2001",
            "01-2002", "02-2002", "03-2002", "04-2002", "05-2002", "06-2002", "07-2002", "08-2002",
            "09-2002",
            "10-2002", "11-2002", "12-2002", "01-2003", "02-2003", "03-2003", "04-2003", "05-2003",
            "06-2003",
            "07-2003", "08-2003", "09-2003", "10-2003", "11-2003", "12-2003", "01-2004", "02-2004",
            "03-2004",
            "04-2004", "05-2004", "06-2004", "07-2004", "08-2004", "09-2004", "10-2004", "11-2004",
            "12-2004",
            "01-2005", "02-2005", "03-2005", "04-2005", "05-2005", "06-2005", "07-2005", "08-2005",
            "09-2005",
            "10-2005", "11-2005", "12-2005", "01-2006", "02-2006", "03-2006", "04-2006", "05-2006",
            "06-2006",
            "07-2006", "08-2006", "09-2006", "10-2006", "11-2006", "12-2006", "01-2007", "02-2007",
            "03-2007",
            "04-2007", "05-2007", "06-2007", "07-2007", "08-2007", "09-2007", "10-2007", "11-2007",
            "12-2007",
            "01-2008", "02-2008", "03-2008", "04-2008", "05-2008", "06-2008"};

    public static double[] a10_val = {3.526591, 3.180891, 3.252221, 3.611003, 3.565869, 4.306371,
            5.088335, 2.814520, 2.985811, 3.204780,
            3.127578, 3.270523, 3.737851, 3.558776, 3.777202, 3.924490, 4.386531, 5.810549, 6.192068,
            3.450857,
            3.772307, 3.734303, 3.905399, 4.049687, 4.315566, 4.562185, 4.608662, 4.667851, 5.093841,
            7.179962,
            6.731473, 3.841278, 4.394076, 4.075341, 4.540645, 4.645615, 4.752607, 5.350605, 5.204455,
            5.301651,
            5.773742, 6.204593, 6.749484, 4.216067, 4.949349, 4.823045, 5.194754, 5.170787, 5.256742,
            5.855277,
            5.490729, 6.115293, 6.088473, 7.416598, 8.329452, 5.069796, 5.262557, 5.597126, 6.110296,
            5.689161,
            6.486849, 6.300569, 6.467476, 6.828629, 6.649078, 8.606937, 8.524471, 5.277918, 5.714303,
            6.214529,
            6.411929, 6.667716, 7.050831, 6.704919, 7.250988, 7.819733, 7.398101, 10.096233, 8.798513,
            5.918261,
            6.534493, 6.675736, 7.064201, 7.383381, 7.813496, 7.431892, 8.275117, 8.260441, 8.596156,
            10.558939,
            10.391416, 6.421535, 8.062619, 7.297739, 7.936916, 8.165323, 8.717420, 9.070964, 9.177113,
            9.251887,
            9.933136, 11.532974, 12.511462, 7.457199, 8.591191, 8.474000, 9.386803, 9.560399, 10.834295,
            10.643751, 9.908162, 11.710041, 11.340151, 12.079132, 14.497581, 8.049275, 10.312891,
            9.753358,
            10.850382, 9.961719, 11.443601, 11.659239, 10.647060, 12.652134, 13.674466, 12.965735,
            16.300269,
            9.053485, 10.002449, 10.788750, 12.106705, 10.954101, 12.844566, 12.196500, 12.854748,
            13.542004,
            13.287640, 15.134918, 16.828350, 9.800215, 10.816994, 10.654223, 12.512323, 12.161210,
            12.998046,
            12.517276, 13.268658, 14.733622, 13.669382, 16.503966, 18.003768, 11.938030, 12.997900,
            12.882645,
            13.943447, 13.989472, 15.339097, 15.370764, 16.142005, 16.685754, 17.636728, 18.869325,
            20.778723,
            12.154552, 13.402392, 14.459239, 14.795102, 15.705248, 15.829550, 17.554701, 18.100864,
            17.496668,
            19.347265, 20.031291, 23.486694, 12.536987, 15.467018, 14.233539, 17.783058, 16.291602,
            16.980282,
            18.612189, 16.623343, 21.430241, 23.575517, 23.334206, 28.038383, 16.763869, 19.792754,
            16.427305,
            21.000742, 20.681002, 21.834890, 23.930204, 22.930357, 23.263340, 25.250030, 25.806090,
            29.665356,
            21.654285, 18.264945, 23.107677, 22.912510, 19.431740};

    /**
     * ausbeer Total quarterly beer production in Australia (in megalitres) from 1956:Q1 to 2008:Q3.
     * strong simple seasonal component, part of a trend that then discontinues
     * [Source] https://rdrr.io/cran/fpp/man/ausbeer.html
     */
    public static String[] ausbeer_key = {"1956:Q1", "1956:Q2", "1956:Q3", "1956:Q4", "1957:Q1",
            "1957:Q2", "1957:Q3", "1957:Q4", "1958:Q1",
            "1958:Q2", "1958:Q3", "1958:Q4", "1959:Q1", "1959:Q2", "1959:Q3", "1959:Q4", "1960:Q1",
            "1960:Q2",
            "1960:Q3", "1960:Q4", "1961:Q1", "1961:Q2", "1961:Q3", "1961:Q4", "1962:Q1", "1962:Q2",
            "1962:Q3",
            "1962:Q4", "1963:Q1", "1963:Q2", "1963:Q3", "1963:Q4", "1964:Q1", "1964:Q2", "1964:Q3",
            "1964:Q4",
            "1965:Q1", "1965:Q2", "1965:Q3", "1965:Q4", "1966:Q1", "1966:Q2", "1966:Q3", "1966:Q4",
            "1967:Q1",
            "1967:Q2", "1967:Q3", "1967:Q4", "1968:Q1", "1968:Q2", "1968:Q3", "1968:Q4", "1969:Q1",
            "1969:Q2",
            "1969:Q3", "1969:Q4", "1970:Q1", "1970:Q2", "1970:Q3", "1970:Q4", "1971:Q1", "1971:Q2",
            "1971:Q3",
            "1971:Q4", "1972:Q1", "1972:Q2", "1972:Q3", "1972:Q4", "1973:Q1", "1973:Q2", "1973:Q3",
            "1973:Q4",
            "1974:Q1", "1974:Q2", "1974:Q3", "1974:Q4", "1975:Q1", "1975:Q2", "1975:Q3", "1975:Q4",
            "1976:Q1",
            "1976:Q2", "1976:Q3", "1976:Q4", "1977:Q1", "1977:Q2", "1977:Q3", "1977:Q4", "1978:Q1",
            "1978:Q2",
            "1978:Q3", "1978:Q4", "1979:Q1", "1979:Q2", "1979:Q3", "1979:Q4", "1980:Q1", "1980:Q2",
            "1980:Q3",
            "1980:Q4", "1981:Q1", "1981:Q2", "1981:Q3", "1981:Q4", "1982:Q1", "1982:Q2", "1982:Q3",
            "1982:Q4",
            "1983:Q1", "1983:Q2", "1983:Q3", "1983:Q4", "1984:Q1", "1984:Q2", "1984:Q3", "1984:Q4",
            "1985:Q1",
            "1985:Q2", "1985:Q3", "1985:Q4", "1986:Q1", "1986:Q2", "1986:Q3", "1986:Q4", "1987:Q1",
            "1987:Q2",
            "1987:Q3", "1987:Q4", "1988:Q1", "1988:Q2", "1988:Q3", "1988:Q4", "1989:Q1", "1989:Q2",
            "1989:Q3",
            "1989:Q4", "1990:Q1", "1990:Q2", "1990:Q3", "1990:Q4", "1991:Q1", "1991:Q2", "1991:Q3",
            "1991:Q4",
            "1992:Q1", "1992:Q2", "1992:Q3", "1992:Q4", "1993:Q1", "1993:Q2", "1993:Q3", "1993:Q4",
            "1994:Q1",
            "1994:Q2", "1994:Q3", "1994:Q4", "1995:Q1", "1995:Q2", "1995:Q3", "1995:Q4", "1996:Q1",
            "1996:Q2",
            "1996:Q3", "1996:Q4", "1997:Q1", "1997:Q2", "1997:Q3", "1997:Q4", "1998:Q1", "1998:Q2",
            "1998:Q3",
            "1998:Q4", "1999:Q1", "1999:Q2", "1999:Q3", "1999:Q4", "2000:Q1", "2000:Q2", "2000:Q3",
            "2000:Q4",
            "2001:Q1", "2001:Q2", "2001:Q3", "2001:Q4", "2002:Q1", "2002:Q2", "2002:Q3", "2002:Q4",
            "2003:Q1",
            "2003:Q2", "2003:Q3", "2003:Q4", "2004:Q1", "2004:Q2", "2004:Q3", "2004:Q4", "2005:Q1",
            "2005:Q2",
            "2005:Q3", "2005:Q4", "2006:Q1", "2006:Q2", "2006:Q3", "2006:Q4", "2007:Q1", "2007:Q2",
            "2007:Q3",
            "2007:Q4", "2008:Q1", "2008:Q2", "2008:Q3", "2008:Q4"};

    public static double[] ausbeer_val = {284.0, 213.0, 227.0, 308.0, 262.0, 228.0, 236.0, 320.0,
            272.0, 233.0, 237.0, 313.0, 261.0, 227.0, 250.0, 314.0, 286.0, 227.0, 260.0, 311.0, 295.0,
            233.0, 257.0,
            339.0, 279.0, 250.0, 270.0, 346.0, 294.0, 255.0, 278.0, 363.0, 313.0, 273.0, 300.0, 370.0,
            331.0, 288.0, 306.0, 386.0, 335.0, 288.0, 308.0, 402.0, 353.0, 316.0,
            325.0, 405.0, 393.0, 319.0, 327.0, 442.0, 383.0, 332.0, 361.0, 446.0, 387.0, 357.0, 374.0,
            466.0, 410.0, 370.0, 379.0, 487.0, 419.0, 378.0, 393.0, 506.0, 458.0,
            387.0, 427.0, 565.0, 465.0, 445.0, 450.0, 556.0, 500.0, 452.0, 435.0, 554.0, 510.0, 433.0,
            453.0, 548.0, 486.0, 453.0, 457.0, 566.0, 515.0, 464.0, 431.0, 588.0,
            503.0, 443.0, 448.0, 555.0, 513.0, 427.0, 473.0, 526.0, 548.0, 440.0, 469.0, 575.0, 493.0,
            433.0, 480.0, 576.0, 475.0, 405.0, 435.0, 535.0, 453.0, 430.0, 417.0,
            552.0, 464.0, 417.0, 423.0, 554.0, 459.0, 428.0, 429.0, 534.0, 481.0, 416.0, 440.0, 538.0,
            474.0, 440.0, 447.0, 598.0, 467.0, 439.0, 446.0, 567.0, 485.0, 441.0,
            429.0, 599.0, 464.0, 424.0, 436.0, 574.0, 443.0, 410.0, 420.0, 532.0, 433.0, 421.0, 410.0,
            512.0, 449.0, 381.0, 423.0, 531.0, 426.0, 408.0, 416.0, 520.0, 409.0,
            398.0, 398.0, 507.0, 432.0, 398.0, 406.0, 526.0, 428.0, 397.0, 403.0, 517.0, 435.0, 383.0,
            424.0, 521.0, 421.0, 402.0, 414.0, 500.0, 451.0, 380.0, 416.0, 492.0,
            428.0, 408.0, 406.0, 506.0, 435.0, 380.0, 421.0, 490.0, 435.0, 390.0, 412.0, 454.0, 416.0,
            403.0, 408.0, 482.0, 438.0, 386.0, 405.0, 491.0, 427.0, 383.0, 394.0,
            473.0, 420.0, 390.0, 410.0};
    /**
     * elecequip Manufacture of electrical equipment; Data adjusted by working days; Euro area (16
     * countries). Industry new orders index. 2005=100. slight seasonal pattern, no clear trend,
     * random on a larger range than the trend can show --- good edge case
     * [Source] https://rdrr.io/cran/fpp/man/elecequip.html
     */
    public static String[] elecequip_key = {"01-1996", "02-1996", "03-1996", "04-1996", "05-1996",
            "06-1996", "07-1996", "08-1996", "09-1996",
            "10-1996", "11-1996", "12-1996", "01-1997", "02-1997", "03-1997", "04-1997", "05-1997",
            "06-1997",
            "07-1997", "08-1997", "09-1997", "10-1997", "11-1997", "12-1997", "01-1998", "02-1998",
            "03-1998",
            "04-1998", "05-1998", "06-1998", "07-1998", "08-1998", "09-1998", "10-1998", "11-1998",
            "12-1998",
            "01-1999", "02-1999", "03-1999", "04-1999", "05-1999", "06-1999", "07-1999", "08-1999",
            "09-1999",
            "10-1999", "11-1999", "12-1999", "01-2000", "02-2000", "03-2000", "04-2000", "05-2000",
            "06-2000",
            "07-2000", "08-2000", "09-2000", "10-2000", "11-2000", "12-2000", "01-2001", "02-2001",
            "03-2001",
            "04-2001", "05-2001", "06-2001", "07-2001", "08-2001", "09-2001", "10-2001", "11-2001",
            "12-2001",
            "01-2002", "02-2002", "03-2002", "04-2002", "05-2002", "06-2002", "07-2002", "08-2002",
            "09-2002",
            "10-2002", "11-2002", "12-2002", "01-2003", "02-2003", "03-2003", "04-2003", "05-2003",
            "06-2003",
            "07-2003", "08-2003", "09-2003", "10-2003", "11-2003", "12-2003", "01-2004", "02-2004",
            "03-2004",
            "04-2004", "05-2004", "06-2004", "07-2004", "08-2004", "09-2004", "10-2004", "11-2004",
            "12-2004",
            "01-2005", "02-2005", "03-2005", "04-2005", "05-2005", "06-2005", "07-2005", "08-2005",
            "09-2005",
            "10-2005", "11-2005", "12-2005", "01-2006", "02-2006", "03-2006", "04-2006", "05-2006",
            "06-2006",
            "07-2006", "08-2006", "09-2006", "10-2006", "11-2006", "12-2006", "01-2007", "02-2007",
            "03-2007",
            "04-2007", "05-2007", "06-2007", "07-2007", "08-2007", "09-2007", "10-2007", "11-2007",
            "12-2007",
            "01-2008", "02-2008", "03-2008", "04-2008", "05-2008", "06-2008", "07-2008", "08-2008",
            "09-2008",
            "10-2008", "11-2008", "12-2008", "01-2009", "02-2009", "03-2009", "04-2009", "05-2009",
            "06-2009",
            "07-2009", "08-2009", "09-2009", "10-2009", "11-2009", "12-2009", "01-2010", "02-2010",
            "03-2010",
            "04-2010", "05-2010", "06-2010", "07-2010", "08-2010", "09-2010", "10-2010", "11-2010",
            "12-2010",
            "01-2011", "02-2011", "03-2011", "04-2011", "05-2011", "06-2011", "07-2011", "08-2011",
            "09-2011",
            "10-2011", "11-2011"};
    public static double[] elecequip_val = {79.43, 75.86, 86.40, 72.67, 74.93, 83.88, 79.88, 62.47,
            85.50, 83.19, 84.29, 89.79, 78.72, 77.49, 89.94,
            81.35, 78.76, 89.59, 83.75, 69.87, 91.18, 89.52, 91.12, 92.97, 81.97, 85.26, 93.09, 81.19,
            85.74, 91.24,
            83.56, 66.45, 93.45, 86.03, 86.91, 93.42, 81.68, 81.68, 91.35, 79.55, 87.08, 96.71, 98.10,
            79.22, 103.68,
            101.00, 99.52, 111.94, 95.42, 98.49, 116.37, 101.09, 104.20, 114.79, 107.75, 96.23, 123.65,
            116.24,
            117.00, 128.75, 100.69, 102.99, 119.21, 92.56, 98.86, 111.26, 96.25, 79.81, 102.18, 96.28,
            101.38,
            109.97, 89.66, 89.23, 104.36, 87.17, 89.43, 102.25, 88.26, 75.73, 99.60, 96.57, 96.22,
            101.12, 89.45,
            86.87, 98.94, 85.62, 85.31, 101.22, 91.93, 77.01, 104.50, 99.83, 101.10, 109.16, 89.93,
            92.73, 105.22,
            91.56, 92.60, 104.46, 96.28, 79.61, 105.55, 99.15, 99.81, 113.72, 91.73, 90.45, 105.56,
            92.15, 91.23,
            108.95, 99.33, 83.30, 110.85, 104.99, 107.10, 114.38, 99.09, 99.73, 116.05, 103.51, 102.99,
            119.45,
            107.98, 90.50, 121.85, 117.12, 113.66, 120.35, 103.92, 103.97, 125.63, 104.69, 108.36,
            123.09, 108.88,
            93.98, 121.94, 116.79, 115.78, 127.28, 109.35, 105.64, 121.30, 108.62, 103.13, 117.84,
            103.62, 89.22,
            109.41, 103.93, 100.07, 101.15, 77.33, 75.01, 86.31, 74.09, 74.09, 85.58, 79.84, 65.24,
            87.92, 84.45,
            87.93, 102.42, 79.16, 78.40, 94.32, 84.45, 84.92, 103.18, 89.42, 77.66, 95.68, 94.03,
            100.99, 101.26,
            91.47, 87.66, 103.33, 88.56, 92.32, 102.21, 92.80, 76.44, 94.00, 91.67, 91.98};
    /**
     * euretail Quarterly retail trade index in the Euro area (17 countries), 1996-2011, (Index:
     * 2005 = 100) no seasonal component, strong trend component but then trend gets violated at the
     * end, local vs global trend, test overfitting p value (great for grid searching)
     * [Source] https://rdrr.io/cran/fpp/man/euretail.html
     */
    public static String[] euretail_key = {"1996:Q1", "1996:Q2", "1996:Q3", "1996:Q4", "1997:Q1",
            "1997:Q2", "1997:Q3", "1997:Q4", "1998:Q1",
            "1998:Q2", "1998:Q3", "1998:Q4", "1999:Q1", "1999:Q2", "1999:Q3", "1999:Q4", "2000:Q1",
            "2000:Q2",
            "2000:Q3", "2000:Q4", "2001:Q1", "2001:Q2", "2001:Q3", "2001:Q4", "2002:Q1", "2002:Q2",
            "2002:Q3",
            "2002:Q4", "2003:Q1", "2003:Q2", "2003:Q3", "2003:Q4", "2004:Q1", "2004:Q2", "2004:Q3",
            "2004:Q4",
            "2005:Q1", "2005:Q2", "2005:Q3", "2005:Q4", "2006:Q1", "2006:Q2", "2006:Q3", "2006:Q4",
            "2007:Q1",
            "2007:Q2", "2007:Q3", "2007:Q4", "2008:Q1", "2008:Q2", "2008:Q3", "2008:Q4", "2009:Q1",
            "2009:Q2",
            "2009:Q3", "2009:Q4", "2010:Q1", "2010:Q2", "2010:Q3", "2010:Q4", "2011:Q1", "2011:Q2",
            "2011:Q3",
            "2011:Q4"};
    public static double[] euretail_val = {89.13, 89.52, 89.88, 90.12, 89.19, 89.78, 90.03, 90.38,
            90.27, 90.77, 91.85, 92.51, 92.21, 92.52, 93.62,
            94.15, 94.69, 95.34, 96.04, 96.30, 94.83, 95.14, 95.86, 95.83, 95.73, 96.36, 96.89, 97.01,
            96.66, 97.76,
            97.83, 97.76, 98.17, 98.55, 99.31, 99.44, 99.43, 99.84, 100.32, 100.40, 99.88, 100.19,
            100.75, 101.01,
            100.84, 101.34, 101.94, 102.10, 101.56, 101.48, 101.13, 100.34, 98.93, 98.31, 97.67, 97.44,
            96.53,
            96.56, 96.51, 96.70, 95.88, 95.84, 95.79, 95.97};
    /**
     * usconsumption Percentage changes in quarterly personal consumption expenditure and personal
     * disposable income for the US, 1970 to 2010. no seasonal and also no clear trend -- shouldn"t
     * find any good fit
     * [Source] https://github.com/cran/fpp/blob/master/man/usconsumption.Rd
     */
    public static String[] usconsumption_key = {"1970:Q1", "1970:Q2", "1970:Q3", "1970:Q4",
            "1971:Q1", "1971:Q2", "1971:Q3", "1971:Q4", "1972:Q1",
            "1972:Q2", "1972:Q3", "1972:Q4", "1973:Q1", "1973:Q2", "1973:Q3", "1973:Q4", "1974:Q1",
            "1974:Q2",
            "1974:Q3", "1974:Q4", "1975:Q1", "1975:Q2", "1975:Q3", "1975:Q4", "1976:Q1", "1976:Q2",
            "1976:Q3",
            "1976:Q4", "1977:Q1", "1977:Q2", "1977:Q3", "1977:Q4", "1978:Q1", "1978:Q2", "1978:Q3",
            "1978:Q4",
            "1979:Q1", "1979:Q2", "1979:Q3", "1979:Q4", "1980:Q1", "1980:Q2", "1980:Q3", "1980:Q4",
            "1981:Q1",
            "1981:Q2", "1981:Q3", "1981:Q4", "1982:Q1", "1982:Q2", "1982:Q3", "1982:Q4", "1983:Q1",
            "1983:Q2",
            "1983:Q3", "1983:Q4", "1984:Q1", "1984:Q2", "1984:Q3", "1984:Q4", "1985:Q1", "1985:Q2",
            "1985:Q3",
            "1985:Q4", "1986:Q1", "1986:Q2", "1986:Q3", "1986:Q4", "1987:Q1", "1987:Q2", "1987:Q3",
            "1987:Q4",
            "1988:Q1", "1988:Q2", "1988:Q3", "1988:Q4", "1989:Q1", "1989:Q2", "1989:Q3", "1989:Q4",
            "1990:Q1",
            "1990:Q2", "1990:Q3", "1990:Q4", "1991:Q1", "1991:Q2", "1991:Q3", "1991:Q4", "1992:Q1",
            "1992:Q2",
            "1992:Q3", "1992:Q4", "1993:Q1", "1993:Q2", "1993:Q3", "1993:Q4", "1994:Q1", "1994:Q2",
            "1994:Q3",
            "1994:Q4", "1995:Q1", "1995:Q2", "1995:Q3", "1995:Q4", "1996:Q1", "1996:Q2", "1996:Q3",
            "1996:Q4",
            "1997:Q1", "1997:Q2", "1997:Q3", "1997:Q4", "1998:Q1", "1998:Q2", "1998:Q3", "1998:Q4",
            "1999:Q1",
            "1999:Q2", "1999:Q3", "1999:Q4", "2000:Q1", "2000:Q2", "2000:Q3", "2000:Q4", "2001:Q1",
            "2001:Q2",
            "2001:Q3", "2001:Q4", "2002:Q1", "2002:Q2", "2002:Q3", "2002:Q4", "2003:Q1", "2003:Q2",
            "2003:Q3",
            "2003:Q4", "2004:Q1", "2004:Q2", "2004:Q3", "2004:Q4", "2005:Q1", "2005:Q2", "2005:Q3",
            "2005:Q4",
            "2006:Q1", "2006:Q2", "2006:Q3", "2006:Q4", "2007:Q1", "2007:Q2", "2007:Q3", "2007:Q4",
            "2008:Q1",
            "2008:Q2", "2008:Q3", "2008:Q4", "2009:Q1", "2009:Q2", "2009:Q3", "2009:Q4", "2010:Q1",
            "2010:Q2",
            "2010:Q3", "2010:Q4"};
    public static double[] usconsumption_val = {0.61227692, 0.45492979, 0.87467302, -0.27251439,
            1.89218699, 0.91337819, 0.79285790, 1.64999566,
            1.32724825, 1.88990506, 1.53272416, 2.31705777, 1.81385569, -0.05055772, 0.35966722,
            -0.29331546,
            -0.87877094, 0.34672003, 0.41195356, -1.47820468, 0.83735987, 1.65397369, 1.41431884,
            1.05310993,
            1.97774749, 0.91507218, 1.05074607, 1.29519619, 1.13545889, 0.55153240, 0.95015960,
            1.49616150,
            0.58229978, 2.11467168, 0.41869886, 0.80276430, 0.50412878, -0.05855113, 0.97755597,
            0.26591209,
            -0.17368425, -2.29656300, 1.06691983, 1.32441742, 0.54583283, 0.00000000, 0.40482184,
            -0.75874883,
            0.64399814, 0.35685950, 0.76412375, 1.80788661, 0.97593734, 1.96559809, 1.75134970,
            1.57374005,
            0.85322727, 1.42002574, 0.76950200, 1.30747803, 1.68128155, 0.90791081, 1.88085044,
            0.21986403,
            0.83153359, 1.05966370, 1.73244172, 0.60006243, -0.15228800, 1.32935729, 1.11041685,
            0.24012547,
            1.65692852, 0.72306031, 0.78681412, 1.17068014, 0.36522624, 0.44694325, 1.03134287,
            0.48794531,
            0.78786794, 0.32958888, 0.37909401, -0.78228237, -0.28358087, 0.75819378, 0.38256742,
            -0.04493204,
            1.70442848, 0.59103346, 1.09931218, 1.21261583, 0.40511535, 0.95540152, 1.07908089,
            0.88609934,
            1.10781585, 0.73801073, 0.79832641, 0.98235581, 0.11670364, 0.81643944, 0.89012327,
            0.70025668,
            0.90999511, 1.12517415, 0.59749105, 0.81104981, 1.00231479, 0.40370845, 1.68561876,
            1.13779625,
            0.98935016, 1.70668759, 1.31690105, 1.52238359, 0.98149855, 1.56147049, 1.19479035,
            1.40026421,
            1.50504064, 0.93588274, 0.97432184, 0.88064976, 0.39868539, 0.37651229, 0.43918859,
            1.55369100,
            0.34382689, 0.50665404, 0.67571194, 0.35472465, 0.50387273, 0.98555573, 1.33766670,
            0.54254673,
            0.88074795, 0.44397949, 0.87037870, 1.07395152, 0.79393888, 0.98477889, 0.75627802,
            0.24819787,
            1.01902713, 0.60048219, 0.59799998, 0.92584113, 0.55424125, 0.38257957, 0.43929546,
            0.29465872,
            -0.25266521, -0.03553182, -0.97177447, -1.31350400, -0.38748400, -0.47008302, 0.57400096,
            0.10932885, 0.67101795, 0.71771819, 0.65314326, 0.87535215};

    // Yearly numbers of sunspots
    // [Source] https://stat.ethz.ch/R-manual/R-devel/library/datasets/html/sunspot.year.html
    public static double[] sunspots_val = {5, 11, 16, 23, 36, 58, 29, 20, 10, 8, 3, 0, 0, 2, 11, 27,
            47, 63, 60, 39, 28, 26, 22, 11, 21, 40, 78, 122, 103, 73,
            47, 35, 11, 5, 16, 34, 70, 81, 111, 101, 73, 40, 20, 16, 5, 11, 22, 40, 60, 80.9, 83.4,
            47.7, 47.8, 30.7, 12.2, 9.6, 10.2, 32.4,
            47.6, 54, 62.9, 85.9, 61.2, 45.1, 36.4, 20.9, 11.4, 37.8, 69.8, 106.1, 100.8, 81.6, 66.5,
            34.8, 30.6, 7, 19.8, 92.5, 154.4,
            125.9, 84.8, 68.1, 38.5, 22.8, 10.2, 24.1, 82.9, 132, 130.9, 118.1, 89.9, 66.6, 60, 46.9,
            41, 21.3, 16, 6.4, 4.1, 6.8, 14.5,
            34, 45, 43.1, 47.5, 42.2, 28.1, 10.1, 8.1, 2.5, 0, 1.4, 5, 12.2, 13.9, 35.4, 45.8, 41.1,
            30.1, 23.9, 15.6, 6.6, 4, 1.8, 8.5,
            16.6, 36.3, 49.6, 64.2, 67, 70.9, 47.8, 27.5, 8.5, 13.2, 56.9, 121.5, 138.3, 103.2, 85.7,
            64.6, 36.7, 24.2, 10.7, 15, 40.1,
            61.5, 98.5, 124.7, 96.3, 66.6, 64.5, 54.1, 39, 20.6, 6.7, 4.3, 22.7, 54.8, 93.8, 95.8, 77.2,
            59.1, 44, 47, 30.5, 16.3, 7.3,
            37.6, 74, 139, 111.2, 101.6, 66.2, 44.7, 17, 11.3, 12.4, 3.4, 6, 32.3, 54.3, 59.7, 63.7,
            63.5, 52.2, 25.4, 13.1, 6.8, 6.3,
            7.1, 35.6, 73, 85.1, 78, 64, 41.8, 26.2, 26.7, 12.1, 9.5, 2.7, 5, 24.4, 42, 63.5, 53.8, 62,
            48.5, 43.9, 18.6, 5.7, 3.6, 1.4,
            9.6, 47.4, 57.1, 103.9, 80.6, 63.6, 37.6, 26.1, 14.2, 5.8, 16.7, 44.3, 63.9, 69, 77.8, 64.9,
            35.7, 21.2, 11.1, 5.7, 8.7,
            36.1, 79.7, 114.4, 109.6, 88.8, 67.8, 47.5, 30.6, 16.3, 9.6, 33.2, 92.6, 151.6, 136.3,
            134.7, 83.9, 69.4, 31.5, 13.9, 4.4,
            38, 141.7, 190.2, 184.8, 159, 112.3, 53.9, 37.6, 27.9, 10.2, 15.1, 47, 93.8, 105.9, 105.5,
            104.5, 66.6, 68.9, 38, 34.5,
            15.5, 12.6, 27.5, 92.5, 155.4, 154.6, 140.4, 115.9, 66.6, 45.9, 17.9, 13.4, 29.4, 100.2,
            157.6, 142.6, 145.7, 94.3, 54.6,
            29.9, 17.5, 8.6, 21.5, 64.3, 93.3, 119.6, 111, 104, 63.7, 40.4, 29.8, 15.2, 7.5, 2.9};

    // Number of potholes on Chicago streets
    public static double[] chicago_potholes_val = {1, 1, 1, 1, 2, 2, 1, 2, 1, 2, 1, 1, 1, 1, 2, 1,
            1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 2, 2, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1, 3,
            1, 1, 2, 1, 2, 1, 7, 2, 1, 1, 1, 2, 1, 3, 1, 3, 1, 4, 3, 1, 2, 1, 1, 1, 6, 1, 1, 3, 1, 1, 1,
            2, 2, 3, 3, 1, 2, 2, 1, 4, 2,
            1, 2, 4, 5, 2, 2, 1, 1, 3, 3, 1, 4, 5, 2, 4, 1, 5, 1, 3, 1, 4, 1, 5, 6, 7, 11, 3, 3, 1, 15,
            13, 13, 13, 7, 7, 17, 25, 35,
            61, 84, 93, 58, 57, 367, 451, 396, 383, 288, 34, 51, 387, 336, 287, 253, 293, 39, 48, 54,
            242, 273, 411, 261,
            68, 60, 233, 305, 342, 243, 241, 71, 52, 306, 201, 8, 16, 66, 18, 20, 192, 193, 111, 167,
            204, 46, 64, 453,
            925, 865, 543, 318, 134, 98, 486, 433, 777, 566, 500, 204, 168, 683, 695, 861, 623, 684,
            201, 228, 416,
            878, 1101, 1382, 797, 206, 164, 638, 877, 855, 863, 736, 184, 143, 631, 709, 642, 848, 654,
            143, 123, 672,
            534, 624, 495, 334, 137, 105, 483, 687, 544, 473, 510, 139, 67, 538, 529, 479, 301, 274, 98,
            84, 348, 377,
            291, 500, 446, 131, 86, 427, 519, 497, 651, 313, 117, 122, 447, 452, 496, 345, 320, 114, 77,
            290, 330, 334,
            269, 204, 75, 70, 270, 343, 570, 446, 429, 77, 68, 240, 382, 274, 242, 195, 72, 54, 54, 330,
            291, 279, 221,
            97, 98, 386, 321, 321, 286, 410, 113, 94, 301, 427, 265, 255, 231, 95, 65, 352, 337, 304,
            285, 489, 76, 81,
            327, 346, 274, 242, 114, 42, 41, 45, 244, 192, 329, 214, 60, 69, 162, 186, 159, 178, 201,
            42, 64, 190, 308,
            162, 277, 170, 30, 45, 196, 215, 169, 157, 168, 84, 39, 182, 212, 176, 153, 155, 40, 30,
            142, 176, 119, 196,
            207, 42, 42, 148, 130, 150, 142, 120, 29, 48, 165, 167, 140, 128, 141, 24, 23, 166, 157,
            171, 130, 117, 32,
            24, 27, 136, 105, 132, 132, 28, 21, 140, 131, 115, 134, 83, 27, 24, 137, 153, 101, 173, 115,
            33, 23, 148, 176,
            146, 130, 114, 30, 36, 183, 141, 193, 166, 152, 31, 33, 70, 235, 123, 113, 81, 40, 13, 90,
            148, 181, 202, 124,
            36, 22, 110, 139, 136, 141, 117, 37, 21, 137, 131, 108, 110, 112, 34, 32, 120, 88, 84, 124,
            73, 32, 30, 96, 133,
            105, 88, 83, 26, 20, 96, 129, 110, 6, 79, 23, 17, 136, 95, 120, 188, 137, 22, 14, 162, 155,
            131, 181, 95, 25, 9, 142,
            117, 86, 103, 86, 13, 19, 119, 126, 78, 88, 190, 6, 4, 13, 134, 126, 178, 104, 29, 22, 39,
            65, 192, 155, 156, 11,
            22, 154, 227, 151, 162, 77, 10, 11, 25, 207, 170, 151, 51, 12, 25, 362, 321, 284, 340, 190,
            40, 30, 226, 249,
            312, 238, 191, 26, 16, 323, 212, 275, 231, 278, 23, 16, 162, 318, 243, 315, 296, 22, 16,
            266, 414, 427, 439, 228,
            65, 48, 229, 331, 508, 376, 263, 53, 56, 100, 404, 336, 323, 245, 44, 45, 251, 359, 407,
            373, 203, 38, 74, 360,
            244, 294, 349, 274, 44, 53, 342, 234, 235, 334, 252, 80, 31, 223, 211, 206, 261, 154, 53,
            19, 183, 213, 167, 204,
            152, 37, 26, 155, 201, 222, 250, 171, 34, 40, 174, 285, 126, 137, 203, 30, 32, 201, 150,
            303, 355, 188, 83, 124,
            219, 181, 385, 159, 145, 38, 43, 165, 182, 152, 194, 142, 49, 34, 118, 164, 191, 140, 115,
            19, 14, 22, 116, 163,
            167, 173, 43, 38, 114, 122, 178, 95, 105, 22, 16, 129, 121, 136, 152, 105, 38, 21, 124, 132,
            144, 111, 122, 26,
            22, 90, 108, 113, 135, 133, 15, 26, 97, 211, 23, 101, 141, 21, 31, 110, 122, 115, 118, 80,
            26, 12, 85, 184, 198,
            88, 85, 35, 17, 55, 80, 107, 153, 103, 39, 31, 92, 112, 186, 139, 91, 26, 30, 97, 88, 94,
            47, 64, 29, 13, 96, 138,
            121, 115, 75, 38, 21, 83, 105, 87, 81, 81, 41, 8, 94, 79, 109, 92, 51, 27, 8, 5, 96, 121,
            71, 53, 21, 22, 88, 105,
            54, 90, 99, 41, 17, 98, 72, 141, 136, 79, 17, 11, 107, 155, 111, 96, 75, 15, 14, 196, 62,
            70, 122, 91, 31, 17,
            15, 64, 126, 147, 68, 15, 31, 66, 98, 138, 73, 108, 56, 14, 83, 113, 80, 114, 78, 34, 23,
            93, 76, 115, 64, 60,
            16, 24, 55, 70, 41, 94, 77, 47, 16, 42, 93, 88, 101, 110, 15, 8, 121, 115, 53, 5, 42, 11,
            14, 92, 158, 94, 130,
            108, 6, 27, 90, 59, 90, 219, 38, 13, 19, 96, 104, 84, 215, 115, 19, 40, 114, 148, 68, 113,
            83, 7, 7, 72, 7, 57,
            110, 131, 19, 21, 49, 8, 163, 205, 230, 15, 18, 270, 340, 342, 344, 271, 18, 13, 184, 230,
            276, 403, 199, 10,
            23, 10, 221, 171, 248, 320, 13, 8, 409, 320, 419, 404, 161, 30, 27, 241, 252, 184, 247, 283,
            71, 85, 535, 296,
            383, 316, 400, 62, 55, 226, 363, 408, 426, 203, 72, 60, 284, 326, 288, 439, 351, 135, 102,
            186, 236, 277, 588,
            396, 129, 167, 621, 648, 464, 444, 605, 162, 143, 719, 561, 690, 517, 383, 96, 92, 526, 437,
            555, 467, 428,
            115, 100, 463, 592, 639, 264, 1205, 884, 503, 627, 634, 348, 382, 458, 127, 158, 458, 382,
            321, 257, 431,
            231, 164, 659, 496, 549, 581, 443, 153, 144, 499, 473, 405, 433, 304, 127, 128, 316, 311,
            474, 348, 399, 174,
            85, 288, 327, 340, 276, 232, 117, 79, 269, 258, 293, 274, 239, 76, 33, 58, 313, 217, 223,
            240, 69, 63, 358, 284,
            322, 226, 199, 53, 88, 269, 315, 272, 229, 198, 41, 36, 265, 269, 241, 355, 196, 62, 58,
            260, 210, 199, 191,
            183, 51, 37, 202, 167, 155, 30, 153, 63, 38, 207, 167, 180, 187, 231, 43, 45, 193, 196, 219,
            139, 111, 57, 47,
            116, 209, 101, 196, 124, 56, 40, 192, 180, 185, 132, 144, 33, 27, 229, 182, 179, 141, 155,
            22, 42, 195, 175,
            149, 181, 97, 23, 33, 181, 115, 133, 134, 280, 25, 20, 116, 143, 109, 94, 92, 39, 36, 29,
            141, 134, 114, 100,
            22, 28, 128, 118, 114, 124, 110, 29, 22, 103, 92, 99, 111, 85, 20, 27, 136, 121, 142, 147,
            77, 21, 9, 109, 95,
            119, 120, 67, 15, 24, 136, 120, 149, 110, 107, 17, 12, 43, 73, 81, 92, 74, 18, 20, 122, 92,
            72, 79, 62, 10, 7, 64,
            126, 77, 83, 58, 13, 19, 78, 70, 89, 86, 65, 13, 14, 47, 87, 94, 152, 70, 10, 11, 96, 123,
            106, 102, 62, 14, 9, 104,
            189, 58, 6, 44, 16, 43, 123, 117, 168, 138, 143, 12, 20, 66, 127, 83, 127, 154, 10, 15, 222,
            194, 239, 309, 329,
            47, 68, 398, 175, 12, 214, 147, 51, 47, 131, 165, 20, 253, 327, 70, 23, 96, 230, 422, 383,
            592, 886, 709, 1873,
            1438, 1158, 1055, 1080, 390, 314, 525, 545, 531, 658, 559, 221, 208, 424, 385, 524, 494,
            455, 130, 207, 760,
            708, 357, 614, 529, 208, 268, 652, 555, 538, 578, 482, 177, 148, 285, 590, 995, 1027, 1143,
            990, 1051, 1680,
            1377, 1636, 1190, 1037, 338, 303, 619, 1192, 761, 1218, 1104, 548, 527, 1997, 1581, 678,
            1044, 1022, 512,
            384, 1005, 892, 1219, 974, 1069, 421, 317, 989, 819, 700, 890, 689, 387, 301, 919, 899, 788,
            857, 850, 335,
            395, 1288, 1030, 751, 960, 723, 278, 254, 705, 662, 805, 716, 430, 246, 164, 553, 598, 588,
            662, 512,
            302, 293, 562, 745, 773, 639, 606, 140, 195, 526, 636, 616, 676, 484, 187, 119, 499, 645,
            507, 524, 431,
            189, 176, 546, 531, 652, 495, 477, 166, 123, 115, 716, 547, 596, 460, 188, 148, 433, 539,
            463, 502, 374,
            184, 117, 446, 483, 519, 494, 501, 162, 121, 435, 518, 596, 549, 561, 226, 132, 560, 449,
            358, 396, 398,
            119, 126, 377, 359, 386, 373, 57, 100, 117, 353, 381, 452, 399, 331, 201, 128, 374, 389,
            384, 426, 282, 143,
            93, 290, 350, 347, 371, 305, 121, 89, 343, 330, 327, 275, 202, 105, 61, 272, 351, 354, 305,
            267, 128, 65, 268,
            255, 279, 241, 210, 64, 64, 243, 293, 199, 191, 169, 53, 53, 220, 259, 190, 263, 148, 46,
            68, 50, 245, 211, 204,
            208, 90, 61, 232, 178, 160, 230, 193, 49, 57, 236, 192, 191, 222, 189, 72, 57, 197, 165,
            315, 153, 196, 57, 69,
            181, 155, 162, 156, 153, 63, 34, 170, 201, 201, 167, 152, 114, 27, 57, 172, 186, 148, 133,
            66, 42, 138, 193, 160,
            128, 130, 45, 31, 120, 90, 148, 109, 88, 39, 28, 97, 78, 87, 76, 87, 23, 21, 101, 54, 74,
            98, 65, 26, 13, 56, 58, 54,
            73, 68, 18, 12, 64, 88, 74, 16, 39, 36, 18, 108, 109, 105, 96, 101, 24, 23, 107, 106, 111,
            109, 123, 33, 32, 91, 113,
            112, 65, 43, 17, 16, 112, 123, 51, 14, 50, 24, 22, 101, 138, 61, 13, 84, 31, 19, 156, 102,
            76, 67, 105, 27, 17, 339,
            433, 369, 328, 378, 39, 34, 62, 486, 331, 338, 226, 57, 28, 369, 379, 331, 252, 237, 26, 13,
            58, 89, 215, 309, 301,
            112, 41, 413, 609, 395, 297, 494, 19, 102, 416, 731, 460, 234, 678, 10, 46, 410, 259, 432,
            152, 105, 25, 34, 47, 373,
            246, 317, 279, 38, 65, 648, 504, 612, 514, 330, 61, 96, 578, 429, 308, 290, 374, 91, 243,
            249, 250, 345, 485, 214,
            209, 236, 441, 247, 297, 258, 383, 201, 26, 240, 345, 292, 289, 365, 61, 107, 456, 318, 262,
            239, 236, 73, 75, 323,
            318, 306, 308, 162, 87, 47, 241, 208, 296, 284, 201, 42, 44, 252, 258, 276, 259, 268, 81,
            82, 230, 305, 306, 295,
            184, 123, 57, 191, 231, 210, 254, 174, 74, 33, 56, 231, 213, 194, 155, 59, 36, 256, 242,
            243, 186, 182, 29, 57, 204,
            228, 169, 195, 185, 63, 86, 151, 192, 232, 214, 191, 59, 77, 263, 235, 224, 225, 170, 71,
            67, 270, 244, 188, 186,
            73, 60, 51, 187, 213, 279, 223, 168, 64, 48, 192, 255, 187, 289, 232, 65, 49, 191, 272, 254,
            192, 235, 45, 51, 201,
            188, 215, 149, 128, 41, 57, 183, 147, 170, 142, 105, 41, 52, 152, 154, 178, 160, 141, 47,
            29, 212, 150, 151, 143,
            135, 23, 30, 131, 141, 128, 157, 180, 42, 44, 145, 90, 138, 132, 235, 29, 25, 35, 114, 190,
            137, 134, 48, 27, 159,
            142, 132, 129, 110, 59, 37, 155, 147, 145, 146, 173, 45, 40, 140, 133, 132, 165, 102, 59, 9,
            109, 133, 132, 118,
            171, 44, 31, 53, 85, 162, 99, 96, 22, 40, 115, 109, 78, 114, 84, 12, 24, 136, 72, 109, 115,
            68, 32, 29, 116, 171, 90,
            131, 105, 28, 30, 116, 116, 76, 100, 78, 27, 26, 92, 108, 135, 94, 71, 20, 20, 121, 64, 69,
            20, 47, 31, 47, 152, 101,
            145, 87, 99, 46, 38, 100, 109, 136, 86, 99, 43, 38, 137, 142, 166, 126, 103, 52, 48, 106,
            130, 92, 60, 11, 40, 35, 88,
            127, 95, 94, 28, 55, 45, 113, 161, 195, 105, 135, 80, 60, 154, 306, 160, 188, 168, 26, 34,
            45, 140, 193, 100, 175,
            33, 22, 179, 151, 200, 210, 132, 41, 54, 175, 237, 221, 259, 300, 49, 64, 444, 203, 268,
            139, 201, 26, 21, 136, 305,
            312, 240, 182, 42, 45, 459, 192, 205, 239, 184, 35, 41, 291, 230, 200, 240, 190, 50, 47, 71,
            266, 298, 324, 180, 62,
            31, 335, 466, 275, 305, 240, 73, 53, 303, 438, 313, 139, 207, 67, 76, 353, 324, 254, 317,
            250, 99, 86, 355, 713, 323,
            319, 244, 67, 86, 334, 368, 361, 482, 228, 48, 66, 223, 232, 268, 216, 198, 81, 62, 234,
            240, 371, 272, 252, 47, 160,
            216, 288, 257, 238, 291, 64, 100, 204, 278, 303, 319, 260, 94, 81, 323, 366, 325, 290, 297,
            80, 61, 207, 296, 254,
            292, 221, 89, 55, 54, 229, 274, 408, 188, 68, 79, 250, 240, 204, 251, 191, 117, 68, 206,
            249, 211, 200, 256, 92, 46,
            247, 167, 180, 222, 198, 61, 60, 205, 188, 260, 175, 133, 33, 26, 34, 136, 177, 161, 152,
            45, 57, 195, 200, 158, 144,
            175, 53, 29, 193, 162, 130, 136, 109, 43, 38, 167, 204, 214, 147, 125, 38, 32, 148, 178, 99,
            116, 106, 45, 28, 103,
            123, 143, 130, 118, 31, 48, 142, 191, 287, 103, 97, 33, 24, 90, 120, 120, 152, 109, 71, 36,
            133, 95, 117, 104, 105,
            30, 33, 47, 112, 135, 109, 127, 48, 38, 134, 125, 124, 108, 96, 39, 31, 70, 105, 81, 93, 66,
            22, 22, 136, 96, 95, 92,
            110, 58, 33, 94, 100, 107, 89, 107, 26, 16, 48, 137, 96, 112, 103, 55, 35, 103, 89, 102,
            100, 85, 34, 34, 138, 92, 84,
            91, 71, 30, 34, 71, 103, 77, 89, 55, 31};

    // some synthetic data to be used for testing
    public static double[] simple_data1_val = new double[]{2, 1, 2, 5, 2, 1, 2, 5, 2, 1, 2, 5, 2, 1,
            2, 5, 2, 1, 2, 5, 2, 1, 2, 5, 2, 1, 2, 5, 2, 1, 2, 5, 2};
    public static double[] simple_data2_val = new double[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2};
    public static double[] simple_data3_val = new double[]{0.984, 1.012, 1.045, 1.109, 1.112, 1.458,
            1.892, 2.018, 2.203, 2.43, 1.897, 0.894, 1.02,
            1.082, 1.109, 1.201, 1.254, 1.562, 2.093, 2.104, 2.540, 2.893, 1.990, 1.035, 1.309, 1.403,
            1.498, 1.73, 1.789, 1.897, 2.309, 2.543, 2.783, 3.093, 2.345, 1.473, 1.234, 1.320, 1.293,
            1.523, 1.432, 1.435, 1.987, 2.235, 2.453, 2.765, 2.097, 1.291, 1.192, 1.109, 1.146, 1.356,
            1.472, 1.592, 2.395, 2.483, 2.698, 2.895, 2.197, 1.303};
    public static double[] simple_data3_answer = new double[]{1.435, 1.532, 1.563, 1.436, 1.578,
            1.799, 2.538};

    /**
     * austa Total international visitors to Australia. 1980-2010. (millions) no seasonal, strong
     * trend with a bit random
     * [Source] https://rdrr.io/cran/fpp/man/austa.html
     */
    public static HashMap<String, Double> austa() {
        HashMap<String, Double> data = new HashMap<String, Double>();
        String[] keys = {"1980", "1981", "1982", "1983", "1984", "1985", "1986", "1987", "1988",
                "1989", "1990", "1991", "1992",
                "1993", "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003",
                "2004", "2005",
                "2006", "2007", "2008"};

        Double[] values = {0.8298943, 0.8595109, 0.8766892, 0.8667072, 0.9320520, 1.0482636,
                1.3111932, 1.6375623, 2.0641074,
                1.9126828, 2.0354457, 2.1772113, 2.3896834, 2.7505921, 3.0906664, 3.4266403, 3.8306491,
                3.9719086,
                3.8316004, 4.1431010, 4.5665510, 4.4754100, 4.4627960, 4.3848290, 4.7968610, 5.0462110,
                5.0987590,
                5.1965190, 5.1668430, 5.1747440, 5.4408940};

        for (int i = 0; i < keys.length; i++) {
            data.put(keys[i], values[i]);
        }

        return data;
    }

    /**
     * Question from cscchris
     */
    public static double[] cscchris_val = new double[]{
            2674.8060304978917, 3371.1788109723193, 2657.161969121835, 2814.5583226655367, 3290.855749923403, 3103.622791045206, 3403.2011487950185, 2841.438925235243, 2995.312700153925, 3256.4042898633224, 2609.8702933486843, 3214.6409110870877, 2952.1736018157644, 3468.7045537306344, 3260.9227206904898, 2645.5024256492215, 3137.857549381811, 3311.3526531674556, 2929.7762119375716, 2846.05991810631, 2606.47822546165, 3174.9770937667918, 3140.910443979614, 2590.6601484185085, 3123.4299821259915, 2714.4060964141136, 3133.9561758319487, 2951.3288157912752, 2860.3114228342765, 2757.4279640677833};
    public static double[] cscchris_answer = new double[]{
            3147.816496825682, 3418.2300802476093, 2856.905414401418, 3419.0312162705545, 3307.9803365878442, 3527.68377555284};
}