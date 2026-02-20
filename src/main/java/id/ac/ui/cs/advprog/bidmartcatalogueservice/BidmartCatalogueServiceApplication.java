package id.ac.ui.cs.advprog.bidmartcatalogueservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication
public class BidmartCatalogueServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidmartCatalogueServiceApplication.class, args);
	}

}
