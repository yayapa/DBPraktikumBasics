SET datestyle = dmy; --because local american version of ubuntu triggers the date format error

CREATE TABLE IF NOT EXISTS customer
    (
        name varchar(100) NOT NULL,
        balance integer,
        PRIMARY KEY (name)
    );

CREATE TABLE IF NOT EXISTS article
    (
        article varchar(100) NOT NULL,
        price integer
            CHECK (price >= 0),
        PRIMARY KEY (article)
    );

CREATE TABLE IF NOT EXISTS purchase
    (
        id serial NOT NULL,
        customer varchar(100) NOT NULL,
        date date,
        article varchar(100) NOT NULL,
        quantity int
            CHECK (quantity >= 0),
        PRIMARY KEY (id),
        FOREIGN KEY (customer) REFERENCES customer (name),
        FOREIGN KEY (article) REFERENCES article (article)
    );