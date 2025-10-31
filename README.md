# Hotel Data Automation

This project stores and analyses hotel room prices for five hotel franchises across five cities.

## About the Project

- Collects daily room prices from travel websites like Priceline, Hotwire or Expedia.
- Stores data in a SQLite database from May 1 of the current year onward.
- Finds the ten cheapest dates for each hotel in each city.
- Designed for a small team using Java.

## Code Overview

- **Data Collector** – uses HTTP requests and simple HTML parsing to fetch room prices from selected websites.
- **Database Layer** – defines a SQLite schema with tables for hotels, cities, dates and prices; provides CRUD operations.
- **Analysis Module** – queries the database to compute the lowest prices and extracts the top ten dates per hotel per city.
- **Tests** – uses the Arrange–Act–Assert pattern with JUnit annotations (`@BeforeClass`, `@AfterClass`, `@Before`, `@After`) to set up and tear down test data.

## How It Works

1. At runtime, the data collector fetches prices for each hotel and city combination.
2. The database layer writes the collected data to a local SQLite database.
3. The analysis module retrieves the data and identifies the ten cheapest dates for each hotel and city.
4. You can extend the project with a simple UI to display results or export them to a report.

## Repository Structure

- `src/` – Java source code for data collection, database management and analysis.
- `tst/` – unit tests for each module.
- `out/` – compiled classes and build outputs.
- `README.md` – project description and usage instructions.

## Getting Started

- Clone this repository.
- Use Java 17 (or later) and SQLite.
- Install any required libraries for HTTP and parsing.
- Run the collector to populate the database, then execute the analysis module to generate results.
