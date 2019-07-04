/***********************************************************************************************************************
 Copyright (c) Damak Mahdi.
 Github.com/damakmahdi
 damakmahdi2012@gmail.com
 linkedin.com/in/mahdi-damak-400a3b14a/
 **********************************************************************************************************************/

import avro.shaded.com.google.common.collect.ImmutableList;
import avro.shaded.com.google.common.collect.ImmutableMap;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.SparkConf;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.classification.LogisticRegressionSummary;
import org.apache.spark.ml.clustering.BisectingKMeans;
import org.apache.spark.ml.clustering.BisectingKMeansModel;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.evaluation.ClusteringEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.stat.Correlation;
import org.apache.spark.ml.stat.Summarizer;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;
import org.elasticsearch.spark.sql.api.java.JavaEsSparkSQL;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
public class SparkTest {


    public static float round(float number) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    @Test
    public void summarizer() {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
        SparkSession spark = SparkSession
                .builder()
                .appName("Spark shell")
                .master("local[*]")
                .getOrCreate();

        SQLContext sql = new SQLContext(spark);
        Dataset<Row> ardg = JavaEsSparkSQL.esDF(sql, "ardgetti").sort("timestamp").select("value");
        Dataset<Row> peak = JavaEsSparkSQL.esDF(sql, "peaktech").sort("timestamp").select("value");
        ardg.show();
        peak.show();
        List<Double> ardgList = ardg.as(Encoders.DOUBLE()).collectAsList().subList((int) ardg.count() - 30, (int) ardg.count());
        List<Double> peakList = peak.as(Encoders.DOUBLE()).collectAsList().subList((int) peak.count() - 30, (int) peak.count());
        int i = 0;
        double[] ardgArray = new double[ardgList.size()];
        for (Double d : ardgList) {
            ardgArray[i] = d;
            i++;
        }
        int j = 0;
        double[] peakArray = new double[peakList.size()];
        for (Double d : peakList) {
            peakArray[j] = d;
            j++;
        }

        List<Row> test = Arrays.asList(
                RowFactory.create((Vectors.dense(ardgArray)))
                //  RowFactory.create(Vectors.dense(peakArray))
        );

        StructType schema = new StructType(new StructField[]{
                new StructField("value", new VectorUDT(), false, Metadata.empty())
        });

        Dataset<Row> df = spark.createDataFrame(test, schema);
        Row result1 = df.select(
                Summarizer.mean(new Column("value")),
                Summarizer.variance(new Column("value"))
        ).first();
        System.out.println("without weight: mean = " + result1.<Vector>getAs(0).toString() +
                ", variance = " + result1.<Vector>getAs(1).toString());
    }

    @Test
    public void correlation() {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
        SparkSession spark = SparkSession
                .builder()
                .appName("Spark shell")
                .master("local[*]")
                .getOrCreate();

        SQLContext sql = new SQLContext(spark);
        Dataset<Row> ardg = JavaEsSparkSQL.esDF(sql, "ardgetti").sort("timestamp");
        Dataset<Row> peak = JavaEsSparkSQL.esDF(sql, "peaktech").sort("timestamp");
        //ardg.show();
        //peak.show();
        List<Double> ardgList = ardg.select("value").as(Encoders.DOUBLE()).collectAsList().subList((int) ardg.count() - 30, (int) ardg.count());
        List<Double> peakList = peak.select("value").as(Encoders.DOUBLE()).collectAsList().subList((int) peak.count() - 30, (int) peak.count());
        Double[] ard = ardgList.toArray(new Double[ardgList.size()]);
        Double[] pek = peakList.toArray(new Double[peakList.size()]);


        double[] ardgArray = ArrayUtils.toPrimitive(ard);
        double[] peakArray = ArrayUtils.toPrimitive(pek);
        List<Row> test = Arrays.asList(
                RowFactory.create((Vectors.dense(ardgArray))),
                RowFactory.create(Vectors.dense(peakArray))
        );
        StructType schema = new StructType(new StructField[]{
                new StructField("value", new VectorUDT(), false, Metadata.empty()),
        });

        Dataset<Row> df = spark.createDataFrame(test, schema);

        Row r1 = Correlation.corr(df, "value", "pearson").head();
        System.out.println("Pearson correlation matrix:\n" + r1.get(0).toString());

        Row r2 = Correlation.corr(df, "value", "spearman").head();
        System.out.println("Spearman correlation matrix:\n" + r2.get(0).toString());
        //ardg.stat().corr("timestamp","value");
    }

