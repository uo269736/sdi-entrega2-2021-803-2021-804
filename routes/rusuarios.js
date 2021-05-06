module.exports = function(app, swig, gestorBD) {

    app.get("/usuario/list", function(req, res) {
        gestorBD.obtenerUsuarios({},function(usuarios, total ) {
            if (usuarios == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al listar",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                let respuesta = swig.renderFile('views/busuarios.html',
                    {
                        usuarios : usuarios,
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuesta);
            }
        });
    });

    app.post('/usuario/eliminar', function(req, res) {
        let usuariosIds =req.body.idChecked;
        let array=Array.isArray(req.body.idChecked);
        let error=false;
        if (usuariosIds!=undefined){
            for (let i = 0; i < usuariosIds.length; i++){
                let criterio = {"email": usuariosIds[i]};
                let criterioOfertas =
                {
                    "vendedor": usuariosIds[i],
                    "comprador": null
                };

                if(!array){
                    criterio={"email": usuariosIds};
                    criterioOfertas={
                        "vendedor": usuariosIds,
                        "comprador": null
                    };
                }
                gestorBD.eliminarUsuario(criterio, function (id) {
                    if (id == null) {
                        error=true;
                    } else{
                        gestorBD.obtenerOfertas(criterioOfertas, function(ofertas){
                            for (i = 0; i < ofertas.length; i++) {
                                let criterioBorrado = {"_id" : ofertas[i]._id }
                                gestorBD.eliminarOferta(criterioBorrado, function(ofertas){});
                            }
                        });
                    }
                });
                if(!array){
                    break;
                }
            }
        }
        if(error==true){
            let respuestaError = swig.renderFile('views/error.html',
                {
                    mensajes: "Error al eliminar",
                    usuario: req.session.usuario,
                    rol: req.session.rol,
                    saldo : req.session.saldo
                });
            res.send(respuestaError);
        }else{
            res.redirect("/usuario/list?mensaje=Usuario/s eliminado/s correctamente");
        }
    });

    app.get("/usuarios", function(req, res) {
        res.send("ver usuarios");
    });

    app.get("/registrarse", function(req, res) {
        let respuesta = swig.renderFile('views/bregistro.html', {});
        res.send(respuesta);
    });

    app.get("/identificarse", function(req, res) {
        let respuesta = swig.renderFile('views/bidentificacion.html', {});
        res.send(respuesta);
    });

    app.get("/home", function(req, res) {
        let criterio = { "destacada" : true };
        gestorBD.obtenerOfertas(criterio, function (ofertas) {
            let respuesta = swig.renderFile('views/home.html', {
                usuario : req.session.usuario,
                rol : req.session.rol,
                saldo : req.session.saldo,
                ofertas : ofertas
            });
            res.send(respuesta);
        });
    });

    app.post("/identificarse", function(req, res) {
        let seguro = app.get("crypto").createHmac('sha256', app.get('clave'))
            .update(req.body.password).digest('hex');
        let criterio = {
            email : req.body.email,
            password : seguro
        }
        gestorBD.obtenerUsuarios(criterio, function(usuarios) {
            if (usuarios == null || usuarios.length == 0) {
                req.session.usuario = null
                res.redirect("/identificarse" +
                    "?mensaje=Email o password incorrecto"+
                    "&tipoMensaje=alert-danger ");
            } else {
                req.session.rol = usuarios[0].rol;
                req.session.usuario = usuarios[0].email;
                req.session.saldo = usuarios[0].saldo;
                res.redirect("/home");
            }
        });
    });

    app.get('/desconectarse', function (req, res) {
        req.session.usuario = null;
        res.redirect("/identificarse");
    });

    app.post('/usuario', function(req, res) {
        let seguro = app.get("crypto").createHmac('sha256', app.get('clave'))
            .update(req.body.password).digest('hex');
        let usuario = {
            email : req.body.email,
            password : seguro,
            nombre : req.body.nombre,
            apellidos : req.body.apellidos,
            rol : "usuario",
            saldo : 100
        }
        let mensaje=validacionRegistro(usuario,req.body.password,req.body.passwordc);
        let criterio ={"email": req.body.email};
        gestorBD.obtenerUsuarios(criterio, function(usuarios) {
            if (usuarios == null || usuarios.length==0) {
                if(mensaje=="") {
                    gestorBD.insertarUsuario(usuario, function (id) {
                        if (id == null) {
                            res.redirect("/registrarse?mensaje=Error al registrar usuario");
                        } else {
                            req.session.rol = usuario.rol;
                            req.session.usuario = usuario.email;
                            req.session.saldo = usuario.saldo;
                            res.redirect("/home?mensaje=Nuevo usuario registrado");
                        }
                    });
                }else{
                    req.session.usuario = null
                    res.redirect("/registrarse" +
                        "?mensaje="+mensaje+"&tipoMensaje=alert-danger ");
                }
            } else {
                req.session.usuario = null
                res.redirect("/registrarse" +
                    "?mensaje=Este email ya está en uso<br>"+mensaje+"&tipoMensaje=alert-danger ");
            }
        });
    });

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
        if (password!=passwordConfirm){
            mensaje+="Las contraseñas deben ser iguales<br>";
        }
        return mensaje;
    };
};