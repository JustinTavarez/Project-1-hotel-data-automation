# Hotel Data Automation

This project helps you track and analyze hotel room prices for five hotel franchises across five cities.

## Scope

- Pick five hotel franchises that interest you.
- Pick five cities where you want to compare prices.
- Use a travel site like Priceline, Hotwire, Expedia or a similar platform to collect room prices.
- Collect daily room rates from May 1 of the current year through the last date with available data.
- Store the results in an SQLite database for easy access and analysis.
- Identify the ten dates with the lowest price for each hotel in each city.

## Objectives

- Build a program that runs smoothly and handles errors.
- Design a SQLite schema to store hotel, city, date and price data.
- Write tests using the Arrange‑Act‑Assert pattern.
- Use JUnit annotations such as `@BeforeClass`, `@AfterClass`, `@Before` and `@After` to manage test setup and teardown.
- Follow the DRY principle to avoid duplicate code.
- Provide a report (Word document) listing the lowest price dates by hotel and city.
- Prepare a short presentation explaining your approach.

## Repository Structure

A suggested structure for this repository could include:

- `src/` – source code for fetching, storing and analyzing data.
- `data/` – SQLite database or scripts to initialize it.
- `tests/` – unit tests following the AAA pattern.
- `docs/` – project notes and the final report.

## Getting Started

1. Fork or clone this repository.
2. Set up your preferred Java IDE (IntelliJ recommended).
3. Install any required libraries for HTTP requests, parsing and SQLite.
4. Create the database schema under `data/`.
5. Write scripts to scrape room prices from the chosen travel site.
6. Run tests to verify functionality.
7. Generate the report and add it to `docs/` when complete.

## Contributions

Work in a small team if you prefer. Keep commit messages clear. Push changes through pull requests for review.
