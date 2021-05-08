package com.uniovi.tests.pageobjects;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.uniovi.tests.util.SeleniumUtils;

public class PO_OfertaAddView extends PO_NavView{
	static public void fillForm(WebDriver driver, String ptitulo, String pdescripcion, String pcantidad) {
		WebElement titulo = driver.findElement(By.name("titulo"));
		titulo.click();
		titulo.clear();
		titulo.sendKeys(ptitulo);
		WebElement descripcion = driver.findElement(By.name("descripcion"));
		descripcion.click();
		descripcion.clear();
		descripcion.sendKeys(pdescripcion);		
		WebElement cantidad = driver.findElement(By.name("cantidad"));
		cantidad.click();
		cantidad.clear();
		cantidad.sendKeys(pcantidad);	
		// Pulsar el boton de Enviar.
		By boton = By.className("btn");
		driver.findElement(boton).click();
	}
	
	static public void fillForm(WebDriver driver, String ptitulo, String pdescripcion, String pcantidad, boolean destacada) {
		WebElement titulo = driver.findElement(By.name("titulo"));
		titulo.click();
		titulo.clear();
		titulo.sendKeys(ptitulo);
		WebElement descripcion = driver.findElement(By.name("descripcion"));
		descripcion.click();
		descripcion.clear();
		descripcion.sendKeys(pdescripcion);		
		WebElement cantidad = driver.findElement(By.name("cantidad"));
		cantidad.click();
		cantidad.clear();
		cantidad.sendKeys(pcantidad);	
		//Seleccionamos el checkbok
		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//input[contains(@type,'checkbox')]");
		elementos.get(0).click();
		// Pulsar el boton de Enviar.
		By boton = By.className("btn");
		driver.findElement(boton).click();
	}
	
	static public void creaOferta(WebDriver driver, String titulo, String descripcion, double cantidad) {
		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
		elementos.get(0).click();
		// Accedemos a Agregar Ofertas
		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/add')]");
		elementos.get(0).click();
		// Rellenamos el formulario y lo enviamos
		PO_OfertaAddView.fillForm(driver, titulo, descripcion, String.valueOf(cantidad));
	}
	
	static public void creaOferta(WebDriver driver, String titulo, String descripcion, double cantidad, boolean destacada) {
		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
		elementos.get(0).click();
		// Accedemos a Agregar Ofertas
		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/add')]");
		elementos.get(0).click();
		// Rellenamos el formulario y lo enviamos
		PO_OfertaAddView.fillForm(driver, titulo, descripcion, String.valueOf(cantidad), true);
	}
	
	static public void checkChangeIdiom(WebDriver driver, String textIdiom1, String textIdiom2, int locale1,
			int locale2) {
		// Pinchamos en la opción de menu de Usuarios: //li[contains(@id, 'users-menu')]/a
		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
		elementos.get(0).click();
		elementos.get(0).click();
		// Pinchamos en la opción de lista de usuarios.
		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href,'oferta/add')]");
		elementos.get(0).click();
		// Esperamos a que se cargue el saludo de bienvenida en Español
		PO_OfertaAddView.checkañadirOfertas(driver, locale1);
		// Cambiamos a segundo idioma
		PO_OfertaAddView.changeIdiom(driver, textIdiom2);
		// Comprobamos que el texto de bienvenida haya cambiado a segundo idioma
		PO_OfertaAddView.checkañadirOfertas(driver, locale2);
		// Volvemos a Español.
		PO_OfertaAddView.changeIdiom(driver, textIdiom1);
		// Esperamos a que se cargue el saludo de bienvenida en Español
		PO_OfertaAddView.checkañadirOfertas(driver, locale1);
	}
	
	static public void checkañadirOfertas(WebDriver driver, int language) {
		SeleniumUtils.EsperaCargaPagina(driver, "text", p.getString("oferta.enviar", language), getTimeout());
		SeleniumUtils.EsperaCargaPagina(driver, "text", p.getString("oferta.titulo", language), getTimeout());
	}
	
}