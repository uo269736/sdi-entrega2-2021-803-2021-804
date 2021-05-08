package com.uniovi.tests.pageobjects;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_RegisterView extends PO_NavView {	
	
	static public void fillForm(WebDriver driver, String emailp, String nombrep, String apellidosp, String passwordp, String passwordconfp) {
		WebElement email = driver.findElement(By.name("email"));
		email.click();
		email.clear();
		email.sendKeys(emailp);
		WebElement nombre = driver.findElement(By.name("nombre"));
		nombre.click();
		nombre.clear();
		nombre.sendKeys(nombrep);
		WebElement apellidos = driver.findElement(By.name("apellidos"));
		apellidos.click();
		apellidos.clear();
		apellidos.sendKeys(apellidosp);
		WebElement password = driver.findElement(By.name("password"));
		password.click();
		password.clear();
		password.sendKeys(passwordp);
		WebElement passwordConfirm = driver.findElement(By.name("passwordc"));
		passwordConfirm.click();
		passwordConfirm.clear();
		passwordConfirm.sendKeys(passwordconfp);
		// Pulsar el boton de Alta.
		By boton = By.className("btn");
		driver.findElement(boton).click();
	}
	
}
