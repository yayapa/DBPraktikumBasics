DROP ROLE IF EXISTS buyer;
CREATE ROLE buyer CONNECTION LIMIT 100;
REVOKE ALL PRIVILEGES ON customer, article, purchase FROM buyer;