package com.uniovi.tests.pageobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_OfertasView extends PO_NavView {
	
	static public void searchOffer(WebDriver driver, String title, String cadenaComprobacion) {
		WebElement buscador = PO_View.checkElement(driver, "id", "filtro-titulo").get(0);
		buscador.click();
		
		// Escribimos la búsqueda a realizar
		buscador.sendKeys(title);		
		
		// Comprobamos
		PO_View.checkElement(driver, "text", cadenaComprobacion);
	}
}
