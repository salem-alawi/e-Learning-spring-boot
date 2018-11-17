package com.exatech.finanacemanager.config;

import org.apache.commons.lang3.Validate;

import javax.inject.Named;

@Named
public class FlywayMigrationReport {

  private int numberOfSuccessfullyAppliedMigrations = 0;

  public void setNumberOfSuccessfullyAppliedMigrations(int number) {
    Validate.isTrue(number >= 0);
    numberOfSuccessfullyAppliedMigrations = number;
  }

  public boolean migrationsApplied() {
    return numberOfSuccessfullyAppliedMigrations > 0;
  }

  public int getNumberOfSuccessfullyAppliedMigrations() {
    return numberOfSuccessfullyAppliedMigrations;
  }

}
