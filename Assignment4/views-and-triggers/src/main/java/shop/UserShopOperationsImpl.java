package shop;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserShopOperationsImpl extends UserShopOperations {
  public UserShopOperationsImpl(String url, String user, String password) throws SQLException {
    super(url, user, password);
  }

  @Override
  public boolean newPurchase(String article, int quantity) throws SQLException, IOException {
    PreparedStatement newPurchaseStatement = this.prepareSelfClosingStatement("new_purchase");
    newPurchaseStatement.setString(1, article);
    newPurchaseStatement.setInt(2, quantity);
    boolean answer = false;
    try (ResultSet res = newPurchaseStatement.executeQuery()) {
      if (res.next()) {
        answer = res.getBoolean("result");
      }
    } catch (SQLException sqlException) {
      throw sqlException;
    }
    return answer;
  }

  @Override
  public void cancelPurchase(String article) throws SQLException, IOException {
    PreparedStatement deleteStatement = this.prepareStatement("delete_from_history");
    deleteStatement.setString(1, article);
    deleteStatement.executeUpdate();
  }

  @Override
  public ResultSet selectHistory() throws SQLException, IOException {
    PreparedStatement selectStatement = this.prepareSelfClosingStatement("select_from_history");
    return selectStatement.executeQuery();
  }

  @Override
  public ResultSet selectHistoryToday() throws SQLException, IOException {
    PreparedStatement selectStatement =
        this.prepareSelfClosingStatement("select_from_history_today");
    return selectStatement.executeQuery();
  }
}
