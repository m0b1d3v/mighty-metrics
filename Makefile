.PHONY: build

all:
	cat --number Makefile

build:
	./gradlew assembleBootDist

buildToolUpdate:
	./gradlew wrapper --gradle-version latest

checkDependencies:
	./gradlew dependencyUpdates

checkSource: clean test
	./gradlew sonar

checkVulnerabilities:
	./gradlew dependencyCheckAnalyze

classes:
	./gradlew classes

clean:
	./gradlew clean

run:
	./gradlew bootRun

tasks:
	./gradlew tasks

test:
	./gradlew test

testIntegrations:
	./gradlew test --tests '*IntegrationTest'

testUnits:
	./gradlew test --tests '*UnitTest'
