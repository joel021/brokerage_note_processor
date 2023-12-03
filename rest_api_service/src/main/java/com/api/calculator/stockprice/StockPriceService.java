package com.api.calculator.stockprice;

import com.api.calculator.stockprice.ws.data.service.PDFFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StockPriceService implements CommandLineRunner {

	@Autowired
	PDFFileService pdfFileService;

	public static void main(String[] args) {
		SpringApplication.run(StockPriceService.class, args);
	}

	@Override
	public void run(String... arg) throws Exception {
		pdfFileService.init();
	}
}
