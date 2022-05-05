package shop;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public class AdminShopOperationsImpl extends AdminShopOperations {

  public AdminShopOperationsImpl(String url, String user, String password) throws SQLException {
    super(url, user, password);
  }

  @Override
  public void createShopDatabase() throws SQLException, IOException {
    PreparedStatement preparedStatement = this.prepareStatement("create_database");
    preparedStatement.executeUpdate();

  }

  @Override
  public void createTables() throws SQLException, IOException {
    PreparedStatement preparedStatement = this.prepareStatement("create_tables");
    preparedStatement.executeUpdate();
  }

  @Override
  public void populateTables() throws SQLException, IOException {
    CopyManager copyManager = new CopyManager((BaseConnection) this.conn);
    InputStream customerStream = ShopResource.getData("customer");
    copyManager.copyIn("COPY CUSTOMER FROM STDIN", customerStream);

    InputStream articleStream = ShopResource.getData("article");
    copyManager.copyIn("COPY ARTICLE FROM STDIN", articleStream);

    InputStream purchaseStream = ShopResource.getData("purchase");
    copyManager.copyIn("COPY PURCHASE FROM STDIN", purchaseStream);
  }

  @Override
  public void createUsers() throws SQLException, IOException {
    PreparedStatement createUsers = this.prepareStatement("create_users");
    createUsers.executeUpdate();

    PreparedStatement selectAllCustomersStatement =
        this.prepareSelfClosingStatement("select_all_customers");
    Statement createUserStatement = this.conn.createStatement();
    try (ResultSet res = selectAllCustomersStatement.executeQuery()) {
      while (res.next()) {
        String customerName = res.getString("name");
        String query = "DROP ROLE IF EXISTS " + customerName + ";"
            + "CREATE ROLE " + customerName
            + " WITH LOGIN PASSWORD '" + customerName + "' IN ROLE buyer;\n";
        createUserStatement.executeUpdate(query);
      }
    }
  }

  @Override
  public void createViewHistory() throws SQLException, IOException {
    PreparedStatement createViewStatement = this.prepareStatement("create_view_history");
    createViewStatement.executeUpdate();
  }

  @Override
  public void createFunctionNewPurchase() throws SQLException, IOException {
    PreparedStatement preparedStatement = this.prepareStatement("create_function_new_purchase");
    preparedStatement.executeUpdate();
  }

  @Override
  public void createRuleDeleteHistory() throws SQLException, IOException {
    PreparedStatement preparedStatement = this.prepareStatement("create_delete_rule");
    preparedStatement.executeUpdate();
  }

  @Override
  public int getBalance(String ofUser) throws SQLException, IOException {
    PreparedStatement getBalanceStatement = this.prepareSelfClosingStatement("get_balance");
    getBalanceStatement.setString(1, ofUser);
    try (ResultSet res = getBalanceStatement.executeQuery()) {
      if (res.next()) {
        return res.getInt("balance");
      }
    } catch (SQLException sqlException) {
      throw sqlException;
    }
    return -1;
  }

  @Override
  public ResultSet selectCustomerName() throws SQLException, IOException {
    PreparedStatement selectStatement = this.prepareSelfClosingStatement("select_from_customer");
    return selectStatement.executeQuery();
  }

  @Override
  public ResultSet selectArticleName() throws SQLException, IOException {
    PreparedStatement selectStatement = this.prepareSelfClosingStatement("select_from_article");
    return selectStatement.executeQuery();
  }

  @Override
  public ResultSet selectPurchaseId() throws SQLException, IOException {
    PreparedStatement selectStatement = this.prepareSelfClosingStatement("select_from_purchase");
    return selectStatement.executeQuery();
  }
}
