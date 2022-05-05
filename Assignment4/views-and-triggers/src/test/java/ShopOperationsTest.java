import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import shop.AdminShopOperations;
import shop.AdminShopOperationsImpl;
import shop.UserShopOperations;
import shop.UserShopOperationsImpl;


public class ShopOperationsTest {
  // the port is specified because of multiple local version of postgresql
  private static final String BASE_URL = "jdbc:postgresql://localhost:5432/";
  private static final String SHOP_URL = BASE_URL + "shop";
  private static final String EMILIE = "emilie";

  @Test
  public void testScenario() throws SQLException, IOException {
    try (AdminShopOperations op = new AdminShopOperationsImpl(BASE_URL, "admin", "admin")) {
      op.createShopDatabase();
    }

    try (AdminShopOperations op = new AdminShopOperationsImpl(SHOP_URL, "admin", "admin")) {
      op.createTables();

      op.populateTables();

      op.createUsers();
      assertPaulHasNoAccess();

      op.createViewHistory();
      assertEmilieSeesHistory();

      op.createFunctionNewPurchase();
      assertEmilieCanPurchaseToner();

      op.createRuleDeleteHistory();
      assertEmilieCanCancelPurchase(op);
    }
  }

  private void assertEmilieSeesHistory() throws IOException, SQLException {
    final String correctResult = "304 2014-08-01 Toner_135 5 135\n"
        + "184 2014-05-20 Toner_259 7 336\n"
        + "120 2014-03-25 Toner_216 2 82\n"
        + "54 2014-02-09 Toner_159 6 282\n";
    String currentResult = "";
    try (UserShopOperations op = new UserShopOperationsImpl(SHOP_URL, EMILIE, EMILIE)) {
      try (ResultSet res = op.selectHistory()) {
        currentResult = this.getHistory(res, true);
      }
      assertEquals(correctResult, currentResult);
    }

  }

  private void assertPaulHasNoAccess() throws SQLException, IOException {
    try (AdminShopOperations op = new AdminShopOperationsImpl(SHOP_URL, "paul", "paul")) {
      String error = "ERROR: permission denied for table ";
      final SQLException sqlExceptionCustomer =
          assertThrows(SQLException.class, op::selectCustomerName);
      assertEquals(error + "customer", sqlExceptionCustomer.getMessage());
      final SQLException sqlExceptionArticle =
          assertThrows(SQLException.class, op::selectArticleName);
      assertEquals(error + "article", sqlExceptionArticle.getMessage());
      final SQLException sqlExceptionPurchase =
          assertThrows(SQLException.class, op::selectPurchaseId);
      assertEquals(error + "purchase", sqlExceptionPurchase.getMessage());
    }
  }

  private void assertEmilieCanPurchaseToner() throws SQLException, IOException {
    final String correctResult = LocalDate.now() + " Toner_216 10 410\n";
    try (UserShopOperations op = new UserShopOperationsImpl(SHOP_URL, EMILIE, EMILIE)) {
      assertTrue(op.newPurchase("Toner_216", 10));
      assertFalse(op.newPurchase("Toner_159", 2));
      ResultSet res = op.selectHistoryToday();
      String currentResult = this.getHistory(res, false);
      assertEquals(correctResult, currentResult);
    }
  }

  private void assertEmilieCanCancelPurchase(AdminShopOperations adminOp)
      throws IOException, SQLException {
    final int originalBalance = 486;
    final String originalPurchase = LocalDate.now() + " Toner_216 10 410\n";
    try (UserShopOperations op = new UserShopOperationsImpl(SHOP_URL, EMILIE, EMILIE)) {
      final int originalPurchaseId = this.getId(op.selectHistoryToday(), originalPurchase);
      op.cancelPurchase("Toner_216");
      // check if the result set of today history is empty
      try (ResultSet res = op.selectHistoryToday()) {
        assertFalse(res.next());
      }
      // check if the purchase was deleted from purchases
      try (ResultSet res = adminOp.selectPurchaseId()) {
        while (res.next()) {
          assertNotEquals(originalPurchaseId, res.getInt(1));
        }
      }
      // check if the balance is reset
      assertEquals(originalBalance, adminOp.getBalance(EMILIE));
    }
  }

  private String getHistory(ResultSet res, boolean withId) throws SQLException {
    String currentResult = "";
    while (res.next()) {
      if (withId) {
        currentResult += res.getInt("id") + " ";
      }
      currentResult += res.getDate("date").toString() + " "
          + res.getString("article") + " "
          + res.getInt("quantity") + " "
          + res.getInt("price") + "\n";
    }
    return currentResult;
  }

  private int getId(ResultSet res, String purchase) throws SQLException {
    int id = -1;
    while (res.next()) {
      String currentPurchase = res.getDate("date").toString() + " "
          + res.getString("article") + " "
          + res.getInt("quantity") + " "
          + res.getInt("price") + "\n";
      if (purchase.equals(currentPurchase)) {
        id = res.getInt("id");
      }
    }
    return id;
  }
}
