module.exports = function(app, gestorBD) {

    app.post("/api/autenticar/", function(req, res) {
        let seguro = app.get("crypto").createHmac('sha256', app.get('clave'))
            .update(req.body.password).digest('hex');
        let criterio = {
            email : req.body.email,
            password : seguro
        }
        // ¿Validar nombre, genero, precio?

        gestorBD.obtenerUsuarios(criterio, function(usuarios){
            if (usuarios == null || usuarios.length == 0) {
                res.status(401);    // Usuario no autorizado
                res.json({
                    autenticado : false
                })
            } else {
                let token = app.get('jwt').sign(
                    {usuario: criterio.email , tiempo: Date.now()/1000},
                    "secreto");
                req.session.usuario=criterio.email;
                res.status(200);
                res.json({
                    autenticado : true,
                    token : token
                })
            }
        });
    });

    app.get("/api/oferta", function(req, res) {
        gestorBD.obtenerOfertas({} , function(ofertas) {
            if (ofertas == null) {
                res.status(500);
                res.json({
                    error : "se ha producido un error"
                })
            } else {
                let ofertasA=ofertasAjenas(ofertas,req.session.usuario)
                res.status(200);
                res.send( JSON.stringify(ofertasA) );
            }
        });
    });

    app.get("/api/oferta/:id", function(req, res) {
        let criterio = { "_id" : gestorBD.mongo.ObjectID(req.params.id)}
        gestorBD.obtenerOfertas(criterio,function(ofertas){
            if ( ofertas == null ){
                res.status(500);
                res.json({
                    error : "se ha producido un error"
                })
            } else {
                res.status(200);
                res.send( JSON.stringify(ofertas[0]) );
            }
        });
    });

    app.get("/api/chat/:idOferta/:emailVendedor/:emailInteresado", function(req, res) {
        let criterio ={"idOferta" : req.params.idOferta,"emailVendedor":req.params.emailVendedor,"emailInteresado": req.params.emailInteresado}

        // En este metodo tambien marcaremos los mensajes como leídos si usuario = destinatrio
        let escritor="";
        // Para los mensajes que marcaremos como leidos, su escritor no puede ser el usuario en sesion
        if(req.session.usuario==req.params.emailVendedor)
            escritor = req.params.emailInteresado;
        else
            escritor = req.params.emailVendedor;
        let criterioMarcarLeido = {"idOferta" : req.params.idOferta,"emailVendedor":req.params.emailVendedor,"emailInteresado": req.params.emailInteresado, "escritor": escritor, "leido": false}
        // Comprobamos si tenemos mensajes que marcar como vistos
        gestorBD.marcarComoLeido(criterioMarcarLeido, function (result){
            gestorBD.obtenerMensajes(criterio , function(mensajes) {

                if (mensajes == null) {
                    res.status(500);
                    res.json({
                        error : "se ha producido un error"
                    })
                } else {
                    res.status(200);
                    res.send( JSON.stringify(mensajes) );
                }
            });
        });
    });

    app.post("/api/chat/enviarMensaje", function(req, res) {
        let mensaje = {
            idOferta : req.body.idOferta,
            emailVendedor : req.body.vendedor,
            emailInteresado : req.body.interesado,
            escritor : req.session.usuario,
            fecha : d.getDate()+"/"+(d.getMonth()+1)+"/"+d.getFullYear()+" - "+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds(),
            leido : false,
            texto :req.body.mensaje
        }
        gestorBD.insertarMensaje(mensaje, function(mensajes){
            if (mensajes == null) {
                res.status(401);    // Usuario no autorizado
                res.json({
                    error : "se ha producido un error"
                })
            } else {
                res.status(200);
                res.json(JSON.stringify(mensaje))
            }
        });
    });

    function propietario(req, criterio){
        gestorBD.obtenerOfertas(criterio,function(ofertas) {
            if (req.session.usuario == null || ofertas == null)
                return false;
            else {
                return req.session.usuario == (ofertas[0].vendedor);
            }
        });
    }

    function ofertasAjenas(ofertas, usuario){
        let ofertasAjenas=[];
        for(let i=0;i<ofertas.length;i++) {
            if (ofertas[i].vendedor != usuario)
                ofertasAjenas.push(ofertas[i]);
        }
        return ofertasAjenas;
    }
};