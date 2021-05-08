package com.uniovi.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
//Paquetes Java
import java.util.List;

import org.bson.BsonBoolean;
import org.bson.conversions.Bson;
//Paquetes JUnit 
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
//Paquetes Selenium 
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.seleniumhq.jetty9.server.Authentication.User;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
//Paquetes con los Page Object
import com.uniovi.tests.pageobjects.PO_HomeView;
import com.uniovi.tests.pageobjects.PO_LoginView;
import com.uniovi.tests.pageobjects.PO_OfertaAddView;
import com.uniovi.tests.pageobjects.PO_OfertaUserList;
import com.uniovi.tests.pageobjects.PO_OfertasCompradasListView;
import com.uniovi.tests.pageobjects.PO_Properties;
import com.uniovi.tests.pageobjects.PO_RegisterView;
import com.uniovi.tests.pageobjects.PO_UserListView;
import com.uniovi.tests.pageobjects.PO_View;
//Paquetes Utilidades de Testing Propias
import com.uniovi.tests.util.SeleniumUtils;

//Ordenamos las pruebas por el nombre del mÃ©todo
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SdiEntrega2Tests {
	// En Windows (Debe ser la versiÃ³n 65.0.1 y desactivar las actualizacioens
	// automÃ¡ticas)):
	static String PathFirefox65 = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
	// Miguel
	//static String Geckdriver024 = "C:\\Users\\MiguelUni\\Desktop\\TrabajoUniversidadMiguel\\Tercero\\SDI\\Sesion 5\\PL-SDI-Sesión5-material\\geckodriver024win64.exe";
	// Alex
	 static String Geckdriver024 =
	 "C:\\Users\\Usuario\\Desktop\\CallateYa\\SDI\\Sesion5\\PL-SDI-Sesión5-material\\geckodriver024win64.exe";
	// ComÃºn a Windows y a MACOSX
	static WebDriver driver = getDriver(PathFirefox65, Geckdriver024);
	static String URL = "https://localhost:8081";
	
	static MongoClient mongoClient;
	static MongoDatabase database;

	public static WebDriver getDriver(String PathFirefox, String Geckdriver) {
		System.setProperty("webdriver.firefox.bin", PathFirefox);
		System.setProperty("webdriver.gecko.driver", Geckdriver);
		WebDriver driver = new FirefoxDriver();
		return driver;
	}

	@Before
	public void setUp() {
		initdb();
		driver.navigate().to(URL);
	}

	@After
	public void tearDown() {
		driver.manage().deleteAllCookies();
	}

	@BeforeClass
	static public void begin() {
		// COnfiguramos las pruebas.
		// Fijamos el timeout en cada opciÃ³n de carga de una vista. 2 segundos.
		PO_View.setTimeout(3);

	}

	@AfterClass
	static public void end() {
		// Cerramos el navegador al finalizar las pruebas
		driver.quit();
		mongoClient.close();
	}
	
	public void initdb() {
        mongoClient = MongoClients.create(
                "mongodb+srv://admin:sdi@mywallapop.thyhc.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        database = mongoClient.getDatabase("myFirstDatabase");        
        //database.getCollection("offers").drop();
    }

	// [Prueba1] Registro de Usuario con datos válidos.
		@Test
		public void Prueba01() {
			// Vamos al formulario de registro
			PO_HomeView.clickOption(driver, "registrarse", "class", "btn btn-primary");
			// Rellenamos el formulario.
			PO_RegisterView.fillForm(driver, "b@email.com", "Josefo", "Perez", "123456", "123456");
			// Comprobamos que entramos en la sección privada
			PO_View.checkElement(driver, "text", "b@email.com");
		}

		// [Prueba2] Registro de Usuario con datos inválidos (email vacío, nombre vacío,
		// apellidos vacíos).
		@Test
		public void Prueba02() {
			// Vamos al formulario de registro
			PO_HomeView.clickOption(driver, "registrarse", "class", "btn btn-primary");
			// Rellenamos el formulario.
			PO_RegisterView.fillForm(driver, "", "", "", "123456", "123456");
			// Comprobamos que no cambiamos de página y que no sale mensaje de error
			SeleniumUtils.textoPresentePagina(driver, "Registrar usuario");
			SeleniumUtils.textoNoPresentePagina(driver, "debe");
		}

		// [Prueba3] Registro de Usuario con datos inválidos (repetición de contraseña
		// inválida).
		@Test
		public void Prueba03() {
			// Vamos al formulario de registro
			PO_HomeView.clickOption(driver, "registrarse", "class", "btn btn-primary");
			// Rellenamos el formulario.
			PO_RegisterView.fillForm(driver, "a@email.com", "Josefo", "Perez", "123456", "456789");
			// Comprobamos que no cambiamos de página y que sale el mensaje de error de las
			// contraseñas
			SeleniumUtils.textoPresentePagina(driver, "Las contraseñas deben ser iguales");

		}

		// [Prueba4] Registro de Usuario con datos inválidos (email existente).
		@Test
		public void Prueba04() {
			// Vamos al formulario de registro
			PO_HomeView.clickOption(driver, "registrarse", "class", "btn btn-primary");
			// Rellenamos el formulario.
			PO_RegisterView.fillForm(driver, "admin@email.com", "Josefo", "Perez", "123456", "456");
			// Comprobamos que no cambiamos de página y que sale el mensaje de error de las
			// contraseñas
			SeleniumUtils.textoPresentePagina(driver, "Este email ya está en uso");
		}

		// [Prueba5] Inicio de sesión con datos válidos (administrador).
		@Test
		public void Prueba05() {
			// Vamos al formulario de logueo.
			PO_HomeView.clickOption(driver, "identificarse", "class", "btn btn-primary");
			// Rellenamos el formulario
			PO_LoginView.fillForm(driver, "admin@email.com", "admin");
			// Comprobamos que entramos en la pagina privada del Admin
			PO_View.checkElement(driver, "text", "admin@email.com");
			SeleniumUtils.textoPresentePagina(driver, "Gestion de Usuarios");
		}

		// [Prueba5b] Inicio de sesión con datos válidos (usuario estándar).
		@Test
		public void Prueba05b() {
			// Vamos al formulario de logueo.
			PO_HomeView.clickOption(driver, "identificarse", "class", "btn btn-primary");
			// Rellenamos el formulario
			PO_LoginView.fillForm(driver, "prueba@uniovi.es", "123456");
			// Comprobamos que entramos en la pagina privada del Usuario Estandar
			PO_View.checkElement(driver, "text", "prueba@uniovi.es");
			PO_View.checkElement(driver, "text", "Gestion de Ofertas");
			SeleniumUtils.textoNoPresentePagina(driver, "Gestion de Usuarios");
		}

		// [Prueba6] Inicio de sesión con datos inválidos (usuario estándar, email
		// existente, pero contraseña
		// incorrecta).
		@Test
		public void Prueba06() {
			// Vamos al formulario de logueo.
			PO_HomeView.clickOption(driver, "identificarse", "class", "btn btn-primary");
			// Rellenamos el formulario
			PO_LoginView.fillForm(driver, "prueba@uniovi.es", "111111");
			// Comprobamos que nos sale el error de contraseña o usuario incorrecto
			SeleniumUtils.textoPresentePagina(driver, "Email o password incorrecto");
		}

		// [Prueba7] Inicio de sesión con datos inválidos (usuario estándar, campo email
		// y contraseña vacíos).
		@Test
		public void Prueba07() {
			// Vamos al formulario de logueo.
			PO_HomeView.clickOption(driver, "identificarse", "class", "btn btn-primary");
			// Rellenamos el formulario
			PO_LoginView.fillForm(driver, "", "");
			// Comprobamos que no nos movemos y que no sale ningún error
			PO_View.checkElement(driver, "text", "Identifícate");
			PO_View.checkElement(driver, "text", "Email");
			SeleniumUtils.textoNoPresentePagina(driver, "Desconectar");
		}

		// [Prueba8] Inicio de sesión con datos inválidos (usuario estándar, email no
		// existente en la aplicación).
		@Test
		public void Prueba08() {
			// Vamos al formulario de logueo.
			PO_HomeView.clickOption(driver, "identificarse", "class", "btn btn-primary");
			// Rellenamos el formulario
			PO_LoginView.fillForm(driver, "123@email.com", "111111");
			// Comprobamos que nos sale el error de contraseña o usuario incorrecto
			SeleniumUtils.textoPresentePagina(driver, "Email o password incorrecto");
		}

		// [Prueba10] Hacer click en la opción de salir de sesión y comprobar que se
		// redirige a la página de inicio
		// de sesión (Login).
		@Test
		public void Prueba09() {
			// Vamos al formulario de logueo.
			PO_HomeView.clickOption(driver, "identificarse", "class", "btn btn-primary");
			// Rellenamos el formulario
			PO_LoginView.fillForm(driver, "prueba@uniovi.es", "123456");
			// Comprobamos que entramos en la pagina privada del Usuario Estandar
			PO_View.checkElement(driver, "text", "prueba@uniovi.es");
			PO_View.checkElement(driver, "text", "Gestion de Ofertas");
			SeleniumUtils.textoNoPresentePagina(driver, "Gestión de Usuarios");
			// Ahora nos desconectamos
			PO_HomeView.clickOption(driver, "desconectarse", "class", "btn btn-primary");
			// Comprobamos que sale la página de inicar sesión
			SeleniumUtils.textoPresentePagina(driver, "Identifícate");
			SeleniumUtils.textoPresentePagina(driver, "Email:");
		}

		// [Prueba10] Comprobar que el botón cerrar sesión no está visible si el usuario
		// no está autenticado
		@Test
		public void Prueba10() {
			// Vamos al formulario de logueo.
			PO_HomeView.clickOption(driver, "identificarse", "class", "btn btn-primary");
			// Probamos si esta el botón desconectar antes de entrar
			SeleniumUtils.textoNoPresentePagina(driver, "Desconectarse");
			// Rellenamos el formulario
			PO_LoginView.fillForm(driver, "prueba@uniovi.es", "123456");
			// Comprobamos que entramos en la pagina privada del Usuario Estandar
			PO_View.checkElement(driver, "text", "prueba@uniovi.es");
			PO_View.checkElement(driver, "text", "Gestion de Ofertas");
			SeleniumUtils.textoNoPresentePagina(driver, "Gestión de Usuarios");
			// Ahora nos desconectamos
			PO_HomeView.clickOption(driver, "desconectarse", "class", "btn btn-primary");
			// Ahora tras desconectarnos comprobamos si esta el botón desconectarse
			SeleniumUtils.textoNoPresentePagina(driver, "Desconectarse");
		}

//		// [Prueba11] Mostrar el listado de usuarios y comprobar que se muestran todos
//		// los que existen en el sistema.
//		@Test
//		public void Prueba11() {
//			// Vamos al formulario de logueo.
//			PO_HomeView.clickOption(driver, "identificarse", "class", "btn btn-primary");
//			// Rellenamos el formulario
//			PO_LoginView.fillForm(driver, "admin@email.com", "admin");
//			// Pinchamos en la opción de menu de Usuarios: //li[contains(@id,
//			// 'gestionUsuarios')]/a
//			List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'gestionUsuarios')]/a");
//			elementos.get(0).click();
//			// Pinchamos en la opción de lista de usuarios.
//			elementos = PO_View.checkElement(driver, "free", "//a[contains(@href,'/usuario/list')]");
//			elementos.get(0).click();
//			PO_UserListView.checkAllUsers(driver,database.getCollection("usuarios").count());
//		}
//
//	// [Prueba12] Ir a la lista de usuarios, borrar el primer usuario de la lista,
//	// comprobar que la lista se actualiza y que el usuario desaparece.
//	@Test
//	public void Prueba12() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "admin@email.com", "admin");
//		// Pinchamos en la opción de menu de Usuarios: //li[contains(@id,
//		// 'users-menu')]/a
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'users-menu')]/a");
//		elementos.get(0).click();
//		// Pinchamos en la opción de lista de usuarios.
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href,'user/list')]");
//		elementos.get(0).click();
//		// Sacamos la lista de usuarios
//		List<User> usuarios = usersService.getUsers();
//		// Comprobamos que está el primer usuario
//		PO_UserListView.comprobarElementoDeLista(driver, 0, usuarios, true);
//		// Seleccionamos los usuarios que queremos eliminar
//		PO_UserListView.seleccionarUsuario(driver, 0);
//		// Le damos a eliminar
//		elementos = PO_View.checkElement(driver, "free", "//button[contains(@type,'submit')]");
//		elementos.get(0).click();
//		// Comprobamos que no está el primer elemento de antes
//		PO_UserListView.comprobarElementoDeLista(driver, 0, usuarios, false);
//	}
//
//	// [Prueba13] Ir a la lista de usuarios, borrar el último usuario de la lista,
//	// comprobar que la lista se actualiza y que el usuario desaparece.
//	@Test
//	public void Prueba13() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "admin@email.com", "admin");
//		// Pinchamos en la opción de menu de Usuarios: //li[contains(@id,
//		// 'users-menu')]/a
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'users-menu')]/a");
//		elementos.get(0).click();
//		// Pinchamos en la opción de lista de usuarios.
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href,'user/list')]");
//		elementos.get(0).click();
//		// Sacamos la lista de usuarios
//		List<User> usuarios = usersService.getUsers();
//		// Comprobamos que está el último usuario (en este caso como es un admin es el
//		// antepenultimo)
//		PO_UserListView.comprobarElementoDeLista(driver, usuarios.size() - 2, usuarios, true);
//		// Seleccionamos los usuarios que queremos eliminar
//		PO_UserListView.seleccionarUsuario(driver, usuarios.size() - 2);
//		// Le damos a eliminar
//		elementos = PO_View.checkElement(driver, "free", "//button[contains(@type,'submit')]");
//		elementos.get(0).click();
//		// Comprobamos que no está el último elemento (penúltimo)
//		PO_UserListView.comprobarElementoDeLista(driver, usuarios.size() - 2, usuarios, false);
//	}
//
//	// [Prueba14] Ir a la lista de usuarios, borrar 3 usuarios, comprobar que la
//	// lista se actualiza y que los usuarios desaparecen
//	@Test
//	public void Prueba14() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "admin@email.com", "admin");
//		// Pinchamos en la opción de menu de Usuarios: //li[contains(@id,
//		// 'users-menu')]/a
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'users-menu')]/a");
//		elementos.get(0).click();
//		// Pinchamos en la opción de lista de usuarios.
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href,'user/list')]");
//		elementos.get(0).click();
//		// Sacamos la lista de usuarios
//		List<User> usuarios = usersService.getUsers();
//		// Comprobamos que están los tres primeros usuarios
//		PO_UserListView.comprobarElementoDeLista(driver, usuarios.size() - 2, usuarios, true);
//		// Seleccionamos los usuarios que queremos eliminar
//		PO_UserListView.seleccionarUsuario(driver, 0);
//		PO_UserListView.seleccionarUsuario(driver, 1);
//		PO_UserListView.seleccionarUsuario(driver, 2);
//		// Le damos a eliminar
//		elementos = PO_View.checkElement(driver, "free", "//button[contains(@type,'submit')]");
//		elementos.get(0).click();
//		// Comprobamos que no están los tres primeros de antes
//		PO_UserListView.comprobarElementoDeLista(driver, 0, usuarios, false);
//		PO_UserListView.comprobarElementoDeLista(driver, 1, usuarios, false);
//		PO_UserListView.comprobarElementoDeLista(driver, 2, usuarios, false);
//	}
//
//	// [Prueba15] Ir al formulario de alta de oferta, rellenarla con datos válidos y
//	// pulsar el botón Submit.
//	// Comprobar que la oferta sale en el listado de ofertas de dicho usuario.
//	@Test
//	public void Prueba15() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		// Creamos la oferta
//		PO_OfertaAddView.creaOferta(driver, "Pato de goma",
//				"Vendo el pato de goma que me regalo mi tia cuando era pequeño. Por cierto, es de goma", 4);
//		// Vamos al listado de ofertas personales
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/userlist')]");
//		elementos.get(0).click();
//		// Comprobamos que la oferta creada esta presente en la ultima posicion
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@class, 'page-link')]");
//		elementos.get(elementos.size() - 1).click();
//		SeleniumUtils.textoPresentePagina(driver, "Pato de goma");
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba16] Ir al formulario de alta de oferta, rellenarla con datos inválidos
//	// (campo título vacío) y pulsar
//	// el botón Submit. Comprobar que se muestra el mensaje de campo obligatorio.
//	@Test
//	public void Prueba16() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		// Creamos oferta no valida
//		PO_OfertaAddView.creaOferta(driver, "Pato",
//				"Vendo el pato de goma que me regalo mi tia cuando era pequeño. Por cierto, es de goma", 4);
//		PO_OfertaAddView.checkKey(driver, "Error.oferta.titulo.length", PO_Properties.getSPANISH());
//
//		// Rellenamos el formulario con descripcion no valida
//		PO_OfertaAddView.fillForm(driver, "Pato de goma", "Vendo pato", String.valueOf(4));
//		PO_OfertaAddView.checkKey(driver, "Error.oferta.descripcion.length", PO_Properties.getSPANISH());
//
//		// Rellenamos el formulario con precio no valido
//		PO_OfertaAddView.fillForm(driver, "Pato",
//				"Vendo el pato de goma que me regalo mi tia cuando era pequeño. Por cierto, es de goma",
//				String.valueOf(-1));
//		PO_OfertaAddView.checkKey(driver, "Error.oferta.cantidad.negativa", PO_Properties.getSPANISH());
//
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba17] Mostrar el listado de ofertas para dicho usuario y comprobar que
//	// se muestran todas los que existen para este usuario.
//	@Test
//	public void Prueba17() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Mis Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/userlist')]");
//		elementos.get(0).click();
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@class, 'page-link')]");
//		// Comprobamos que las ofertas de este usuario esten presentes
//		List<Oferta> ofertas = ofertaService.getListaOfertas();
//		User usuario = usersService.getUserByEmail("UO101014@uniovi.es");
//		List<Oferta> userOfertas = new ArrayList<Oferta>();
//		// Eliminamos las ofertas que no sean del usuario
//		for (Oferta o : ofertas) {
//			if (o.getUser().getEmail() == usuario.getEmail())
//				userOfertas.add(o);
//		}
//		// Realizamos las comprobaciones en cada pagina
//		PO_OfertaUserList.comprobarAllOfertas(driver, usuario, userOfertas, elementos);
//
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba18] Ir a la lista de ofertas, borrar la primera oferta de la lista,
//	// comprobar que la lista se actualiza y que la oferta desaparece.
//	@Test
//	public void Prueba18() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		User usuario = usersService.getUserByEmail("UO101014@uniovi.es");
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Mis Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/userlist')]");
//		elementos.get(0).click();
//		// Comprobamos que este la primera oferta
//		List<Oferta> misOfertas = new ArrayList<Oferta>();
//		for (Oferta o : ofertaService.getListaOfertas())
//			if (usuario.getEmail().equals(o.getUser().getEmail()))
//				misOfertas.add(o);
//		int posicion = 0;
//		String titulo = misOfertas.get(posicion).getTitulo();
//		SeleniumUtils.textoPresentePagina(driver, titulo);
//		// Borramos la primera oferta
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/delete')]");
//		elementos.get(0).click();
//		// Comprobamos que no este la primera oferta
//		SeleniumUtils.textoNoPresentePagina(driver, titulo);
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba19] Ir a la lista de ofertas, borrar la última oferta de la lista,
//	// comprobar que la lista se actualiza y
//	// que la oferta desaparece.
//	@Test
//	public void Prueba19() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		User usuario = usersService.getUserByEmail("UO101014@uniovi.es");
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Mis Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/userlist')]");
//		elementos.get(0).click();
//		// Comprobamos que este la ultima oferta
//		List<Oferta> misOfertas = new ArrayList<Oferta>();
//		for (Oferta o : ofertaService.getListaOfertas())
//			if (usuario.getEmail().equals(o.getUser().getEmail()))
//				misOfertas.add(o);
//		int posicion = misOfertas.size() - 1;
//		String titulo = misOfertas.get(posicion).getTitulo();
//		PO_OfertaUserList.comprobarElementoDeLista(driver, posicion, misOfertas, true);
//		// Esperamos a que se muestren los enlaces de paginación la lista de notas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@class, 'page-link')]");
//		// Nos vamos a la última página
//		elementos.get(posicion).click();
//		// Borramos la ultima oferta
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/delete')]");
//		elementos.get(posicion).click();
//		// Comprobamos que no este la primera oferta
//		SeleniumUtils.textoNoPresentePagina(driver, titulo);
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba20] Hacer una búsqueda con el campo vacío y comprobar que se muestra
//	// la página que corresponde con el listado de las ofertas existentes en el
//	// sistema
//	@Test
//	public void Prueba20() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/list')]");
//		elementos.get(0).click();
//		// Clickamos en Buscar
//		elementos = PO_View.checkElement(driver, "free", "//button[contains(@type, 'submit')]");
//		elementos.get(0).click();
//		// Comprobamos que las ofertas de este usuario esten presentes
//		List<Oferta> ofertas = ofertaService.getListaOfertas();
//		User usuario = usersService.getUserByEmail("UO101014@uniovi.es");
//		// Realizamos las comprobaciones en cada pagina
//		PO_OfertaUserList.comprobarAllOfertasPaginaPorPagina(driver, usuario, ofertas, elementos);
//
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba21] Hacer una búsqueda escribiendo en el campo un texto que no exista
//	// y comprobar que se muestra la página que corresponde, con la lista de ofertas
//	// vacía.
//	@Test
//	public void Prueba21() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/list')]");
//		elementos.get(0).click();
//		// Realizamos una busqueda de algo inexistente
//		elementos = PO_View.checkElement(driver, "free", "//input[contains(@name, 'searchText')]");
//		elementos.get(0).click();
//		elementos.get(0).clear();
//		elementos.get(0).click();
//		elementos.get(0).sendKeys("nada");
//		// Clickamos en Buscar
//		By boton = By.className("btn");
//		driver.findElement(boton).click();
//		// Comprobamos que no se muestran ofertas
//		List<Oferta> ofertas = ofertaService.getListaOfertas();
//		for (Oferta o : ofertas)
//			SeleniumUtils.textoNoPresentePagina(driver, o.getTitulo());
//
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba22] Hacer una búsqueda escribiendo en el campo un texto en minúscula o
//	// mayúscula y
//	// comprobar que se muestra la página que corresponde, con la lista de ofertas
//	// que contengan
//	// dicho texto, independientemente que el título esté almacenado en minúsculas o
//	// mayúscula.
//	@Test
//	public void Prueba22() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/list')]");
//		elementos.get(0).click();
//		// Realizamos una busqueda de algo inexistente
//		elementos = PO_View.checkElement(driver, "free", "//input[contains(@name, 'searchText')]");
//		elementos.get(0).click();
//		elementos.get(0).clear();
//		elementos.get(0).click();
//		elementos.get(0).sendKeys("nada");
//		// Clickamos en Buscar
//		By boton = By.className("btn");
//		driver.findElement(boton).click();
//		// Comprobamos que no se muestran ofertas
//		List<Oferta> ofertas = ofertaService.getListaOfertas();
//		for (Oferta o : ofertas)
//			SeleniumUtils.textoNoPresentePagina(driver, o.getTitulo());
//
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba23] Sobre una búsqueda determinada (a elección del desarrollador),
//	// comprar una oferta que deja
//	// un saldo positivo en el contador del comprador. Comprobar que el contador se
//	// actualiza correctamente
//	// en la vista del comprador
//	@Test
//	public void Prueba23() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario -> usuario con saldo muy elevado.
//		PO_LoginView.fillForm(driver, "UO101011@uniovi.es", "123456");
//		User usuario = usersService.getUserByEmail("UO101011@uniovi.es");
//		double saldoInicial = usuario.getSaldo();
//		List<Oferta> ofertas = ofertaService.getListaOfertas();
//		Oferta oferta = null;
//		for (int i = 0; i < ofertas.size(); i++)
//			if (!ofertas.get(i).isComprada()) {
//				oferta = ofertas.get(i);
//				break;
//			}
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/list')]");
//		elementos.get(0).click();
//		// Realizamos una compra
//		elementos = PO_View.checkElement(driver, "free", "//button[contains(@id, 'buyButton')]");
//		elementos.get(0).click();
//		// Comprobamos el saldo
//		SeleniumUtils.textoPresentePagina(driver, "Saldo: " + (saldoInicial - oferta.getCantidad()));
//
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba24] Sobre una búsqueda determinada (a elección del desarrollador),
//	// comprar una oferta que deja un saldo 0 en el contador del comprador.
//	// Comprobar que el contador se actualiza correctamente en la
//	// vista del comprador.
//	@Test
//	public void Prueba24() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		// Creamos una oferta que comprara un nuevo usuario
//		PO_OfertaAddView.creaOferta(driver, "Balon de baloncesto",
//				"Pelota de baloncesto de maxima calidad, sin estrenar.", 100);
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//		// Nos registramos
//		// Vamos al formulario de registro
//		PO_HomeView.clickOption(driver, "signup", "class", "btn btn-primary");
//		PO_RegisterView.fillForm(driver, "prueba24@email.com", "El Pepe", "Perez", "123456", "123456");
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/list')]");
//		elementos.get(0).click();
//		// Realizamos la busqueda
//		elementos = PO_View.checkElement(driver, "free", "//input[contains(@name, 'searchText')]");
//		elementos.get(0).click();
//		elementos.get(0).clear();
//		elementos.get(0).click();
//		elementos.get(0).sendKeys("Balon de baloncesto");
//		// Clickamos en Buscar
//		By boton = By.className("btn");
//		driver.findElement(boton).click();
//		// Realizamos la compra que nos deje el saldo a 0
//		elementos = PO_View.checkElement(driver, "free", "//button[contains(@id, 'buyButton')]");
//		elementos.get(0).click();
//		// Comprobamos el saldo a 0
//		SeleniumUtils.textoPresentePagina(driver, "Saldo: 0");
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba25] Sobre una búsqueda determinada (a elección del desarrollador),
//	// intentar comprar una oferta que esté por encima de saldo disponible del
//	// comprador. Y comprobar que se muestra el mensaje de saldo no suficiente.
//	@Test
//	public void Prueba25() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		User usuario = usersService.getUserByEmail("UO101014@uniovi.es");
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/list')]");
//		elementos.get(0).click();
//		// Buscamos la oferta mas cara "Motocicleta"
//		elementos = PO_View.checkElement(driver, "free", "//input[contains(@name, 'searchText')]");
//		elementos.get(0).click();
//		elementos.get(0).clear();
//		elementos.get(0).click();
//		elementos.get(0).sendKeys("Motocicleta");
//
//		// Clickamos en Buscar
//		By boton = By.className("btn");
//		driver.findElement(boton).click();
//		String saldo = String.valueOf(usuario.getSaldo());
//		SeleniumUtils.textoPresentePagina(driver, saldo);
//
//		// Intentamos realizar la compra
//		elementos = PO_View.checkElement(driver, "free", "//button[contains(@id, 'buyButton')]");
//		elementos.get(0).click();
//		// No pasa nada y no podemos capturar el error ya que hacemos uso del metodo
//		// alert para notificarlo
//		// Por tanto para asegurar su correcto funcionamiento esperamos 2 seg
//		SeleniumUtils.esperarSegundos(driver, 2);
//		driver.navigate().to("http://localhost:8080/");
//		// El saldo no ha variado, la compra no se ha realizado
//		SeleniumUtils.textoPresentePagina(driver, saldo);
//		// Nos desconectamos
//		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
//	}
//
//	// [Prueba26] Ir a la opción de ofertas compradas del usuario y mostrar la
//	// lista. Comprobar que aparecen las ofertas que deben aparecer.
//	@Test
//	public void Prueba26() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "UO101010@uniovi.es", "123456");
//		// Pinchamos en la opción de menu de Usuarios: //li[contains(@id,
//		// 'users-menu')]/a
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Pinchamos en la opción de lista de usuarios.
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href,'oferta/userlistcompradas')]");
//		elementos.get(0).click();
//		// Comprobamos que entramos en las lista de compras
//		SeleniumUtils.textoPresentePagina(driver, "Mis Compras");
//		// Seleccionamos los usuarios que queremos eliminar
//		PO_OfertasCompradasListView.comprobarOfertasCompradas(driver, ofertaService, "UO101010@uniovi.es");
//
//	}
//
//	// [Prueba27] Al crear una oferta marcar dicha oferta como destacada y a
//	// continuación comprobar: i) que
//	// aparece en el listado de ofertas destacadas para los usuarios y que el saldo
//	// del usuario se actualiza
//	// adecuadamente en la vista del ofertante (-20).
//	@Test
//	public void Prueba27() {
//		// Vamos al formulario de login
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario.
//		PO_LoginView.fillForm(driver, "UO101014@uniovi.es", "123456");
//		// Comprobamos que el saldo del usuario es el que tiene que ser
//		User u = usersService.getUserByEmail("UO101014@uniovi.es");
//		double saldon = u.getSaldo();
//		String saldo = String.valueOf(saldon);
//		SeleniumUtils.textoPresentePagina(driver, saldo);
//		// Creamos la oferta
//		PO_OfertaAddView.creaOferta(driver, "Pato de goma",
//				"Vendo el pato de goma que me regalo mi tia cuando era pequeño. Por cierto, es de goma", 4, true);
//		// Vamos a la vista home
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'home')]/a");
//		elementos.get(0).click();
//		// Comprobamos que la oferta destacada esta en la lista de home
//		SeleniumUtils.textoPresentePagina(driver, "Pato de goma");
//		// Comprobamos que el saldo del usuario sea el del principio menos 20
//		saldo = String.valueOf(saldon - 20);
//		SeleniumUtils.textoPresentePagina(driver, saldo);
//
//	}
//
//	// [Prueba28] Sobre el listado de ofertas de un usuario con menos de 20 euros de
//	// saldo, pinchar en el
//	// enlace Destacada y a continuación comprobar: que aparece en el listado de
//	// ofertas destacadas para los
//	// usuarios y que el saldo del usuario se actualiza adecuadamente en la vista
//	// del ofertante (-20).
//	@Test
//	public void Prueba28() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "UO101010@uniovi.es", "123456");
//		// Comprobamos que el saldo del usuario es el que tiene que ser
//		User u = usersService.getUserByEmail("UO101010@uniovi.es");
//		double saldon = u.getSaldo();
//		String saldo = String.valueOf(saldon);
//		SeleniumUtils.textoPresentePagina(driver, saldo);
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Mis Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/userlist')]");
//		elementos.get(0).click();
//		// Comprobamos que la oferta que vamos a destacar esta presente
//		SeleniumUtils.textoPresentePagina(driver, "Patinete");
//		// Seleccionamos el boton de destacar
//		elementos = PO_View.checkElement(driver, "free", "//button[contains(@id, 'destacarButton')]");
//		elementos.get(0).click();
//		// Vamos a la vista home
//		elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'home')]/a");
//		elementos.get(0).click();
//		// Comprobamos que la oferta destacada esta en la lista de home
//		SeleniumUtils.textoPresentePagina(driver, "Patinete");
//		// Comprobamos que el saldo del usuario sea el del principio menos 20
//		saldo = String.valueOf(saldon - 20);
//		SeleniumUtils.textoPresentePagina(driver, saldo);
//	}
//
//	// [Prueba29] Sobre el listado de ofertas de un usuario con menos de 20 euros de
//	// saldo, pinchar en el
//	// enlace Destacada y a continuación comprobar que se muestra el mensaje de
//	// saldo no suficiente.
//	@Test
//	public void Prueba29() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "UO101010@uniovi.es", "123456");
//		// Comprobamos que el saldo del usuario es el que tiene que ser
//		User u = usersService.getUserByEmail("UO101010@uniovi.es");
//		double saldon = u.getSaldo();
//		String saldo = String.valueOf(saldon);
//		SeleniumUtils.textoPresentePagina(driver, saldo);
//		// Seleccionamos el menu ofertas
//		List<WebElement> elementos = PO_View.checkElement(driver, "free", "//li[contains(@id, 'ofertas-menu')]/a");
//		elementos.get(0).click();
//		// Accedemos a Ver Mis Ofertas
//		elementos = PO_View.checkElement(driver, "free", "//a[contains(@href, 'oferta/userlist')]");
//		elementos.get(0).click();
//		// Comprobamos que la oferta que vamos a destacar esta presente
//		SeleniumUtils.textoPresentePagina(driver, "Patinete");
//		// Seleccionamos el boton de destacar
//		elementos = PO_View.checkElement(driver, "free", "//button[contains(@id, 'destacarButton')]");
//		elementos.get(0).click();
//		// Comprobamos que ahora esta la otra oferta a destacar
//		SeleniumUtils.textoPresentePagina(driver, "Cesta picnic");
//		// Hemos destacado una para quedarnos sin dinero, ahora probaremos a destacar
//		// otra
//		elementos = PO_View.checkElement(driver, "free", "//button[contains(@id, 'destacarButton')]");
//		elementos.get(0).click();
//		// Usamos alert por tanto no podemos capturar el mensaje
//		// Por tanto para asegurar su correcto funcionamiento esperamos 2 seg
//		SeleniumUtils.esperarSegundos(driver, 2);
//		// Comprobamos que en home no esta la segunda oferta destacada
//		driver.navigate().to("http://localhost:8080");
//		SeleniumUtils.textoNoPresentePagina(driver, "Cesta picnic");
//		// Comprobamos que el saldo es el mismo
//		saldo = String.valueOf(saldon - 20);// -20 de la primera que destacamos
//		SeleniumUtils.textoPresentePagina(driver, saldo);
//
//	}
//
	// [Prueba30] Inicio de sesión con datos válidos.
	@Test
	public void Prueba30() {
		driver.navigate().to("https://localhost:8081/cliente.html");
		// Comprobamos que vamos a la vista de iniciar sesión
		SeleniumUtils.textoPresentePagina(driver, "Password:");
		SeleniumUtils.textoPresentePagina(driver, "Email:");
		
		// Iniciamos sesion con datos validos
		PO_LoginView.fillForm(driver, "prueba@uniovi.es", "123456");
		PO_View.checkElement(driver, "text", "Titulo");
		
		// Tras iniciar sesión con datos se nos mostrará la lista de ofertas
		SeleniumUtils.textoPresentePagina(driver, "Titulo");
		SeleniumUtils.textoPresentePagina(driver, "Descripción");
		SeleniumUtils.textoPresentePagina(driver, "Vendedor");
	}

	// [Prueba31] Inicio de sesión con datos inválidos (email existente, pero
	// contraseña incorrecta).
	@Test
	public void Prueba31() {
		// Intentamos acceder a lista de usuarios sin identificarnos
		driver.navigate().to("https://localhost:8081/cliente.html");
		// Comprobamos que vamos a la vista de iniciar sesión
		SeleniumUtils.textoPresentePagina(driver, "Password:");
		SeleniumUtils.textoPresentePagina(driver, "Email:");
		
		// Iniciamos sesion con datos invalidos
		PO_LoginView.fillForm(driver, "prueba@uniovi.es", "nada");
		
		// Comprobamos el mensaje de error
		SeleniumUtils.textoPresentePagina(driver, "Usuario no encontrado");
	}

	// [Prueba32] Inicio de sesión con datos inválidos (campo email o contraseña
	// vacíos)
	@Test
	public void Prueba32() {
		// Intentamos acceder a lista de usuarios sin identificarnos
		driver.navigate().to("https://localhost:8081/cliente.html");
		// Comprobamos que vamos a la vista de iniciar sesión
		SeleniumUtils.textoPresentePagina(driver, "Password:");
		SeleniumUtils.textoPresentePagina(driver, "Email:");
		
		// Iniciamos sesion con datos invalidos (usuario vacio)
		PO_LoginView.fillForm(driver, "", "123456");
		
		// Comprobamos el mensaje de error
		SeleniumUtils.textoPresentePagina(driver, "Usuario no encontrado");

	}

	// [Prueba33] Mostrar el listado de ofertas disponibles y comprobar que se
	// muestran todas las que
	// existen, menos las del usuario identificado.
	@Test
	public void Prueba33() {
		// Intentamos acceder a lista de usuarios sin identificarnos
		driver.navigate().to("https://localhost:8081/cliente.html");
		// Comprobamos que vamos a la vista de iniciar sesión
		SeleniumUtils.textoPresentePagina(driver, "Password:");
		SeleniumUtils.textoPresentePagina(driver, "Email:");
		
		// Iniciamos sesion
		PO_LoginView.fillForm(driver, "prueba@uniovi.es", "123456");
		
		// Comprobamos que ninguna de las ofertas sea del usuario identificado
		List<WebElement> vendedores = PO_View.checkElement(driver, "class", "vendedor");
		
		for(WebElement w : vendedores){
			assertNotEquals(w.getText(), "prueba@uniovi.es");
		}
		
		//Bson b = (Bson) JSON.parse("{titulo: 'Manzanas'}");
		//System.out.println(database.getCollection("ofertas").find().filter(b).first().toString());
		
	}

	// [Prueba34] Sobre una búsqueda determinada de ofertas (a elección de
	// desarrollador), enviar un
	// mensaje a una oferta concreta. Se abriría dicha conversación por primera vez.
	// Comprobar que el mensaje aparece en el listado de mensajes.
	@Test
	public void Prueba34() {
		// Intentamos acceder a lista de usuarios sin identificarnos
		driver.navigate().to("https://localhost:8081/cliente.html");
		// Comprobamos que vamos a la vista de iniciar sesión
		SeleniumUtils.textoPresentePagina(driver, "Password:");
		SeleniumUtils.textoPresentePagina(driver, "Email:");
		
		// Iniciamos sesion
		PO_LoginView.fillForm(driver, "prueba@uniovi.es", "123456");
	}

