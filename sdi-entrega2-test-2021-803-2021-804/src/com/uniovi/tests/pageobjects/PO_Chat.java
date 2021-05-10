package com.uniovi.tests.pageobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_Chat extends PO_View {
	static public void send(WebDriver driver, String message) {
		WebElement input = PO_View.checkElement(driver, "id", "escribir-mensaje").get(0);
		input.click();
		
		// Escribimos la búsqueda a realizar
		input.sendKeys(message);	
		// Enviamos el mensaje
		PO_View.checkElement(driver, "id", "boton-enviarMensaje").get(0).click();
	}
}
