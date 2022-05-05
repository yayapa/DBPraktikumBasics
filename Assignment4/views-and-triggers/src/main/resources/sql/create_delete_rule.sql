GRANT DELETE ON history TO buyer;
CREATE OR REPLACE RULE delete_history AS ON DELETE TO history DO INSTEAD ( UPDATE customer
                                                                              SET balance = balance + old.price
                                                                            WHERE name = (SELECT SESSION_USER); DELETE
                                                                                                                  FROM purchase
                                                                                                                 WHERE article = old.article
                                                                                                                   AND date = (SELECT CURRENT_DATE); );