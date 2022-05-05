CREATE OR REPLACE VIEW history
AS
SELECT id, date, article, quantity, (price * quantity) AS price
  FROM purchase
           NATURAL JOIN article
 WHERE customer LIKE (SELECT CURRENT_USER)
 ORDER BY id DESC;

GRANT SELECT ON TABLE history TO buyer;