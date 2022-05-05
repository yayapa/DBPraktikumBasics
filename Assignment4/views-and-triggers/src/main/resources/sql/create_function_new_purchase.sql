CREATE OR REPLACE FUNCTION new_purchase(articlename text, quantity INTEGER) RETURNS boolean AS
$$
DECLARE
    actualprice    int;
    currentbalance int;
    totalprice     int;
BEGIN
    SELECT price INTO actualprice FROM article WHERE article = articlename;
    SELECT balance INTO currentbalance FROM customer WHERE name = (SELECT SESSION_USER);
    totalprice = actualprice * quantity;

    IF (totalprice > currentbalance) THEN
        RETURN FALSE;
    ELSE
        INSERT INTO purchase (id, customer, date, article, quantity)
        VALUES ((SELECT MAX(id) FROM purchase) + 1, SESSION_USER, CURRENT_DATE, articlename,
                quantity); --DEFAULT does not work: commented solutions also

        UPDATE customer SET balance = currentbalance - totalprice WHERE name = (SELECT SESSION_USER);

        RETURN TRUE;
    END IF;
END
$$ LANGUAGE plpgsql
SECURITY DEFINER;