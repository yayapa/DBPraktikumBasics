/* A1 */
/* 1. */
SELECT *
  FROM country
 WHERE population > 60000000;

/* 2. */
SELECT code
  FROM country
 WHERE capital IS NOT NULL
   AND population > 2500000;

/* 3. */
SELECT DISTINCT country.name
  FROM country
           INNER JOIN encompasses
           ON country.code = encompasses.country
 WHERE encompasses.continent LIKE 'Europe'
    OR encompasses.continent LIKE 'Asia'
 ORDER BY country.name;

/* 4. */
SELECT country.name
  FROM country
           INNER JOIN encompasses
           ON country.code = encompasses.country
 WHERE encompasses.continent LIKE 'Europe'
    OR encompasses.continent LIKE 'Asia'
 GROUP BY country.name
HAVING COUNT(*) > 1;

/* 5. */
SELECT city.name, city.population, country.name
  FROM (country INNER JOIN encompasses ON country.code = encompasses.country)
           INNER JOIN city
           ON country.capital = city.name
 WHERE encompasses.continent LIKE 'Asia';

/* 6.  */
SELECT code
  FROM country
           LEFT JOIN borders b1
           ON country.code = b1.country1
           LEFT JOIN borders b2
           ON country.code = b2.country2
 WHERE b1.country1 IS NULL
   AND b2.country2 IS NULL;

/* 7. */
SELECT DISTINCT geo_lake.country
  FROM lake
           INNER JOIN geo_lake
           ON lake.name = geo_lake.lake
 WHERE area = (SELECT MAX(area) FROM lake);

/* 8. */
SELECT *
  FROM city
 WHERE population = (SELECT MAX(population) FROM city);

/* 9. */
SELECT c1.*, c2.*
  FROM ((borders b1 LEFT JOIN borders b2 ON b1.length < b2.length) LEFT JOIN country c1 ON b1.country1 = c1.code)
           LEFT JOIN country c2
           ON b1.country2 = c2.code
 WHERE b2.country1 IS NULL;

/* 10. */
SELECT river
  FROM river
 GROUP BY river
HAVING river IS NOT NULL;

/* 11. */
SELECT DISTINCT (COUNT(*) OVER ())
  FROM language
 WHERE percentage >= 50
 GROUP BY country
HAVING COUNT(name) > 1;

/* 12. */
SELECT r.name, COUNT(*) AS numbereuropeanasiencountries
  FROM religion r
           INNER JOIN encompasses e
           ON r.country = e.country
 WHERE e.continent = 'Europe'
    OR e.continent = 'Asia'
 GROUP BY r.name
 ORDER BY COUNT(*) DESC;

/* 13. */
SELECT CASE WHEN c1.name = 'Germany' THEN c2.name ELSE c1.name END
  FROM (borders b LEFT JOIN country c1 ON b.country1 = c1.code)
           LEFT JOIN country c2
           ON b.country2 = c2.code
 WHERE (c2.name = 'Germany' OR c1.name = 'Germany')
   AND NOT EXISTS((SELECT language.name
                     FROM language
                              LEFT JOIN country
                              ON language.country = country.code
                    WHERE country.name = c2.name)
 INTERSECT
 (SELECT language.name
    FROM language
             LEFT JOIN country
             ON language.country = country.code
   WHERE country.name = c1.name));

/* A2 */
SELECT MIN(l1.city) AS city, l1.lake
  FROM located l1
           INNER JOIN city c
           ON c.name = l1.city AND l1.country = c.country
 WHERE l1.lake IS NOT NULL
 GROUP BY l1.lake
HAVING COUNT(l1.city) = 1
 ORDER BY MIN(c.population);
