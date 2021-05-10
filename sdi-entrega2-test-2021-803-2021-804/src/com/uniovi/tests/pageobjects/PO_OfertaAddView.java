package com.uniovi.tests.pageobjects;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.uniovi.tests.util.SeleniumUtils;

public class PO_OfertaAddView extends PO_NavView{
	static public void fillForm(WebDriver driver, String ptitulo, String pdescripcion, String pcantidad) {
		WebElement titulo = driver.findElement(By.name("nombre"));
		titulo.click();
		titulo.clear();
		titulo.sendKeys(ptitulo);
		WebElement descripcion = driver.findElement(By.name("descripcion"));
		descripcion.click();
		descripcion.clear();
		descripcion.sendKeys(pdescripcion);		
		WebElement cantidad = driver.findElement(By.name("precio"));
		cantidad.click();
		cantidad.clear();
		cantidad.sendKeys(pcantidad);	
		// Pulsar el boton de Enviar.
		By boton = By.className("btn");
		driver.findElement(boton).click();
	}
	
	static public void fillForm(WebDriver driver, String ptitulo, String pdescripcion, String pcantidad, boolean destacada) {
		WebElement titulo = driver.findElement(By.name("nombre"));
		titulo.click();
		titulo.clear();
		titulo.sendKeys(ptitulo);
		WebElement descripcion = driver.findElement(By.name("descripcion"));
		descripcion.click();
		descripcion.clear();
		descripcion.sendKeys(pdescripcion);		
		WebElement cantidad = driver.findElement(By.name("precio"));
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
		// Vamos a add ofertas
		PO_View.checkElement(driver, "text", "Gestion de Ofertas").get(0).click();
		PO_View.checkElement(driver, "text", "Agregar Oferta").get(0).click();
		// Rellenamos el formulario y lo enviamos
		PO_OfertaAddView.fillForm(driver, titulo, descripcion, String.valueOf(cantidad));
	}
	
	static public void creaOferta(WebDriver driver, String titulo, String descripcion, double cantidad, boolean destacada) {
		// Vamos a add ofertas
		PO_View.checkElement(driver, "text", "Gestion de Ofertas").get(0).click();
		PO_View.checkElement(driver, "text", "Agregar Oferta").get(0).click();
		// Rellenamos el formulario y lo enviamos
		PO_OfertaAddView.fillForm(driver, titulo, descripcion, String.valueOf(cantidad), true);
	}	
}