//	// [Prueba35] Sobre el listado de conversaciones enviar un mensaje a una conversación ya abierta.
//	// Comprobar que el mensaje aparece en el listado de mensajes.
//	@Test
//	public void Prueba35() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "UO101010@uniovi.es", "123456");
//		// Intentamos acceder a lista de usuarios identificados como usuario estandar
//		driver.navigate().to("http://localhost:8080/user/list");
//		// Comprobamos que aparece el texto que nos prohibe entrar
//		SeleniumUtils.textoPresentePagina(driver, "HTTP Status 403 – Forbidden");
//	}
//
//	// [Prueba36] Mostrar el listado de conversaciones ya abiertas. Comprobar que el listado contiene las
//	// conversaciones que deben ser
//	@Test
//	public void Prueba36() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "UO101010@uniovi.es", "123456");
//		// Intentamos acceder a lista de usuarios identificados como usuario estandar
//		driver.navigate().to("http://localhost:8080/user/list");
//		// Comprobamos que aparece el texto que nos prohibe entrar
//		SeleniumUtils.textoPresentePagina(driver, "HTTP Status 403 – Forbidden");
//	}
//
//	// [Prueba37] Sobre el listado de conversaciones ya abiertas. Pinchar el enlace Eliminar de la primera y
//	// comprobar que el listado se actualiza correctamente
//	@Test
//	public void Prueba37() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "UO101010@uniovi.es", "123456");
//		// Intentamos acceder a lista de usuarios identificados como usuario estandar
//		driver.navigate().to("http://localhost:8080/user/list");
//		// Comprobamos que aparece el texto que nos prohibe entrar
//		SeleniumUtils.textoPresentePagina(driver, "HTTP Status 403 – Forbidden");
//	}
//
//	// [Prueba38] Sobre el listado de conversaciones ya abiertas. Pinchar el enlace Eliminar de la última y
//	// comprobar que el listado se actualiza correctamente
//	@Test
//	public void Prueba38() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "UO101010@uniovi.es", "123456");
//		// Intentamos acceder a lista de usuarios identificados como usuario estandar
//		driver.navigate().to("http://localhost:8080/user/list");
//		// Comprobamos que aparece el texto que nos prohibe entrar
//		SeleniumUtils.textoPresentePagina(driver, "HTTP Status 403 – Forbidden");
//	}
//
//	// [Prueba39] Identificarse en la aplicación y enviar un mensaje a una oferta, validar que el mensaje
//	// enviado aparece en el chat. Identificarse después con el usuario propietario de la oferta y validar
//	// que tiene un mensaje sin leer, entrar en el chat y comprobar que el mensaje pasa a tener el estado
//	// leído.
//	@Test
//	public void Prueba39() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "UO101010@uniovi.es", "123456");
//		// Intentamos acceder a lista de usuarios identificados como usuario estandar
//		driver.navigate().to("http://localhost:8080/user/list");
//		// Comprobamos que aparece el texto que nos prohibe entrar
//		SeleniumUtils.textoPresentePagina(driver, "HTTP Status 403 – Forbidden");
//	}
//
//	// [Prueba40] Identificarse en la aplicación y enviar tres mensajes a una oferta, validar que los mensajes
//	// enviados aparecen en el chat. Identificarse después con el usuario propietario de la oferta y
//	// validar que el número de mensajes sin leer aparece en su oferta.
//	@Test
//	public void Prueba40() {
//		// Vamos al formulario de logueo.
//		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
//		// Rellenamos el formulario
//		PO_LoginView.fillForm(driver, "UO101010@uniovi.es", "123456");
//		// Intentamos acceder a lista de usuarios identificados como usuario estandar
//		driver.navigate().to("http://localhost:8080/user/list");
//		// Comprobamos que aparece el texto que nos prohibe entrar
//		SeleniumUtils.textoPresentePagina(driver, "HTTP Status 403 – Forbidden");
//	}

}
