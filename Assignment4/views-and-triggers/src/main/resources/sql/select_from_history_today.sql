SELECT *
  FROM history
 WHERE date = (SELECT CURRENT_DATE);