    @Test
    public void writeData() {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
        SparkConf conf = new SparkConf()
                .setAppName("Spark shell")
                .setMaster("local[*]");

        JavaSparkContext jsc = new JavaSparkContext(conf);
        Map<String, ?> numbers = ImmutableMap.of("one", 1, "two", 2);
        Map<String, ?> airports = ImmutableMap.of("OTP", "Otopeni", "SFO", "San Fran");

        JavaRDD<Map<String, ?>> javaRDD = jsc.parallelize(ImmutableList.of(numbers, airports));
        JavaEsSpark.saveToEs(javaRDD, "test");

    }

    @Test
    public void arimaAndSTATS() {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
        SparkSession spark = SparkSession
                .builder()
                .appName("Spark shell")
                .master("local[*]")
                .getOrCreate();
        SQLContext sql = new SQLContext(spark);

        Dataset<Row> ardg = JavaEsSparkSQL.esDF(sql, "ardgetti").select("value").sort("timestamp");
        //ardg.describe("value").show();
        List<Double> ardgList = ardg.as(Encoders.DOUBLE()).collectAsList().subList(0, 500);
        Double[] ard = ardgList.toArray(new Double[ardgList.size()]);
        double[] ardgArray = ArrayUtils.toPrimitive(ard);

        ArimaTest at = new ArimaTest();
        // System.out.println("prediction starts now ");
        at.forecast(ardgArray);


    }

    @Test
    public void kMeans() {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
        SparkSession spark = SparkSession
                .builder()
                .appName("Spark shell")
                .master("local[*]")
                .getOrCreate();

        SQLContext sql = new SQLContext(spark);
        Dataset<Row> ds = JavaEsSparkSQL.esDF(sql, "ardgetti")
                .select("value")
                .sort("timestamp");

        // $example on$
        // Loads data.
        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{"value"})
                .setOutputCol("features");

        Dataset<Row> dataset = assembler.transform(ds);
        // Trains a k-means model.
        KMeans kmeans = new KMeans().setK(3).setSeed(1L);
        KMeansModel model = kmeans.fit(dataset);

        // Make predictions
        Dataset<Row> predictions = model.transform(dataset);
        predictions.show();
        // Evaluate clustering by computing Silhouette score
        ClusteringEvaluator evaluator = new ClusteringEvaluator();
        double silhouette = evaluator.evaluate(predictions);
        System.out.println("Silhouette with squared euclidean distance = " + silhouette);
        // Shows the result.
        Vector[] centers = model.clusterCenters();
        System.out.println("Cluster Centers: ");
        for (Vector center : centers) {
            System.out.println(center);
        }
        // $example off$

