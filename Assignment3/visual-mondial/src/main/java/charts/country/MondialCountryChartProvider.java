package charts.country;

import charts.ChartUpdateException;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import postgres.DbConnector;

public class MondialCountryChartProvider implements CountryChartProvider {
  private DbConnector dbConnector;
  //private DefaultCategoryDataset dsLanguagesChart = new DefaultCategoryDataset(); // new DefaultPieDataset( );
  private DefaultPieDataset dsLanguagesChart = new DefaultPieDataset();
  private DefaultPieDataset dsReligionsChart = new DefaultPieDataset();
  private DefaultCategoryDataset dsCitiesChart = new DefaultCategoryDataset();

  public MondialCountryChartProvider(DbConnector dbConnector) {
    this.dbConnector = dbConnector;
  }

  @Override
  public void update(String countryName) throws ChartUpdateException {
    String query = "SELECT mondial.language.name, mondial.language.percentage " +
        "FROM mondial.language INNER JOIN mondial.country on mondial.language.country = mondial.country.code " +
        "WHERE mondial.country.name = ?";
    String query2 = "SELECT mondial.religion.name, mondial.religion.percentage " +
        "FROM mondial.religion INNER JOIN mondial.country on mondial.religion.country = mondial.country.code " +
        "WHERE mondial.country.name LIKE ?";
    String query3 =
        "SELECT mondial.city.name, mondial.city.population, CASE WHEN mondial.city.name = mondial.country.capital THEN TRUE ELSE FALSE END AS is_capital " +
            "FROM mondial.city INNER JOIN mondial.country on mondial.city.country = mondial.country.code " +
            "WHERE mondial.country.name LIKE ? " +
            "ORDER BY mondial.city.population DESC " +
            "LIMIT 10";
    try (Connection con = dbConnector.getConnection()) {
      this.dsLanguagesChart.clear();
      try (PreparedStatement stmt = con.prepareStatement(query)) {
        stmt.setString(1, countryName);
        try (ResultSet rs = stmt.executeQuery();) {
          while (rs.next()) {
            this.dsLanguagesChart.setValue(
                rs.getString("name"),
                Double.parseDouble(rs.getString("percentage"))
                //this.countryName,
                //rs.getString("name")
            );
          }
        }
      }

      this.dsReligionsChart.clear();
      try (PreparedStatement stmt2 = con.prepareStatement(query2)) {
        stmt2.setString(1, countryName);
        try (ResultSet rs = stmt2.executeQuery();) {
          while (rs.next()) {
            this.dsReligionsChart.setValue(
                rs.getString("name"),
                Double.parseDouble(rs.getString("percentage"))
            );
          }
        }
      }

      this.dsCitiesChart.clear();
      try (PreparedStatement stmt3 = con.prepareStatement(query3)) {
        stmt3.setString(1, countryName);
        try (ResultSet rs = stmt3.executeQuery();) {
          while (rs.next()) {
            String name = rs.getString("name");
            // highlight the capital ("Bonuspunkte")
            if (rs.getBoolean("is_capital")) {
              name = name.toUpperCase() + "(capital)";
            }
            this.dsCitiesChart.setValue(
                Integer.parseInt(rs.getString("population")),
                countryName,
                name
            );
          }
        }
      }

    } catch (SQLException sqlException) {
      throw new ChartUpdateException(sqlException.getMessage());
    }

  }

  @Override
  public JFreeChart getLanguagesChart() {
    JFreeChart chart =
        ChartFactory.createPieChart("Languages Chart", this.dsLanguagesChart, true, true, false);
    final PiePlot plot = (org.jfree.chart.plot.PiePlot) chart.getPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setCircular(true);
    NumberFormat defaultFormat = NumberFormat.getPercentInstance();
    defaultFormat.setMinimumFractionDigits(1);
    plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
        "{0} = {2}", NumberFormat.getNumberInstance(), defaultFormat
    ));
    return chart;
  }

  @Override
  public JFreeChart getReligionsChart() {
    return ChartFactory.createPieChart("Religions Chart",
        this.dsReligionsChart,
        true,
        true,
        true);
  }

  @Override
  public JFreeChart getCitiesChart() {
    return ChartFactory.createBarChart("Cities Chart",
        "City",
        "Population",
        this.dsCitiesChart,
        PlotOrientation.HORIZONTAL,
        false,
        false,
        false);
  }
}
