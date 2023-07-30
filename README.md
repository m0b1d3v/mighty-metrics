# The Mighty Metrics

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/summary/overall?id=mobiusk_mighty-metrics)

A workout tracker for [The Mighty Gym](https://www.themightygym.com/) community.
Workout records captured from the Score Core can be uploaded here for analysis.

## Development Makefile tasks

Tasks are listed here and in the Makefile alphabetically.

| Task                 | Description                                                                          |
|----------------------|--------------------------------------------------------------------------------------|
| build                | Create a ZIP file in the `build` directory with everything necessary to run program. |
| checkDependencies    | Check library dependencies for potential updates.                                    |
| checkSource          | Check source code against SonarCloud scanner for issues.                             |
| checkVulnerabilities | Check library dependencies against the OWASP database for vulnerabilities.           |
| clean                | Wipe out the `build` directory from the file system.                                 |
| run                  | Start the program locally.                                                           |
| tasks                | Output all possible tasks that Gradle can run for this project.                      |
| test                 | Run all test files.                                                                  |
| testIntegrations     | Run test files that end with "IntegrationTest".                                      |
| testUnits            | Run test files that end with "UnitTest".                                             |
