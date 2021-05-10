package com.uniovi.tests.pageobjects;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_UserListView extends PO_NavView {

//	static public void checkAllUsers(WebDriver driver, UserService userService) {
//		// Sacamos todos los usuarios del sistema
//		List<User> usuarios = userService.getUsers();
//		// Comprobamos que todos están en la vista
//		for (User u : usuarios) {
//			checkElement(driver, "text", u.getEmail());
//		}
//	}
//
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
		PO_View.checkElement(driver,"free", "//button[contains(@type,'submit')]").get(0).click();
	}
//
//	static public void comprobarElementoDeLista(WebDriver driver, int posicion, List<User> usuarios, boolean esta) {
//		// Sacamos todos los usuarios del sistema
//		String email = usuarios.get(posicion).getEmail();
//		if (esta)
//			SeleniumUtils.textoPresentePagina(driver, email);
//		else
//			SeleniumUtils.textoNoPresentePagina(driver, email);
//	}
//
//	static public void checkChangeIdiom(WebDriver driver, String textIdiom1, String textIdiom2, int locale1,
//			int locale2) {
//		// Pinchamos en la opción de menu de Usuarios: //li[contains(@id,
//		// 'users-menu')]/a
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'users-menu')]/a");
//		elementos.get(0).click();
//		// Pinchamos en la opción de lista de usuarios.
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href,'user/list')]");
//		elementos.get(0).click();
//		// Esperamos a que se cargue el saludo de bienvenida en Español
//		PO_UserListView.checkOfertasCompradas(driver, locale1);
//		// Cambiamos a segundo idioma
//		PO_UserListView.changeIdiom(driver, textIdiom2);
//		// COmprobamos que el texto de bienvenida haya cambiado a segundo idioma
//		PO_UserListView.checkOfertasCompradas(driver, locale2);
//		// Volvemos a Español.
//		PO_UserListView.changeIdiom(driver, textIdiom1);
//		// Esperamos a que se cargue el saludo de bienvenida en Español
//		PO_UserListView.checkOfertasCompradas(driver, locale1);
//	}
//
//	static public void checkOfertasCompradas(WebDriver driver, int language) {
//		SeleniumUtils.EsperaCargaPagina(driver, "text", p.getString("user.message", language), getTimeout());
//		SeleniumUtils.EsperaCargaPagina(driver, "text", p.getString("user.email", language), getTimeout());
//	}

}