        spark.stop();
    }

    @Test
    public void logRegression() {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
        SparkSession spark = SparkSession
                .builder()
                .appName("JavaLogisticRegressionSummaryExample")
                .master("local[*]")
                .getOrCreate();

        // Load training data
        SQLContext sql = new SQLContext(spark);
        Dataset<Row> ds = JavaEsSparkSQL.esDF(sql, "peaktech")
                .sort("timestamp")
                .select("value");

        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{"value"})
                .setOutputCol("features");
        Dataset<Row> training = assembler.transform(ds);
        ds.show();

        LogisticRegression lr = new LogisticRegression()
                .setMaxIter(5)
                .setRegParam(0.3)
                .setLabelCol("value")
                .setElasticNetParam(0.9);

        // Fit the model
        LogisticRegressionModel lrModel = lr.fit(training);

        // $example on$
        // Extract the summary from the returned LogisticRegressionModel instance trained in the earlier
        // example
        LogisticRegressionSummary trainingSummary = lrModel.summary();
        trainingSummary.predictions().show(100);

        // Obtain the receiver-operating characteristic as a dataframe and areaUnderROC.
        System.out.println("accuracy " + trainingSummary.accuracy());
        spark.stop();
    }

    @Test
    public void bKmeans(){
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
        SparkSession spark = SparkSession
                .builder()
                .appName("JavaLogisticRegressionSummaryExample")
                .master("local[*]")
                .getOrCreate();

        // Load training data
        SQLContext sql = new SQLContext(spark);
        Dataset<Row> ds = JavaEsSparkSQL.esDF(sql, "ardgetti")
                .sort("timestamp")
                .select("value");

        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{"value"})
                .setOutputCol("features");
        Dataset<Row> dataset = assembler.transform(ds);
        ds.show();

        // Trains a bisecting k-means model.
        BisectingKMeans bkm = new BisectingKMeans().setK(2).setSeed(1);
        BisectingKMeansModel model = bkm.fit(dataset);

        // Make predictions
        Dataset<Row> predictions = model.transform(dataset);

        // Evaluate clustering by computing Silhouette score
        ClusteringEvaluator evaluator = new ClusteringEvaluator();

        double silhouette = evaluator.evaluate(predictions);
        System.out.println("Silhouette with squared euclidean distance = " + silhouette);

        // Shows the result.
        System.out.println("Cluster Centers: ");
        Vector[] centers = model.clusterCenters();
        for (Vector center : centers) {
            System.out.println(center);
        }
        // $example off$

        spark.stop();
    }

    @Test
    public void test(){
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
        SparkSession spark = SparkSession.builder().appName("test").master("local[*]").getOrCreate();
        SQLContext sql = new SQLContext(spark);
        Dataset<Row> ds = JavaEsSparkSQL.esDF(sql, "ardgetti");
        StructType schema = new StructType(new StructField[]{
                new StructField("timestamp", DataTypes.TimestampType, true, Metadata.empty()),
                new StructField("value", DataTypes.FloatType, true, Metadata.empty()),
                new StructField("allure", DataTypes.StringType, true, Metadata.empty()),
                new StructField("pic", DataTypes.BooleanType, true, Metadata.empty())
        });
        List<Row> rows = new ArrayList<>();
        Float previousValue=444F;
        boolean pic=false;
        String allure="decroissante";
        Float nextValue;
        List<Row> ar=ds.collectAsList();
        int i=0;
        int picNumber=0;
        int crNumber=0;
        int decNumber=0;
        int stagNumber=0;

        for (Row r : ds.collectAsList()){
            nextValue=round(ar.get(i+1).getFloat(1));
            if(round(r.getFloat(1))>=round(previousValue)) {
                allure="croissante";
                crNumber=crNumber+1;
                if(round(r.getFloat(1))>nextValue ) {
                    pic=true;
                    picNumber=picNumber+1;
                } }
            if(round(round(r.getFloat(1)))==round(previousValue)) {
                allure="stagnante";
                stagNumber=stagNumber+1;
            }
            rows.add(RowFactory.create(r.getAs("timestamp"), r.getAs("value"), allure,pic));
            previousValue=round(r.getFloat(1));
            pic=false;
            if (allure.equals("decroissante")){decNumber=decNumber+1;}
            allure="decroissante";
           if (ar.size()-2>i){
               i++;} }
        Dataset<Row> df = spark.createDataFrame(rows, schema);
        JavaEsSparkSQL.saveToEs(df,"test");
        df.show(10);
        df.describe("value").show();
        System.out.println("Le nombre de pics = "+picNumber);
        System.out.println("Le nombre de segments croissants = "+crNumber);
        System.out.println("Le nombre de segments decroissants = "+decNumber);
        System.out.println("Le nombre de segments stagnants = "+stagNumber);
        double p = picNumber*100  / df.count();
        double c = crNumber*100  / df.count();
        double d= decNumber*100  / df.count();
        double s = stagNumber*100  / df.count();
        System.out.println("Le % des pic = "+ p+"%");
        System.out.println("Le % des segments croissants = "+ c+"%");
        System.out.println("Le % des segments decroissants = "+ d+"%");
        System.out.println("Le % des segments stagnants = "+ s+"%");
        spark.stop();
    }

}


