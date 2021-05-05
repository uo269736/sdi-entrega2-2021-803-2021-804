module.exports = function(app, swig, gestorBD) {

    app.get("/usuario/list", function(req, res) {
        gestorBD.obtenerUsuarios({},function(usuarios, total ) {
            if (usuarios == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al listar",
                        usuario : req.session.usuario,
                        rol : req.session.rol
                    });
                res.send(respuestaError);
            } else {
                let respuesta = swig.renderFile('views/busuarios.html',
                    {
                        usuarios : usuarios,
                        usuario : req.session.usuario,
                        rol : req.session.rol
                    });
                res.send(respuesta);
            }
        });
    });


    //COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS//COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS//COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS//COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS
    //COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS//COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS//COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS//COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS
    //COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS//COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS//COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS//COMPROBAR QUE SE ELIMINEN LAS OFERTAS Y DEMAS
    app.post('/usuario/eliminar', function(req, res) {
        let usuariosIds =req.body.idChecked;
        let array=Array.isArray(req.body.idChecked);
        let error=false;
        if (usuariosIds!=undefined){
            for (let i = 0; i < usuariosIds.length; i++){
                let criterio = {"email": usuariosIds[i]};
                if(!array){
                    criterio={"email": usuariosIds}
                }
                gestorBD.eliminarUsuario(criterio, function (id) {
                    if (id == null) {
                        error=true;
                    }
                });
                if(!array){
                    break;
                }
            }
            if(error==true){
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes: "Error al eliminar",
                        usuario: req.session.usuario,
                        rol: req.session.rol
                    });
                res.send(respuestaError);
            }else{
                res.redirect("/usuario/list?mensaje=Usuario/s eliminado/s correctamente");
            }
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
        let respuesta = swig.renderFile('views/home.html', {
            usuario : req.session.usuario,
            rol : req.session.rol
        });
        res.send(respuesta);
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
            rol : "usuario"
        }

        gestorBD.insertarUsuario(usuario, function(id) {
            if (id == null){
                res.redirect("/registrarse?mensaje=Error al registrar usuario");
            } else {
                req.session.rol = usuario.rol;
                req.session.usuario = usuario.email;
                res.redirect("/home?mensaje=Nuevo usuario registrado");
            }
        });
    });
};