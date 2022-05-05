CREATE DOMAIN Anteil AS decimal(5, 2) CHECK (VALUE BETWEEN 0 and 1);
CREATE DOMAIN Anzahl AS INTEGER CHECK (VALUE >= 0);
CREATE DOMAIN Laenge AS numeric(100, 5) CHECK (VALUE >= 0);

CREATE TABLE Land(
    Code char(2) NOT NULL,
    Name varchar(200),
    Flaeche Laenge,
    Bevoelkerungsgroesse Anzahl,
    BruttosozialproduktDesVorjahres Anzahl, /* Mrd Euro */
    PRIMARY KEY (Code)
);

CREATE TABLE Sprache(Name varchar(200) NOT NULL);

CREATE TABLE Religion(Name varchar(200) NOT NULL);

CREATE TABLE Stadt(
    Name varchar(200),
    AnzahlDerEinwohner Anzahl,
    AnzahlDerUniversitaeten Anzahl,
    Stadt_ID serial,
    PRIMARY KEY (Stadt_ID)
);

CREATE TABLE Stadtteil(
    Name varchar(200) NOT NULL,
    AnteilDerFlaeche Anteil,
    AnzahlDerSchulen Anzahl,
    Stadt_ID serial,
    PRIMARY KEY (Name),
    CONSTRAINT Stadt_ID FOREIGN KEY (Stadt_ID) REFERENCES Stadt (Stadt_ID)
);

CREATE TABLE Fluss(
    Name varchar(200) NOT NULL,
    Laenge Laenge,
    FreigabeSchifffahrt boolean,
    PRIMARY KEY (Name)
);

CREATE TABLE wird_gesprochen(
    Code char(2) NOT NULL,
    NameSprache varchar(200) NOT NULL,
    AnteilDerBevoelkerung Anteil,
    PRIMARY KEY (Code, NameSprache)
);

CREATE TABLE ist_offiziell(
	Code char(2) NOT NULL,
	NameSprache varchar(200) NOT NULL,
	PRIMARY KEY (Code)
);

CREATE TABLE ist_verbreitet(
    Code char(2) NOT NULL,
    NameReligion varchar(200),
    AnteilDerBevoelkerung Anteil,
    PRIMARY KEY (Code, NameReligion)
);

CREATE TABLE hat_Grenze(
    Code_A char(2) NOT NULL,
    Code_B char(2) NOT NULL,
    Laenge Laenge,
    AnzahlDerGrenzuebergaenge Anzahl,
    PRIMARY KEY (Code_B)
);

CREATE TABLE Hauptstadt(
    Code char(2) NOT NULL,
    Stadt_ID serial,
    Jahr INTEGER,
    PRIMARY KEY (Code)
);

CREATE TABLE fliesst(
    Stadt_ID serial,
    NameFluss varchar(200) NOT NULL,
    PRIMARY KEY (Stadt_ID, NameFluss)
);

CREATE TABLE muendet(
    Name_A varchar(200) NOT NULL,
    Name_B varchar(200) NOT NULL,
    PRIMARY KEY (Name_A, Name_B)
);