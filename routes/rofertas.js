//Función que exportamos como módulo
module.exports = function(app,swig,gestorBD) {


    app.get('/oferta/agregar', function (req, res) {
        let respuesta = swig.renderFile('views/bagregar.html', {
            usuario : req.session.usuario,
            rol : req.session.rol,
            saldo : req.session.saldo
        });
        res.send(respuesta);
    });

    app.get('/oferta/eliminar/:id', function (req, res) {
        let criterio = {"_id" : gestorBD.mongo.ObjectID(req.params.id) };
        gestorBD.eliminarCancion(criterio,function(canciones){
            if ( canciones == null ){
                res.send(respuesta);
            } else {
                res.redirect("/publicaciones");
            }
        });
    });

    app.get("/oferta/list", function(req, res) {
        let criterio = {};
        if( req.query.busqueda != null ){
            criterio = { "titulo" : {$regex : ".*"+req.query.busqueda+".*"} };
        }

        let pg = parseInt(req.query.pg); // Es String !!!
        if ( req.query.pg == null){ // Puede no venir el param
            pg = 1;
        }

        gestorBD.obtenerOfertasPg(criterio, pg , function(ofertas, total ) {
            if (ofertas == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al listar",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                let ultimaPg = total/4;
                if (total % 4 > 0 ){ // Sobran decimales
                    ultimaPg = ultimaPg+1;
                }
                let paginas = []; // paginas mostrar
                for(let i = pg-2 ; i <= pg+2 ; i++){
                    if ( i > 0 && i <= ultimaPg){
                        paginas.push(i);
                    }
                }
                let respuesta = swig.renderFile('views/bofertas.html',
                    {
                        ofertas : ofertas,
                        paginas : paginas,
                        actual : pg,
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuesta);
            }
        });
    });

    app.get("/oferta/propias", function(req, res) {
        let criterio = { vendedor : req.session.usuario };
        gestorBD.obtenerOfertas(criterio, function(ofertas) {
            if (ofertas == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al listar",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                let respuesta = swig.renderFile('views/bpropias.html',
                    {
                        ofertas : ofertas,
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuesta);
            }
        });
    });

    app.get('/oferta/comprar/:id', function (req, res) {
        let cancionId = gestorBD.mongo.ObjectID(req.params.id);
        let compra = {
            usuario: req.session.usuario,
            cancionId: cancionId
        }
        let criterioCancion = {"_id": gestorBD.mongo.ObjectID(req.params.id)};
        gestorBD.obtenerCanciones(criterioCancion, function (canciones) {
            if (canciones == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al recuperar la canción",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                let criterioCompra = {"usuario": req.session.usuario};
                gestorBD.obtenerCompras(criterioCompra, function (compras) {
                    if (compras == null) {
                        let respuestaError = swig.renderFile('views/error.html',
                            {
                                mensajes : "Error al listar",
                                usuario : req.session.usuario,
                                rol : req.session.rol,
                                saldo : req.session.saldo
                            });
                        res.send(respuestaError);
                    } else {
                        if (isVendedorOrComprada(compras, canciones[0], req.session.usuario)) {
                            let respuestaError = swig.renderFile('views/error.html',
                                {
                                    mensajes : "No puedes comprar si eres el autor o ya la tienes comprada",
                                    usuario : req.session.usuario,
                                    rol : req.session.rol,
                                    saldo : req.session.saldo
                                });
                            res.send(respuestaError);
                        } else {
                            gestorBD.insertarCompra(compra, function (idCompra) {
                                if (idCompra == null) {
                                    res.send(respuesta);
                                } else {
                                    res.redirect("/compras");
                                }
                            });
                        }
                    }
                });
            }
            ;
        });
    });

    app.get('/oferta/compradas', function (req, res) {
        let criterio = { "usuario" : req.session.usuario };

        gestorBD.obtenerCompras(criterio ,function(compras){
            if ( compras == null ){
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al listar",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                let cancionesCompradasIds = [];
                for(i=0; i<compras.length; i++){
                    cancionesCompradasIds.push( compras[i].cancionId );
                }
                let criterio = { "_id" : { $in: cancionesCompradasIds } }
                gestorBD.obtenerCanciones(criterio,function (canciones){
                    let respuesta = swig.renderFile('views/bcompras.html',
                        {
                            canciones : canciones,
                            usuario : req.session.usuario,
                            rol : req.session.rol,
                            saldo : req.session.saldo
                        });
                    res.send(respuesta);
                });
            }
        });
    });

    app.get('/oferta/:id', function(req, res) {
        let criterio = {"_id": gestorBD.mongo.ObjectID(req.params.id)};
        gestorBD.obtenerOfertas(criterio, function (ofertas) {
            if (ofertas == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes: "Error al recuperar la oferta.",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                /*let configuracion = {
                    url: "https://www.freeforexapi.com/api/live?pairs=EURUSD",
                    method: "get",
                    headers: {
                        "token": "ejemplo",
                    }
                }
                let rest = app.get("rest");
                rest(configuracion, function (error, response, body) {
                    console.log("cod: " + response.statusCode + " Cuerpo :" + body);
                    let objetoRespuesta = JSON.parse(body);
                    let cambioUSD = objetoRespuesta.rates.EURUSD.rate;
                     nuevo campo "usd"
                    ofertas[0].usd = cambioUSD * ofertas[0].precio;*/

                    criterio = {"usuario": req.session.usuario};
                    gestorBD.obtenerCompras(criterio, function (compras) {
                        let respuesta = swig.renderFile('views/boferta.html',
                            {
                                oferta: ofertas[0],
                                propietario: isVendedorOrComprada(compras, ofertas[0], req.session.usuario)
                            });
                        res.send(respuesta);
                    });
                //});
            }
        });
    });

    app.post("/oferta", function (req, res){
        let oferta = {
            titulo : req.body.nombre,
            descripcion : req.body.descripcion,
            precio : req.body.precio,
            vendedor: req.session.usuario,
            comprador : null
        }
        // Conectarse
        gestorBD.insertarOferta(oferta, function(id){
            if (id == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al insertar oferta",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                res.redirect("/oferta/list");
            }
        });
    });

    function isVendedorOrComprada(compras, oferta, usuario) {
        if (oferta.autor == usuario) {
            return true;
        } else {
            for (let i = 0; i < compras.length; i++) {
                if ((compras[i].ofertaId).equals(oferta._id)) {
                    return true;
                }
            }
            return false;
        }
    };
};
