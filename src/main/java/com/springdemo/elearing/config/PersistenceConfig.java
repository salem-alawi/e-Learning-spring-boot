package com.exatech.finanacemanager.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories()
@EnableTransactionManagement
public class PersistenceConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceConfig.class);

  @Value("${spring.jpa.properties.hibernate.search.default.directory_provider:ram}")
  private String hibernateSearchDefaultDirectoryProvider;

  @Value("${spring.jpa.properties.hibernate.search.default.indexBase:}")
  private String hibernateSearchDefaulIndexBase;

  @Value("${spring.jpa.properties.hibernate.dialect:}")
  private String hibernateDialect;

  @Value("${flyway.locations:classpath:/db/migration/h2}")
  private String[] flywayLocations;

  @Inject
  private DataSource dataSource;

  @Inject
  private FlywayMigrationReport flywayMigrationReport;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  @DependsOn(value = "flyway")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
    emfb.setDataSource(dataSource);
    emfb.setJpaVendorAdapter(jpaVendorAdapter());
    emfb.setPackagesToScan("com.exatech.finanacemanager.domain");
    emfb.setJpaProperties(hibernateProperties());
    return emfb;
  }

  @Bean
  @DependsOn(value = "flyway")
  public JpaVendorAdapter jpaVendorAdapter() {
    return new HibernateJpaVendorAdapter();
  }

  private Properties hibernateProperties() {
    final Properties props = new Properties();
    props.setProperty("hibernate.hbm2ddl.auto", "validate");

    if (hibernateDialect != null) {
      props.setProperty("hibernate.dialect", hibernateDialect);
    }

    if (hibernateSearchDefaultDirectoryProvider == null) {
      throw new IllegalStateException("Hibernate Search Default Directory Provider not set");
    }
    props.put("hibernate.search.default.directory_provider", hibernateSearchDefaultDirectoryProvider);
    if (hibernateSearchDefaulIndexBase != null) {
      LOGGER.info("Hibernate search default index base : " + hibernateSearchDefaulIndexBase);
      props.put("hibernate.search.default.indexBase", hibernateSearchDefaulIndexBase);
    } else {
      LOGGER.warn("Hibernate search default index base not set");
    }
    return props;
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    final EntityManagerFactory factory = entityManagerFactory().getObject();
    return new JpaTransactionManager(factory);
  }

  @Bean
  @DependsOn(value = "dataSource")
  public Flyway flyway() {
    final Flyway flyway = new Flyway();
    flyway.setBaselineOnMigrate(true);
    flyway.setLocations(flywayLocations);
    flyway.setDataSource(dataSource);
    flyway.setValidateOnMigrate(true);
    final int numberOfSuccessfulAppliedMigrations = flyway.migrate();
    flywayMigrationReport.setNumberOfSuccessfullyAppliedMigrations(numberOfSuccessfulAppliedMigrations);
    return flyway;
  }

}
