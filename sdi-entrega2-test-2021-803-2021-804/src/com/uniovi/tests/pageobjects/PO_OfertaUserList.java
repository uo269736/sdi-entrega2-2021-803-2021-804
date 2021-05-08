package com.uniovi.tests.pageobjects;

public class PO_OfertaUserList extends PO_NavView {
//	static public void comprobarElementoDeLista(WebDriver driver, int posicion, List<Oferta> ofertas, boolean esta) {
//		// Sacamos todos los usuarios del sistema
//		String titulo = ofertas.get(posicion).getTitulo();
//		if (esta)
//			SeleniumUtils.textoPresentePagina(driver, titulo);
//		else
//			SeleniumUtils.textoNoPresentePagina(driver, titulo);
//	}
//
//	static public void comprobarAllOfertas(WebDriver driver, User usuario, List<Oferta> ofertas,
//			List<WebElement> elementos) {
//		int posicion = 0;
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@class, 'page-link')]");
//		// Vamos pagina por pagina (elementos)
//		for (int j = 1; j < elementos.size() - 1; j++) {
//			elementos.get(j).click();
//			// En cada pagina comprobamos los 5 elementos que le pertenecen
//			for (int i = 0; i < 5 && posicion < ofertas.size(); i++) {
//				PO_OfertaUserList.comprobarElementoDeLista(driver, posicion, ofertas, true);
//				posicion += 1;
//			}
//		}
//	}
//
//	// Misma funcionalidad
//	// PERO teniendo en cuenta que al coger las paginas si tengo:
//	// PRIMERA - 1 - 2 - SEGUNDA -> lista de 4 elementos pero al pasar
//	// a la pagina 2 tendre PRIMERA - 1-2-3- SEGUNDA
//	// asi que en cada iteracion cambiar elementos
//	static public void comprobarAllOfertasPaginaPorPagina(WebDriver driver, User usuario, List<Oferta> ofertas,
//			List<WebElement> elementos) {
//		int posicion = 0;
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@class, 'page-link')]");
//		// Vamos pagina por pagina (elementos)
//		while (true) {
//			if (elementos.size() == 4)
//				elementos.get(1).click();
//			else
//				elementos.get(2).click();
//			// En cada pagina comprobamos los 5 elementos que le pertenecen
//			for (int i = 0; i < 5 && posicion < ofertas.size(); i++) {
//				PO_OfertaUserList.comprobarElementoDeLista(driver, posicion, ofertas, true);
//				posicion += 1;
//			}
//			// pasamos pagina
//			elementos = PO_View.checkElement(driver, "free", "//a[contains(@class, 'page-link')]");
//			if (elementos.size() == 4)
//				elementos.get(2).click();
//			else
//				elementos.get(3).click();
//			elementos = PO_View.checkElement(driver, "free", "//a[contains(@class, 'page-link')]");
//			if (elementos.size() == 4)
//				break;
//		}
//	}
}