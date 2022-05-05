package charts.runtime;

import charts.ChartUpdateException;
import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import postgres.DbConnector;
import stats.ConfidenceInterval;

public class PostgresRuntimesChartProvider implements RuntimesChartProvider {
  private static final String[] CATEGORIES = {"Query 1", "Query 2", "Diff"};
  double[][] starts = new double[1][3];
  double[][] ends = new double[1][3];
  private DbConnector dbConnector;
  private DefaultIntervalCategoryDataset dsRuntimesChart =
      new DefaultIntervalCategoryDataset(starts, ends);

  public PostgresRuntimesChartProvider(DbConnector dbConnector) {
    this.dbConnector = dbConnector;
  }

  @Override
  public JFreeChart getRuntimesChart() {
    dsRuntimesChart.setCategoryKeys(CATEGORIES);
    // my cosmetic better version ("Bonuspunkte")
    IntervalBarRenderer renderer = new IntervalBarRenderer();
    renderer.setSeriesPaint(0, new Color(51, 102, 153));
    CategoryPlot plot = new CategoryPlot(dsRuntimesChart,
        new CategoryAxis("Measurement Type"), new NumberAxis("Time [ms]"),
        renderer);
    plot.setOutlinePaint(Color.white);
    plot.setOrientation(
        PlotOrientation.HORIZONTAL); // comment to get the similar version as in assignment
    JFreeChart chart = new JFreeChart("Comparison of Execution Times", plot);
    chart.setBackgroundPaint(Color.lightGray);
    return chart;
  }

  @Override
  public void update(String query1, String query2, int numObservations, double conf)
      throws ChartUpdateException {
    double[] values1 = new double[numObservations];
    double[] values2 = new double[numObservations];

    try (Connection con = dbConnector.getConnection()) {
      Statement stmt = con.createStatement();
      values1 = measure(stmt, numObservations, query1);
      values2 = measure(stmt, numObservations, query2);
    } catch (SQLException sqlException) {

    }
    ConfidenceInterval ci1 = ConfidenceInterval.forMean(values1, conf);
    ConfidenceInterval ci2 = ConfidenceInterval.forMean(values2, conf);
    ConfidenceInterval ci12 = ConfidenceInterval.forMeanDifference(values1, values2, conf);

    starts[0][0] = ci1.getLower();
    starts[0][1] = ci2.getLower();
    starts[0][2] = ci12.getLower();
    ends[0][0] = ci1.getUpper();
    ends[0][1] = ci2.getUpper();
    ends[0][2] = ci12.getUpper();

    dsRuntimesChart.setStartValue(0, "Query 1", starts[0][0]);
    dsRuntimesChart.setStartValue(0, "Query 2", starts[0][1]);
    dsRuntimesChart.setStartValue(0, "Diff", starts[0][2]);
    dsRuntimesChart.setEndValue(0, "Query 1", ends[0][0]);
    dsRuntimesChart.setEndValue(0, "Query 2", ends[0][1]);
    dsRuntimesChart.setEndValue(0, "Diff", ends[0][2]);

    // print the values for analysis
    for (int i = 0; i < starts[0].length; i++) {
      System.out.println(CATEGORIES[i] + ": " + starts[0][i] + " " + ends[0][i]);
    }
  }

  private double[] measure(Statement stmt, int numObservations, String query) throws SQLException {
    String explainQuery = "EXPLAIN ANALYZE " + query;
    double[] values = new double[numObservations];
    for (int i = 0; i < numObservations; i++) {
      try (ResultSet rs = stmt.executeQuery(explainQuery);) {
        while (rs.next()) {
          String row = rs.getString(1);
          String[] rowSplit = row.split("\\s+");
          if (rowSplit[0].equals("Execution") && rowSplit[1].equals("Time:")) {
            values[i] = Double.parseDouble(rowSplit[2]);
          }
        }
      } catch (SQLException sqlException) {
        throw sqlException;
      }
    }
    return values;
  }
}
