module.exports = function(app, swig, gestorBD) {

    /**
     * /usuario/list
     *
     * Muestra la lista de usuario del sistema, carga la vista. Es solo accesible
     * para el administrador
     */
    app.get("/usuario/list", function(req, res) {
        gestorBD.obtenerUsuarios({},function(usuarios) {
            if (usuarios == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al listar",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                app.get("logger").info("ERROR : No se han podido listar los usuarios");
                res.send(respuestaError);
            } else {
                let respuesta = swig.renderFile('views/busuarios.html',
                    {
                        usuarios : usuarios,
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                app.get("logger").info("Usuarios listados con éxito");
                res.send(respuesta);
            }
        });
    });

    /**
     * /usuario/eliminar
     *
     * Elimina los usuarios seleccionados por el administrador
     */
    app.post('/usuario/eliminar', function(req, res) {
        let usuariosIds =req.body.idChecked;
        let array=Array.isArray(req.body.idChecked);
        let error=false;
        if (usuariosIds!==undefined){
            for (let i = 0; i < usuariosIds.length; i++){
                let criterio = {"email": usuariosIds[i]};
                let criterioOfertas =
                {
                    "vendedor": usuariosIds[i],
                    "comprador": null
                };
                let criterioMensajes={$or: [{"emailVendedor":usuariosIds[i]},{"emailInteresado":usuariosIds[i]}]};
                if(!array){
                    criterio={"email": usuariosIds};
                    criterioOfertas={
                        "vendedor": usuariosIds,
                        "comprador": null
                    };
                    criterioMensajes={$or: [{"emailVendedor":usuariosIds},{"emailInteresado":usuariosIds}]};
                }
                gestorBD.eliminarUsuario(criterio, function (id) {
                    if (id == null) {
                        error=true;
                    } else{
                        gestorBD.obtenerOfertas(criterioOfertas, function(ofertas){
                            for (i = 0; i < ofertas.length; i++) {
                                let criterioBorrado = {"_id" : ofertas[i]._id }
                                gestorBD.eliminarOferta(criterioBorrado, function(ofertas){
                                });
                            }
                            gestorBD.eliminarMensaje(criterioMensajes,function(mensajes){
                            });
                        });
                    }
                });
                if(!array){
                    break;
                }
            }
        }
        if(error===true){
            let respuestaError = swig.renderFile('views/error.html',
                {
                    mensajes: "Error al eliminar",
                    usuario: req.session.usuario,
                    rol: req.session.rol,
                    saldo : req.session.saldo
                });
            app.get("logger").info("ERROR: No se ha podido eliminar el usuario/s seleccionado/s");
            res.send(respuestaError);
        }else{
            app.get("logger").info("Usuario/s eliminado/s con éxito");
            res.redirect("/usuario/list?mensaje=Usuario/s eliminado/s correctamente");
        }
    });

    /**
     * /registrarse
     *
     * Carga la vista de registro para que un usuario pueda registrarse
     */
    app.get("/registrarse", function(req, res) {
        let respuesta = swig.renderFile('views/bregistro.html', {});
        app.get("logger").info("Accediendo a la vista de registro");
        res.send(respuesta);
    });

    /**
     * /identificarse
     *
     * Carga la vista de identificación para que un usuario ya registrado pueda
     * iniciar sesión
     */
    app.get("/identificarse", function(req, res) {
        let respuesta = swig.renderFile('views/bidentificacion.html', {});
        app.get("logger").info("Accediendo a la vista de inicio de sesión");
        res.send(respuesta);
    });

    /**
     * /home
     *
     * Carga la vista de home, que se muestra al iniciar sesión con un usuario.
     * En ella se cargan las ofertas destacadas
     */
    app.get("/home", function(req, res) {
        let criterio = { "destacada" : true };
        gestorBD.obtenerOfertas(criterio, function (ofertas) {
            if(ofertas == null){
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al listar",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                app.get("logger").info("ERROR : En el home del usuario "+req.session.usuario+" no se han podido cargar las ofertas destacadas");
                res.send(respuestaError);
            }
            let respuesta = swig.renderFile('views/home.html', {
                usuario : req.session.usuario,
                rol : req.session.rol,
                saldo : req.session.saldo,
                ofertas : ofertas
            });
            app.get("logger").info("Accediendo a la página privada del usuario: "+req.session.usuario);
            res.send(respuesta);
        });
    });

    /**
     * /identificarse
     *
     * Identifica al usuario tras rellenar el formulario de inicio de sesión,
     * comprobando que este está en la base de datos ya registrado
     */
    app.post("/identificarse", function(req, res) {
        let seguro = app.get("crypto").createHmac('sha256', app.get('clave'))
            .update(req.body.password).digest('hex');
        let criterio = {
            email : req.body.email,
            password : seguro
        }
        gestorBD.obtenerUsuarios(criterio, function(usuarios) {
            if (usuarios == null || usuarios.length === 0) {
                app.get("logger").info("ERROR: Inicio de sesión fallido");
                req.session.usuario = null
                res.redirect("/identificarse" +
                    "?mensaje=Email o password incorrecto"+
                    "&tipoMensaje=alert-danger ");
            } else {
                req.session.rol = usuarios[0].rol;
                req.session.usuario = usuarios[0].email;
                req.session.saldo = usuarios[0].saldo;
                app.get("logger").info("Inicio de sesión correcto del usuario: "+req.session.usuario);
                res.redirect("/home");
            }
        });
    });

    /**
     * /desconectarse
     *
     * Desconecta al usuario de sesión y lo redirige al login
     */
    app.get('/desconectarse', function (req, res) {
        app.get("logger").info("El usuario "+req.session.usuario+" se ha desconectado");
        req.session.usuario = null;
        res.redirect("/identificarse");
    });

    /**
     * /usuario
     *
     * Registra a un usuario en base de datos, haciendo una serie de validaciones y comprobando
     * que no está ya registrado. Si se realiza con éxito se le inicia sesión directamente
     */
    app.post('/usuario', function(req, res) {
        let seguro = app.get("crypto").createHmac('sha256', app.get('clave'))
            .update(req.body.password).digest('hex');
        let usuario = {
            email : req.body.email,
            password : seguro,
            nombre : req.body.nombre,
            apellidos : req.body.apellidos,
            rol : "usuario",
            saldo : parseInt("100")
        }
        let mensaje=validacionRegistro(usuario,req.body.password,req.body.passwordc);
        let criterio ={"email": req.body.email};
        gestorBD.obtenerUsuarios(criterio, function(usuarios) {
            if (usuarios == null || usuarios.length===0) {
                if(mensaje==="") {
                    gestorBD.insertarUsuario(usuario, function (id) {
                        if (id == null) {
                            res.redirect("/registrarse?mensaje=Error al registrar usuario");
                        } else {
                            req.session.rol = usuario.rol;
                            req.session.usuario = usuario.email;
                            req.session.saldo = usuario.saldo;
                            app.get("logger").info("El usuario con email "+req.session.usuario +" ha sido registrado con éxito");
                            res.redirect("/home?mensaje=Nuevo usuario registrado");
                        }
                    });
                }else{
                    req.session.usuario = null
                    app.get("logger").info("ERROR: "+mensaje);
                    res.redirect("/registrarse" +
                        "?mensaje="+mensaje+"&tipoMensaje=alert-danger ");
                }
            } else {
                req.session.usuario = null
                app.get("logger").info("ERROR: Ya existe un usuario con el email "+req.body.email);
                res.redirect("/registrarse" +
                    "?mensaje=Este email ya está en uso<br>"+mensaje+"&tipoMensaje=alert-danger ");
            }
        });
    });

    /**
     * Método para validar los campos del registro
     * @param usuario Usuario con los datos introducidos
     * @param password Contraseña del usuario
     * @param passwordConfirm Contraseña de confirmación
     * @returns {string} Devuelve una cadena con los errores en el caso de que haya.
     */
    function validacionRegistro(usuario,password,passwordConfirm) {
        let mensaje="";
        if (usuario.nombre.length<5){
            mensaje+="El nombre debe tener al menos 5 caracteres<br>";
        }
        if (usuario.apellidos.length<5){
            mensaje+="Los apellidos deben tener al menos 5 caracteres<br>";
        }
        if (password.length<5){
            mensaje+="La contraseña debe tener al menos 5 caracteres<br>";
        }
        if (password!==passwordConfirm){
            mensaje+="Las contraseñas deben ser iguales<br>";
        }
        return mensaje;
    }
};