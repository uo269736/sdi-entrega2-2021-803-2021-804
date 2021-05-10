package com.uniovi.tests.pageobjects;

import java.util.ArrayList;
import java.util.List;

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
	
	static public void removeLastOffer(WebDriver driver) {
		// Seleccionamos el menu ofertas
		PO_View.checkElement(driver, "text", "Gestion de Ofertas").get(0).click();
		// Accedemos a Ver Ofertas
		PO_View.checkElement(driver, "text", "Ver Mis Ofertas").get(0).click();
		// Seleccionamos la oferta a borrar
		List<WebElement> botones = PO_View.checkElement(driver, "id", "eliminar");
		botones.get(botones.size()-1).click();
	}
}
