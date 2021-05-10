package com.uniovi.tests.pageobjects;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.uniovi.tests.util.SeleniumUtils;

public class PO_UserListView extends PO_NavView {

	static public void seleccionarUsuario(WebDriver driver, int posicion) {
		// Sacamos todos los elemetos que tienen checkbox
		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//input[contains(@type,'checkbox')]");
		// Si la posicion es correcta seleccionas el checkbox
		if (posicion < elementos.size())
			elementos.get(posicion).click();
	}
	
	static public void removeUser(WebDriver driver, String email) {
		// Seleccionamos el menu ofertas
		PO_View.checkElement(driver, "text", "Gestion de Usuarios").get(0).click();
		// Accedemos a Ver Ofertas
		PO_View.checkElement(driver, "text", "Ver Usuarios").get(0).click();
		// Seleccionamos la oferta a borrar
		List<WebElement> emails = PO_View.checkElement(driver, "id", "email");
		for(int i=1; i<emails.size(); i++) 
			if(emails.get(i).getText().equals(email))
				seleccionarUsuario(driver,i-1);
		SeleniumUtils.esperarSegundos(driver, 5);
		PO_View.checkElement(driver,"free", "//button[contains(@type,'submit')]").get(0).click();
	}
}